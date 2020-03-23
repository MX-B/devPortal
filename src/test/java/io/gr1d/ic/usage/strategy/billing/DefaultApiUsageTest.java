package io.gr1d.ic.usage.strategy.billing;

import io.gr1d.ic.usage.api.billing.model.InvoiceRequestItem;
import io.gr1d.ic.usage.api.billing.model.InvoiceRequestSplit;
import io.gr1d.ic.usage.api.subscriptions.model.*;
import io.gr1d.ic.usage.model.audit.Invoice;
import io.gr1d.ic.usage.strategy.ApiEndpointUsage;
import io.gr1d.ic.usage.strategy.ApiUsage;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultApiUsageTest {

    private static final String USER_ID = "25f35df7-af7c-4bdb-880e-7d5d503d5ec7";
    private static final String API_ID = "b6a3b331-249b-4a25-80ee-d7d62c9b880c";
    private static final String PLAN_ID = "e16d1399-ce86-40c0-8a1f-5d3a5f2251e2";
    private static final String PROVIDER_ID = "a6a3b331-ce86-40c0-8a1f-5d3a5f28274";
    private static final String API_NAME = "Payments";
    private static final String PLAN_NAME = "BASIC";
    private static final String PLAN_DESC = "Payments - Description";
    private static final String MODALITY = "DEFAULT";
    private static final String GATEWAY_UUID = "5d3a5f2251e2";

    private DefaultApiUsage defaultApiUsage;

    @Before
    public void setUp() {
        defaultApiUsage = new DefaultApiUsage();
    }

    @Test
    public void testExecution() {
        final Invoice invoice = new Invoice();
        final SubscriptionResponse subscription = new SubscriptionResponse();
        final ApiGatewayResponse apiGatewayResponse = new ApiGatewayResponse();
        final GatewayResponse gatewayResponse = new GatewayResponse();
        final ProviderResponse providerResponse = new ProviderResponse();

        providerResponse.setName("PROVIDER");
        providerResponse.setUuid(PROVIDER_ID);

        gatewayResponse.setUuid(GATEWAY_UUID);

        subscription.setUserId(USER_ID);
        subscription.setApi(apiGatewayResponse);
        subscription.setPercentageTenantSplit(BigDecimal.valueOf(25.55));
        apiGatewayResponse.setApiUuid(API_ID);
        apiGatewayResponse.setName(API_NAME);
        apiGatewayResponse.setSplitMode("AUTO");
        apiGatewayResponse.setGateway(gatewayResponse);
        apiGatewayResponse.setProvider(providerResponse);

        final PlanResponse apiPlanResponse = new PlanResponse();

        apiPlanResponse.setUuid(PLAN_ID);
        apiPlanResponse.setDescription(PLAN_DESC);
        apiPlanResponse.setName(PLAN_NAME);
        apiPlanResponse.setModality(MODALITY);
        apiPlanResponse.setValue(7500L);
        apiGatewayResponse.setApiPlan(apiPlanResponse);

        final PlanEndpointResponse planApiEndpointResponseTest = new PlanEndpointResponse();
        apiPlanResponse.getPlanEndpoints().add(planApiEndpointResponseTest);
        planApiEndpointResponseTest.setEndpoint("/test");
        planApiEndpointResponseTest.setMetadata(buildMetadata(0, 0));

        final PlanEndpointResponse planApiEndpointResponseTest2 = new PlanEndpointResponse();
        apiPlanResponse.getPlanEndpoints().add(planApiEndpointResponseTest2);
        planApiEndpointResponseTest2.setEndpoint("/test2");
        planApiEndpointResponseTest2.setMetadata(buildMetadata(10000, 15000));

        final PlanResponse planResponse = new PlanResponse();

        subscription.setPlan(planResponse);
        planResponse.setUuid(PLAN_ID);
        planResponse.setDescription(PLAN_DESC);
        planResponse.setName(PLAN_NAME);
        planResponse.setModality(MODALITY);
        planResponse.setValue(15000L);

        final TenantResponse tenantResponse = new TenantResponse();
        subscription.setTenant(tenantResponse);
        tenantResponse.setName("TENANT");
        tenantResponse.setRealm("tenant");

        final PlanEndpointResponse planEndpointResponseTest = new PlanEndpointResponse();
        planResponse.getPlanEndpoints().add(planEndpointResponseTest);
        planEndpointResponseTest.setEndpoint("/test");
        planEndpointResponseTest.setMetadata(buildMetadata(0, 0));

        final PlanEndpointResponse planEndpointResponseTest2 = new PlanEndpointResponse();
        planResponse.getPlanEndpoints().add(planEndpointResponseTest2);
        planEndpointResponseTest2.setEndpoint("/test2");
        planEndpointResponseTest2.setMetadata(buildMetadata(10000, 30000));

        final ApiUsage userApiUsage = new ApiUsage(0, API_ID);
        final ApiEndpointUsage userApiEndpointUsageTest = new ApiEndpointUsage(14548, "/test");
        userApiUsage.add(userApiEndpointUsageTest);

        final ApiEndpointUsage userApiEndpointUsageTest2 = new ApiEndpointUsage(20000, "/test2");
        userApiUsage.add(userApiEndpointUsageTest2);

        userApiUsage.add(new ApiEndpointUsage(152618, "/freeEndpoint"));

        final List<InvoiceRequestItem> items = defaultApiUsage.billApiUsage(subscription, userApiUsage, invoice);

        assertThat(items).hasSize(4);
        assertThat(items.get(0).getItemId()).isEqualTo(API_ID);
        assertThat(items.get(0).getQuantity()).isEqualTo(1);
        assertThat(items.get(0).getUnitValue().toString()).isEqualTo("150.000000");
        assertThat(items.get(0).getDescription()).isEqualTo("Payments API - Assinatura do Plano BASIC (187.166 hits utilizados)");

        assertThat(items.get(1).getItemId()).isEqualTo(API_ID);
        assertThat(items.get(1).getQuantity()).isEqualTo(14548);
        assertThat(items.get(1).getUnitValue()).isEqualTo(BigDecimal.ZERO);
        assertThat(items.get(1).getDescription()).isEqualTo("Payments API - Consumo do Endpoint /test - (0 hits) - 14.548 hits utilizados, 0 hits excedentes");

        assertThat(items.get(2).getItemId()).isEqualTo(API_ID);
        assertThat(items.get(2).getQuantity()).isEqualTo(10000);
        assertThat(items.get(2).getUnitValue().toString()).isEqualTo("0.030000");
        assertThat(items.get(2).getDescription()).isEqualTo("Payments API - Consumo do Endpoint /test2 - (10.000 hits) - 20.000 hits utilizados, 10.000 hits excedentes");

        assertThat(items.get(3).getItemId()).isEqualTo(API_ID);
        assertThat(items.get(3).getQuantity()).isEqualTo(152618);
        assertThat(items.get(3).getUnitValue().toString()).isEqualTo("0");
        assertThat(items.get(3).getDescription()).isEqualTo("Payments API - Consumo do Endpoint /freeEndpoint - 152.618 hits utilizados");

        assertThat(invoice.getItems()).hasSize(4);
        assertThat(invoice.getItems().get(0).getApiUuid()).isEqualTo(API_ID);
        assertThat(invoice.getItems().get(0).getHits()).isEqualTo(187166L);
        assertThat(invoice.getItems().get(0).getChargedAmount().doubleValue()).isEqualTo(150.00);
        assertThat(invoice.getItems().get(0).getEndpoint()).isEqualTo(items.get(0).getEndpoint());
        assertThat(invoice.getItems().get(0).getInvoice()).isEqualTo(invoice);
        assertThat(invoice.getItems().get(0).getCreatedAt()).isNotNull();

        assertThat(invoice.getItems().get(1).getApiUuid()).isEqualTo(API_ID);
        assertThat(invoice.getItems().get(1).getHits()).isEqualTo(14548L);
        assertThat(invoice.getItems().get(1).getChargedAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(invoice.getItems().get(1).getEndpoint()).isEqualTo(items.get(1).getEndpoint());
        assertThat(invoice.getItems().get(1).getInvoice()).isEqualTo(invoice);
        assertThat(invoice.getItems().get(1).getCreatedAt()).isNotNull();

        assertThat(invoice.getItems().get(2).getApiUuid()).isEqualTo(API_ID);
        assertThat(invoice.getItems().get(2).getHits()).isEqualTo(20000L);
        assertThat(invoice.getItems().get(2).getChargedAmount().doubleValue()).isEqualTo(300.00);
        assertThat(invoice.getItems().get(2).getEndpoint()).isEqualTo(items.get(2).getEndpoint());
        assertThat(invoice.getItems().get(2).getInvoice()).isEqualTo(invoice);
        assertThat(invoice.getItems().get(2).getCreatedAt()).isNotNull();

        assertThat(invoice.getItems().get(3).getApiUuid()).isEqualTo(API_ID);
        assertThat(invoice.getItems().get(3).getHits()).isEqualTo(152618L);
        assertThat(invoice.getItems().get(3).getChargedAmount().doubleValue()).isEqualTo(0.00);
        assertThat(invoice.getItems().get(3).getEndpoint()).isEqualTo(items.get(3).getEndpoint());
        assertThat(invoice.getItems().get(3).getInvoice()).isEqualTo(invoice);
        assertThat(invoice.getItems().get(3).getCreatedAt()).isNotNull();

        final ApiUsage apiUsage = new ApiUsage(0, API_ID);
        final ApiEndpointUsage apiEndpointUsageTest = new ApiEndpointUsage(14548, "/test");
        apiUsage.add(apiEndpointUsageTest);

        final ApiEndpointUsage apiEndpointUsageTest2 = new ApiEndpointUsage(20000, "/test2");
        apiUsage.add(apiEndpointUsageTest2);

        apiUsage.add(new ApiEndpointUsage(152618, "/freeEndpoint"));

        final List<InvoiceRequestSplit> invoiceRequestSplits = defaultApiUsage.splitApiUsage(subscription, userApiUsage, invoice, items, apiUsage);

        assertThat(invoice.getSplit()).hasSize(3);
        assertThat(invoiceRequestSplits).hasSize(3);

        assertThat(invoiceRequestSplits.get(0).getApiUuid()).isEqualTo(API_ID);
        assertThat(invoiceRequestSplits.get(0).getPlanUuid()).isEqualTo(PLAN_ID);
        assertThat(invoiceRequestSplits.get(0).getProviderUuid()).isEqualTo(PROVIDER_ID);
        assertThat(invoiceRequestSplits.get(0).getTenantValue()).isNull();
        assertThat(invoiceRequestSplits.get(0).getProviderValue().toString()).isEqualTo("75.000000");

        assertThat(invoiceRequestSplits.get(1).getApiUuid()).isEqualTo(API_ID);
        assertThat(invoiceRequestSplits.get(1).getPlanUuid()).isEqualTo(PLAN_ID);
        assertThat(invoiceRequestSplits.get(1).getProviderUuid()).isEqualTo(PROVIDER_ID);
        assertThat(invoiceRequestSplits.get(1).getTenantValue()).isNull();
        assertThat(invoiceRequestSplits.get(1).getProviderValue().toString()).isEqualTo("150.000000");

        assertThat(invoiceRequestSplits.get(2).getApiUuid()).isEqualTo(API_ID);
        assertThat(invoiceRequestSplits.get(2).getPlanUuid()).isEqualTo(PLAN_ID);
        assertThat(invoiceRequestSplits.get(2).getProviderUuid()).isEqualTo(PROVIDER_ID);
        assertThat(invoiceRequestSplits.get(2).getTenantValue()).isEqualTo(BigDecimal.valueOf(225).multiply(BigDecimal.valueOf(0.255500)).setScale(2, BigDecimal.ROUND_HALF_UP));
        assertThat(invoiceRequestSplits.get(2).getProviderValue()).isNull();
    }

    private Map<String, String> buildMetadata(long hits, long hitValue) {
        final Map<String, String> metadata = new HashMap<>();
        metadata.put("hits", String.valueOf(hits));
        metadata.put("hit_value", String.valueOf(hitValue));
        return metadata;
    }

}