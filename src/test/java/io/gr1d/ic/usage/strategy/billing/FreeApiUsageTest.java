package io.gr1d.ic.usage.strategy.billing;

import br.com.six2six.fixturefactory.loader.FixtureFactoryLoader;
import io.gr1d.ic.usage.api.billing.model.InvoiceRequestItem;
import io.gr1d.ic.usage.api.billing.model.InvoiceRequestSplit;
import io.gr1d.ic.usage.api.subscriptions.model.*;
import io.gr1d.ic.usage.model.audit.Invoice;
import io.gr1d.ic.usage.strategy.ApiEndpointUsage;
import io.gr1d.ic.usage.strategy.ApiUsage;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FreeApiUsageTest {

    private static final String USER_ID = "25f35df7-af7c-4bdb-880e-7d5d503d5ec7";
    private static final String API_ID = "b6a3b331-249b-4a25-80ee-d7d62c9b880c";
    private static final String PLAN_ID = "e16d1399-ce86-40c0-8a1f-5d3a5f2251e2";
    private static final String PROVIDER_ID = "a6a3b331-ce86-40c0-8a1f-5d3a5f28274";
    private static final String API_NAME = "Payments";
    private static final String PLAN_NAME = "FREE";
    private static final String PLAN_DESC = "Payments - Description";
    private static final String MODALITY = "FREE";
    private static final String GATEWAY_UUID = "23fhj890f7hg8f2g78";

    private FreeApiUsage freeApiUsage;

    @Before
    public void setUp() {
        freeApiUsage = new FreeApiUsage();
        FixtureFactoryLoader.loadTemplates("io.gr1d.devportal.usage.fixtures");
    }

    @Test
    public void testUnlimitExecution() {
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
        apiGatewayResponse.setGateway(gatewayResponse);
        apiGatewayResponse.setProvider(providerResponse);
        apiGatewayResponse.setSplitMode("AUTO");

        final PlanResponse planResponse = new PlanResponse();

        apiGatewayResponse.setApiPlan(planResponse);
        subscription.setPlan(planResponse);
        planResponse.setUuid(PLAN_ID);
        planResponse.setDescription(PLAN_DESC);
        planResponse.setName(PLAN_NAME);
        planResponse.setModality(MODALITY);

        final TenantResponse tenantResponse = new TenantResponse();
        subscription.setTenant(tenantResponse);
        tenantResponse.setName("TENANT");
        tenantResponse.setRealm("tenant");

        final PlanEndpointResponse planEndpointResponseTest = new PlanEndpointResponse();

        planResponse.getPlanEndpoints().add(planEndpointResponseTest);
        planEndpointResponseTest.setEndpoint("/test");
        planEndpointResponseTest.setMetadata(Collections.singletonMap("limit", "10000"));

        final ApiUsage userApiUsage = new ApiUsage(0L, API_ID);
        final ApiEndpointUsage userApiEndpointUsageTest = new ApiEndpointUsage(5000, "/test");
        userApiUsage.add(userApiEndpointUsageTest);

        final List<InvoiceRequestItem> items = freeApiUsage.billApiUsage(subscription, userApiUsage, invoice);

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getItemId()).isEqualTo(API_ID);
        assertThat(items.get(0).getQuantity()).isEqualTo(1L);
        assertThat(items.get(0).getUnitValue()).isEqualTo(BigDecimal.ZERO);
        assertThat(items.get(0).getDescription()).isEqualTo("Payments API - Endpoint /test - Assinatura do Plano FREE - 10.000 de 5.000 hits utilizados");

        assertThat(invoice.getItems()).hasSize(1);
        assertThat(invoice.getItems().get(0).getApiUuid()).isEqualTo(API_ID);
        assertThat(invoice.getItems().get(0).getChargedAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(invoice.getItems().get(0).getHits()).isEqualTo(5000L);
        assertThat(invoice.getItems().get(0).getEndpoint()).isEqualTo(planResponse.getPlanEndpoints().get(0).getEndpoint());
        assertThat(invoice.getItems().get(0).getInvoice()).isEqualTo(invoice);
        assertThat(invoice.getItems().get(0).getCreatedAt()).isNotNull();

        final ApiUsage apiUsage = new ApiUsage(0L, API_ID);
        final ApiEndpointUsage apiEndpointUsageTest = new ApiEndpointUsage(5000, "/test");
        apiUsage.add(apiEndpointUsageTest);

        final List<InvoiceRequestSplit> invoiceRequestSplits = freeApiUsage.splitApiUsage(subscription, userApiUsage, invoice, items, apiUsage);

        assertThat(invoiceRequestSplits).hasSize(0);
        assertThat(invoice.getSplit()).hasSize(0);

    }

}