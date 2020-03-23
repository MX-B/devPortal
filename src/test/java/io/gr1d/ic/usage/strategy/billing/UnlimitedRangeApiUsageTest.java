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
import java.util.Arrays;
import java.util.List;

import static java.math.RoundingMode.HALF_DOWN;
import static org.assertj.core.api.Assertions.assertThat;

public class UnlimitedRangeApiUsageTest {

    private static final String USER_ID = "25f35df7-af7c-4bdb-880e-7d5d503d5ec7";
    private static final String API_ID = "b6a3b331-249b-4a25-80ee-d7d62c9b880c";
    private static final String PLAN_ID = "e16d1399-ce86-40c0-8a1f-5d3a5f2251e2";
    private static final String PROVIDER_ID = "a6a3b331-ce86-40c0-8a1f-5d3a5f28274";
    private static final String API_NAME = "Payments";
    private static final String PLAN_NAME = "INFINITY";
    private static final String PLAN_DESC = "Payments - Description";
    private static final String MODALITY = "UNLIMITED_RANGE";
    private static final String GATEWAY_UUID = "f23h98vh23978f32gh87f293";

    private UnlimitedRangeApiUsage unlimitedRangeApiUsage;

    @Before
    public void setUp() {
        unlimitedRangeApiUsage = new UnlimitedRangeApiUsage();
        FixtureFactoryLoader.loadTemplates("io.gr1d.devportal.usage.fixtures");
    }

    @Test
    public void testExecution() {
        final Invoice invoice = new Invoice();
        final SubscriptionResponse subscription = new SubscriptionResponse();
        final ApiGatewayResponse apiGatewayResponse = new ApiGatewayResponse();
        final GatewayResponse gatewayResponse = new GatewayResponse();
        final ProviderResponse providerResponse = new ProviderResponse();

        providerResponse.setUuid(PROVIDER_ID);
        providerResponse.setName("PROVIDER");

        gatewayResponse.setUuid(GATEWAY_UUID);
        subscription.setUserId(USER_ID);
        subscription.setApi(apiGatewayResponse);
        subscription.setPercentageTenantSplit(BigDecimal.valueOf(25.55));
        apiGatewayResponse.setApiUuid(API_ID);
        apiGatewayResponse.setName(API_NAME);
        apiGatewayResponse.setGateway(gatewayResponse);
        apiGatewayResponse.setProvider(providerResponse);

        final PlanResponse apiPlanResponse = new PlanResponse();

        apiPlanResponse.setUuid(PLAN_ID);
        apiPlanResponse.setDescription(PLAN_DESC);
        apiPlanResponse.setName(PLAN_NAME);
        apiPlanResponse.setModality(MODALITY);
        apiPlanResponse.setValue(100000L);
        apiGatewayResponse.setApiPlan(apiPlanResponse);

        final PlanEndpointResponse planApiEndpointResponseTest = new PlanEndpointResponse();

        apiPlanResponse.getPlanEndpoints().add(planApiEndpointResponseTest);
        planApiEndpointResponseTest.setEndpoint("/test");

        final PlanEndpointRangeResponse apiRangeResponse1 = new PlanEndpointRangeResponse();
        apiRangeResponse1.setInitRange(0L);
        apiRangeResponse1.setFinalRange(7500L);
        apiRangeResponse1.setValue(50000L);
        final PlanEndpointRangeResponse apiRangeResponse2 = new PlanEndpointRangeResponse();
        apiRangeResponse2.setInitRange(7501L);
        apiRangeResponse2.setValue(45000L);

        planApiEndpointResponseTest.setRanges(Arrays.asList(apiRangeResponse1, apiRangeResponse2));

        final PlanEndpointResponse planApiEndpointResponseTest2 = new PlanEndpointResponse();

        apiPlanResponse.getPlanEndpoints().add(planApiEndpointResponseTest2);
        planApiEndpointResponseTest2.setEndpoint("/test2");

        planApiEndpointResponseTest2.setRanges(Arrays.asList(apiRangeResponse1, apiRangeResponse2));

        final PlanResponse planResponse = new PlanResponse();

        subscription.setPlan(planResponse);
        planResponse.setUuid(PLAN_ID);
        planResponse.setDescription(PLAN_DESC);
        planResponse.setName(PLAN_NAME);
        planResponse.setModality(MODALITY);
        planResponse.setValue(200000L);

        final TenantResponse tenantResponse = new TenantResponse();
        subscription.setTenant(tenantResponse);
        tenantResponse.setName("TENANT");
        tenantResponse.setRealm("tenant");

        final PlanEndpointResponse planEndpointResponseTest = new PlanEndpointResponse();

        planResponse.getPlanEndpoints().add(planEndpointResponseTest);
        planEndpointResponseTest.setEndpoint("/test");

        final PlanEndpointRangeResponse rangeResponse1 = new PlanEndpointRangeResponse();
        rangeResponse1.setInitRange(0L);
        rangeResponse1.setFinalRange(15000L);
        rangeResponse1.setValue(100000L);
        final PlanEndpointRangeResponse rangeResponse2 = new PlanEndpointRangeResponse();
        rangeResponse2.setInitRange(15001L);
        rangeResponse2.setValue(90000L);

        planEndpointResponseTest.setRanges(Arrays.asList(rangeResponse1, rangeResponse2));

        final PlanEndpointResponse planEndpointResponseTest2 = new PlanEndpointResponse();
        planResponse.getPlanEndpoints().add(planEndpointResponseTest2);
        planEndpointResponseTest2.setEndpoint("/test2");

        planEndpointResponseTest2.setRanges(Arrays.asList(rangeResponse1, rangeResponse2));

        final ApiUsage userApiUsage = new ApiUsage(0, API_ID);
        final ApiEndpointUsage userApiEndpointUsageTest = new ApiEndpointUsage(14548, "/test");
        userApiUsage.add(userApiEndpointUsageTest);

        final ApiEndpointUsage userApiEndpointUsageTest2 = new ApiEndpointUsage(20000, "/test2");
        userApiUsage.add(userApiEndpointUsageTest2);

        userApiUsage.add(new ApiEndpointUsage(152618, "/freeEndpoint"));

        final List<InvoiceRequestItem> items = unlimitedRangeApiUsage.billApiUsage(subscription, userApiUsage, invoice);

        assertThat(items).hasSize(3);
        assertThat(items.get(0).getItemId()).isEqualTo(API_ID);
        assertThat(items.get(0).getQuantity()).isEqualTo(14548L);
        assertThat(items.get(0).getUnitValue().toString()).isEqualTo("0.100000");
        assertThat(items.get(0).getDescription()).isEqualTo("Payments API - Consumo do Endpoint /test - 14.548 hits utilizados");

        assertThat(items.get(1).getItemId()).isEqualTo(API_ID);
        assertThat(items.get(1).getQuantity()).isEqualTo(20000L);
        assertThat(items.get(1).getUnitValue().toString()).isEqualTo("0.090000");
        assertThat(items.get(1).getDescription()).isEqualTo("Payments API - Consumo do Endpoint /test2 - 20.000 hits utilizados");

        assertThat(items.get(2).getItemId()).isEqualTo(API_ID);
        assertThat(items.get(2).getQuantity()).isEqualTo(152618);
        assertThat(items.get(2).getUnitValue().toString()).isEqualTo("0");
        assertThat(items.get(2).getDescription()).isEqualTo("Payments API - Consumo do Endpoint /freeEndpoint - 152.618 hits utilizados");

        assertThat(invoice.getItems()).hasSize(3);
        assertThat(invoice.getItems().get(0).getApiUuid()).isEqualTo(API_ID);
        assertThat(invoice.getItems().get(0).getHits()).isEqualTo(14548L);
        assertThat(invoice.getItems().get(0).getChargedAmount().toString()).isEqualTo("1454.800000");
        assertThat(invoice.getItems().get(0).getInvoice()).isEqualTo(invoice);
        assertThat(invoice.getItems().get(0).getEndpoint()).isEqualTo(planResponse.getPlanEndpoints().get(0).getEndpoint());
        assertThat(invoice.getItems().get(0).getCreatedAt()).isNotNull();

        assertThat(invoice.getItems().get(1).getApiUuid()).isEqualTo(API_ID);
        assertThat(invoice.getItems().get(1).getHits()).isEqualTo(20000L);
        assertThat(invoice.getItems().get(1).getChargedAmount().toString()).isEqualTo("1800.000000");
        assertThat(invoice.getItems().get(1).getInvoice()).isEqualTo(invoice);
        assertThat(invoice.getItems().get(1).getEndpoint()).isEqualTo(planResponse.getPlanEndpoints().get(1).getEndpoint());
        assertThat(invoice.getItems().get(1).getCreatedAt()).isNotNull();

        assertThat(invoice.getItems().get(2).getApiUuid()).isEqualTo(API_ID);
        assertThat(invoice.getItems().get(2).getHits()).isEqualTo(152618L);
        assertThat(invoice.getItems().get(2).getChargedAmount().toString()).isEqualTo("0");
        assertThat(invoice.getItems().get(2).getEndpoint()).isEqualTo(items.get(2).getEndpoint());
        assertThat(invoice.getItems().get(2).getInvoice()).isEqualTo(invoice);
        assertThat(invoice.getItems().get(2).getCreatedAt()).isNotNull();


        final ApiUsage apiUsage = new ApiUsage(0, API_ID);
        final ApiEndpointUsage apiEndpointUsageTest = new ApiEndpointUsage(14548, "/test");
        apiUsage.add(apiEndpointUsageTest);

        final ApiEndpointUsage apiEndpointUsageTest2 = new ApiEndpointUsage(20000, "/test2");
        apiUsage.add(apiEndpointUsageTest2);

        apiUsage.add(new ApiEndpointUsage(152618, "/freeEndpoint"));

        final List<InvoiceRequestSplit> invoiceRequestSplits = unlimitedRangeApiUsage.splitApiUsage(subscription, userApiUsage, invoice, items, apiUsage);

        assertThat(invoice.getSplit()).hasSize(3);
        assertThat(invoiceRequestSplits).hasSize(3);

        assertThat(invoiceRequestSplits.get(0).getApiUuid()).isEqualTo(API_ID);
        assertThat(invoiceRequestSplits.get(0).getPlanUuid()).isEqualTo(PLAN_ID);
        assertThat(invoiceRequestSplits.get(0).getProviderUuid()).isEqualTo(PROVIDER_ID);
        assertThat(invoiceRequestSplits.get(0).getTenantValue()).isNull();
        assertThat(invoiceRequestSplits.get(0).getProviderValue().toString()).isEqualTo("654.66");

        assertThat(invoiceRequestSplits.get(1).getApiUuid()).isEqualTo(API_ID);
        assertThat(invoiceRequestSplits.get(1).getPlanUuid()).isEqualTo(PLAN_ID);
        assertThat(invoiceRequestSplits.get(1).getProviderUuid()).isEqualTo(PROVIDER_ID);
        assertThat(invoiceRequestSplits.get(1).getTenantValue()).isNull();
        assertThat(invoiceRequestSplits.get(1).getProviderValue().toString()).isEqualTo("900.00");

        assertThat(invoiceRequestSplits.get(2).getApiUuid()).isEqualTo(API_ID);
        assertThat(invoiceRequestSplits.get(2).getPlanUuid()).isEqualTo(PLAN_ID);
        assertThat(invoiceRequestSplits.get(2).getProviderUuid()).isEqualTo(PROVIDER_ID);
        assertThat(invoiceRequestSplits.get(2).getTenantValue()).isEqualTo(BigDecimal.valueOf(1700.14).multiply(BigDecimal.valueOf(0.255500)).setScale(2, BigDecimal.ROUND_HALF_UP));
        assertThat(invoiceRequestSplits.get(2).getProviderValue()).isNull();
    }

    @Test
    public void testExecutionMinValue() {
        final Invoice invoice = new Invoice();

        final SubscriptionResponse subscription = new SubscriptionResponse();
        final ApiGatewayResponse apiGatewayResponse = new ApiGatewayResponse();
        final GatewayResponse gatewayResponse = new GatewayResponse();
        final ProviderResponse providerResponse = new ProviderResponse();

        providerResponse.setUuid(PROVIDER_ID);
        providerResponse.setName("PROVIDER");

        gatewayResponse.setUuid(GATEWAY_UUID);
        subscription.setUserId(USER_ID);
        subscription.setApi(apiGatewayResponse);
        subscription.setPercentageTenantSplit(BigDecimal.valueOf(25.55));
        apiGatewayResponse.setApiUuid(API_ID);
        apiGatewayResponse.setName(API_NAME);
        apiGatewayResponse.setGateway(gatewayResponse);
        apiGatewayResponse.setProvider(providerResponse);

        final PlanResponse apiPlanResponse = new PlanResponse();

        apiPlanResponse.setUuid(PLAN_ID);
        apiPlanResponse.setDescription(PLAN_DESC);
        apiPlanResponse.setName(PLAN_NAME);
        apiPlanResponse.setModality(MODALITY);
        apiPlanResponse.setValue(200000L);
        apiGatewayResponse.setApiPlan(apiPlanResponse);

        final PlanEndpointResponse planApiEndpointResponseTest = new PlanEndpointResponse();

        apiPlanResponse.getPlanEndpoints().add(planApiEndpointResponseTest);
        planApiEndpointResponseTest.setEndpoint("/test");

        final PlanEndpointRangeResponse apiRangeResponse1 = new PlanEndpointRangeResponse();
        apiRangeResponse1.setInitRange(0L);
        apiRangeResponse1.setFinalRange(7500L);
        apiRangeResponse1.setValue(50000L);
        final PlanEndpointRangeResponse apiRangeResponse2 = new PlanEndpointRangeResponse();
        apiRangeResponse2.setInitRange(7501L);
        apiRangeResponse2.setValue(45000L);

        planApiEndpointResponseTest.setRanges(Arrays.asList(apiRangeResponse1, apiRangeResponse2));

        final PlanEndpointResponse planApiEndpointResponseTest2 = new PlanEndpointResponse();

        apiPlanResponse.getPlanEndpoints().add(planApiEndpointResponseTest2);
        planApiEndpointResponseTest2.setEndpoint("/test2");

        planApiEndpointResponseTest2.setRanges(Arrays.asList(apiRangeResponse1, apiRangeResponse2));

        final PlanResponse planResponse = new PlanResponse();

        subscription.setPlan(planResponse);
        planResponse.setUuid(PLAN_ID);
        planResponse.setDescription(PLAN_DESC);
        planResponse.setName(PLAN_NAME);
        planResponse.setModality(MODALITY);
        planResponse.setValue(400000L);

        final TenantResponse tenantResponse = new TenantResponse();
        subscription.setTenant(tenantResponse);
        tenantResponse.setName("TENANT");
        tenantResponse.setRealm("tenant");

        final PlanEndpointResponse planEndpointResponseTest = new PlanEndpointResponse();

        planResponse.getPlanEndpoints().add(planEndpointResponseTest);
        planEndpointResponseTest.setEndpoint("/test");

        final PlanEndpointRangeResponse rangeResponse1 = new PlanEndpointRangeResponse();
        rangeResponse1.setInitRange(0L);
        rangeResponse1.setFinalRange(15000L);
        rangeResponse1.setValue(100000L);
        final PlanEndpointRangeResponse rangeResponse2 = new PlanEndpointRangeResponse();
        rangeResponse2.setInitRange(15001L);
        rangeResponse2.setValue(90000L);

        planEndpointResponseTest.setRanges(Arrays.asList(rangeResponse1, rangeResponse2));

        final PlanEndpointResponse planEndpointResponseTest2 = new PlanEndpointResponse();
        planResponse.getPlanEndpoints().add(planEndpointResponseTest2);
        planEndpointResponseTest2.setEndpoint("/test2");

        planEndpointResponseTest2.setRanges(Arrays.asList(rangeResponse1, rangeResponse2));

        final ApiUsage userApiUsage = new ApiUsage(0, API_ID);
        final ApiEndpointUsage userApiEndpointUsageTest = new ApiEndpointUsage(14548, "/test");
        userApiUsage.add(userApiEndpointUsageTest);

        final ApiEndpointUsage userApiEndpointUsageTest2 = new ApiEndpointUsage(20000, "/test2");
        userApiUsage.add(userApiEndpointUsageTest2);

        final List<InvoiceRequestItem> items = unlimitedRangeApiUsage.billApiUsage(subscription, userApiUsage, invoice);

        final BigDecimal minValue = BigDecimal.valueOf(planResponse.getValue()).divide(new BigDecimal(100), HALF_DOWN);

        assertThat(items).hasSize(3);
        assertThat(items.get(0).getItemId()).isEqualTo(API_ID);
        assertThat(items.get(0).getQuantity()).isEqualTo(14548L);
        assertThat(items.get(0).getUnitValue().doubleValue()).isEqualTo(0.1);
        assertThat(items.get(0).getDescription()).isEqualTo("Payments API - Consumo do Endpoint /test - 14.548 hits utilizados");

        assertThat(items.get(1).getItemId()).isEqualTo(API_ID);
        assertThat(items.get(1).getQuantity()).isEqualTo(20000L);
        assertThat(items.get(1).getUnitValue().doubleValue()).isEqualTo(0.09);
        assertThat(items.get(1).getDescription()).isEqualTo("Payments API - Consumo do Endpoint /test2 - 20.000 hits utilizados");

        assertThat(items.get(2).getItemId()).isEqualTo(API_ID);
        assertThat(items.get(2).getQuantity()).isEqualTo(1L);
        assertThat(items.get(2).getUnitValue().doubleValue()).isEqualTo(745.20);
        assertThat(items.get(2).getDescription()).isEqualTo("Payments API - Complemento de valor mínimo do Plano (R$ 4.000,00) não atingido");

        assertThat(invoice.getItems()).hasSize(3);
        assertThat(invoice.getItems().get(0).getApiUuid()).isEqualTo(API_ID);
        assertThat(invoice.getItems().get(0).getHits()).isEqualTo(14548L);
        assertThat(invoice.getItems().get(0).getChargedAmount().doubleValue()).isEqualTo(1454.80);
        assertThat(invoice.getItems().get(0).getInvoice()).isEqualTo(invoice);
        assertThat(invoice.getItems().get(0).getEndpoint()).isEqualTo(planResponse.getPlanEndpoints().get(0).getEndpoint());
        assertThat(invoice.getItems().get(0).getCreatedAt()).isNotNull();

        assertThat(invoice.getItems().get(1).getApiUuid()).isEqualTo(API_ID);
        assertThat(invoice.getItems().get(1).getHits()).isEqualTo(20000L);
        assertThat(invoice.getItems().get(1).getChargedAmount().doubleValue()).isEqualTo(1800.00);
        assertThat(invoice.getItems().get(1).getInvoice()).isEqualTo(invoice);
        assertThat(invoice.getItems().get(1).getEndpoint()).isEqualTo(planResponse.getPlanEndpoints().get(1).getEndpoint());
        assertThat(invoice.getItems().get(1).getCreatedAt()).isNotNull();

        assertThat(invoice.getItems().get(2).getApiUuid()).isEqualTo(API_ID);
        assertThat(invoice.getItems().get(2).getHits()).isEqualTo(34548L);
        assertThat(invoice.getItems().get(2).getChargedAmount().doubleValue()).isEqualTo(745.2);
        assertThat(invoice.getItems().get(2).getInvoice()).isEqualTo(invoice);
        assertThat(invoice.getItems().get(2).getCreatedAt()).isNotNull();


        final ApiUsage apiUsage = new ApiUsage(0, API_ID);
        final ApiEndpointUsage apiEndpointUsageTest = new ApiEndpointUsage(14548, "/test");
        apiUsage.add(apiEndpointUsageTest);

        final ApiEndpointUsage apiEndpointUsageTest2 = new ApiEndpointUsage(20000, "/test2");
        apiUsage.add(apiEndpointUsageTest2);

        apiUsage.add(new ApiEndpointUsage(152618, "/freeEndpoint"));

        final List<InvoiceRequestSplit> invoiceRequestSplits = unlimitedRangeApiUsage.splitApiUsage(subscription, userApiUsage, invoice, items, apiUsage);

        assertThat(invoice.getSplit()).hasSize(4);
        assertThat(invoiceRequestSplits).hasSize(4);

        assertThat(invoiceRequestSplits.get(0).getApiUuid()).isEqualTo(API_ID);
        assertThat(invoiceRequestSplits.get(0).getPlanUuid()).isEqualTo(PLAN_ID);
        assertThat(invoiceRequestSplits.get(0).getProviderUuid()).isEqualTo(PROVIDER_ID);
        assertThat(invoiceRequestSplits.get(0).getTenantValue()).isNull();
        assertThat(invoiceRequestSplits.get(0).getProviderValue().toString()).isEqualTo("654.66");

        assertThat(invoiceRequestSplits.get(1).getApiUuid()).isEqualTo(API_ID);
        assertThat(invoiceRequestSplits.get(1).getPlanUuid()).isEqualTo(PLAN_ID);
        assertThat(invoiceRequestSplits.get(1).getProviderUuid()).isEqualTo(PROVIDER_ID);
        assertThat(invoiceRequestSplits.get(1).getTenantValue()).isNull();
        assertThat(invoiceRequestSplits.get(1).getProviderValue().toString()).isEqualTo("900.00");

        assertThat(invoiceRequestSplits.get(2).getApiUuid()).isEqualTo(API_ID);
        assertThat(invoiceRequestSplits.get(2).getPlanUuid()).isEqualTo(PLAN_ID);
        assertThat(invoiceRequestSplits.get(2).getProviderUuid()).isEqualTo(PROVIDER_ID);
        assertThat(invoiceRequestSplits.get(2).getTenantValue()).isNull();
        assertThat(invoiceRequestSplits.get(2).getProviderValue().toString()).isEqualTo("445.340000");

        assertThat(invoiceRequestSplits.get(3).getApiUuid()).isEqualTo(API_ID);
        assertThat(invoiceRequestSplits.get(3).getPlanUuid()).isEqualTo(PLAN_ID);
        assertThat(invoiceRequestSplits.get(3).getProviderUuid()).isEqualTo(PROVIDER_ID);
        assertThat(invoiceRequestSplits.get(3).getTenantValue()).isEqualTo(BigDecimal.valueOf(2000).multiply(BigDecimal.valueOf(0.255500)).setScale(2, BigDecimal.ROUND_HALF_UP));
        assertThat(invoiceRequestSplits.get(3).getProviderValue()).isNull();

    }

    @Test
    public void testExecutionMinValue2() {
        final Invoice invoice = new Invoice();

        final SubscriptionResponse subscription = new SubscriptionResponse();
        final ApiGatewayResponse apiGatewayResponse = new ApiGatewayResponse();
        final GatewayResponse gatewayResponse = new GatewayResponse();
        final ProviderResponse providerResponse = new ProviderResponse();

        providerResponse.setUuid(PROVIDER_ID);
        providerResponse.setName("PROVIDER");

        gatewayResponse.setUuid(GATEWAY_UUID);
        subscription.setUserId(USER_ID);
        subscription.setApi(apiGatewayResponse);
        subscription.setPercentageTenantSplit(BigDecimal.valueOf(25.55));
        apiGatewayResponse.setApiUuid(API_ID);
        apiGatewayResponse.setName(API_NAME);
        apiGatewayResponse.setGateway(gatewayResponse);
        apiGatewayResponse.setProvider(providerResponse);

        final PlanResponse apiPlanResponse = new PlanResponse();

        apiPlanResponse.setUuid(PLAN_ID);
        apiPlanResponse.setDescription(PLAN_DESC);
        apiPlanResponse.setName(PLAN_NAME);
        apiPlanResponse.setModality(MODALITY);
        apiPlanResponse.setValue(2500L);
        apiGatewayResponse.setApiPlan(apiPlanResponse);

        final PlanEndpointResponse planApiEndpointResponseTest = new PlanEndpointResponse();

        apiPlanResponse.getPlanEndpoints().add(planApiEndpointResponseTest);
        planApiEndpointResponseTest.setEndpoint("/offers/v1/byofferid");

        final PlanEndpointRangeResponse apiRangeResponse1 = new PlanEndpointRangeResponse();
        apiRangeResponse1.setInitRange(0L);
        apiRangeResponse1.setFinalRange(9L);
        apiRangeResponse1.setValue(100000L);
        final PlanEndpointRangeResponse apiRangeResponse2 = new PlanEndpointRangeResponse();
        apiRangeResponse2.setInitRange(10L);
        apiRangeResponse2.setFinalRange(19L);
        apiRangeResponse2.setValue(75000L);
        final PlanEndpointRangeResponse apiRangeResponse3 = new PlanEndpointRangeResponse();
        apiRangeResponse3.setInitRange(20L);
        apiRangeResponse3.setValue(50000L);

        planApiEndpointResponseTest.setRanges(Arrays.asList(apiRangeResponse1, apiRangeResponse2, apiRangeResponse3));

        final PlanEndpointResponse planApiEndpointResponseTest2 = new PlanEndpointResponse();

        apiPlanResponse.getPlanEndpoints().add(planApiEndpointResponseTest2);
        planApiEndpointResponseTest2.setEndpoint("/test2");

        planApiEndpointResponseTest2.setRanges(Arrays.asList(apiRangeResponse1, apiRangeResponse2));

        final PlanResponse planResponse = new PlanResponse();

        subscription.setPlan(planResponse);
        planResponse.setUuid(PLAN_ID);
        planResponse.setDescription(PLAN_DESC);
        planResponse.setName(PLAN_NAME);
        planResponse.setModality(MODALITY);
        planResponse.setValue(5000L);

        final TenantResponse tenantResponse = new TenantResponse();
        subscription.setTenant(tenantResponse);
        tenantResponse.setName("TENANT");
        tenantResponse.setRealm("tenant");

        final PlanEndpointResponse planEndpointResponseTest = new PlanEndpointResponse();

        planResponse.getPlanEndpoints().add(planEndpointResponseTest);
        planEndpointResponseTest.setEndpoint("/offers/v1/byofferid");

        final PlanEndpointRangeResponse rangeResponse1 = new PlanEndpointRangeResponse();
        rangeResponse1.setInitRange(0L);
        rangeResponse1.setFinalRange(9L);
        rangeResponse1.setValue(200000L);
        final PlanEndpointRangeResponse rangeResponse2 = new PlanEndpointRangeResponse();
        rangeResponse2.setInitRange(10L);
        rangeResponse2.setFinalRange(19L);
        rangeResponse2.setValue(150000L);
        final PlanEndpointRangeResponse rangeResponse3 = new PlanEndpointRangeResponse();
        rangeResponse3.setInitRange(20L);
        rangeResponse3.setValue(100000L);

        planEndpointResponseTest.setRanges(Arrays.asList(rangeResponse1, rangeResponse2, rangeResponse3));

        final ApiUsage userApiUsage = new ApiUsage(21, API_ID);
        final ApiEndpointUsage userApiEndpointUsageTest = new ApiEndpointUsage(21, "/offers/v1/byofferid");
        userApiUsage.add(userApiEndpointUsageTest);

        final List<InvoiceRequestItem> items = unlimitedRangeApiUsage.billApiUsage(subscription, userApiUsage, invoice);

        assertThat(items).hasSize(2);
        assertThat(items.get(0).getItemId()).isEqualTo(API_ID);
        assertThat(items.get(0).getQuantity()).isEqualTo(21L);
        assertThat(items.get(0).getUnitValue().doubleValue()).isEqualTo(0.1);
        assertThat(items.get(0).getDescription()).isEqualTo("Payments API - Consumo do Endpoint /offers/v1/byofferid - 21 hits utilizados");

        assertThat(items.get(1).getItemId()).isEqualTo(API_ID);
        assertThat(items.get(1).getQuantity()).isEqualTo(1L);
        assertThat(items.get(1).getUnitValue().toString()).isEqualTo("47.900000");
        assertThat(items.get(1).getDescription()).isEqualTo("Payments API - Complemento de valor mínimo do Plano (R$ 50,00) não atingido");

        assertThat(invoice.getItems()).hasSize(2);
        assertThat(invoice.getItems().get(0).getApiUuid()).isEqualTo(API_ID);
        assertThat(invoice.getItems().get(0).getHits()).isEqualTo(21L);
        assertThat(invoice.getItems().get(0).getChargedAmount().toString()).isEqualTo("2.100000");
        assertThat(invoice.getItems().get(0).getInvoice()).isEqualTo(invoice);
        assertThat(invoice.getItems().get(0).getEndpoint()).isEqualTo(planResponse.getPlanEndpoints().get(0).getEndpoint());
        assertThat(invoice.getItems().get(0).getCreatedAt()).isNotNull();

        assertThat(invoice.getItems().get(1).getApiUuid()).isEqualTo(API_ID);
        assertThat(invoice.getItems().get(1).getHits()).isEqualTo(42L);
        assertThat(invoice.getItems().get(1).getChargedAmount().toString()).isEqualTo("47.900000");
        assertThat(invoice.getItems().get(1).getInvoice()).isEqualTo(invoice);
        assertThat(invoice.getItems().get(1).getCreatedAt()).isNotNull();

        final ApiUsage apiUsage = new ApiUsage(0, API_ID);
        final ApiEndpointUsage apiEndpointUsageTest = new ApiEndpointUsage(21, "/offers/v1/byofferid");
        apiUsage.add(apiEndpointUsageTest);

        final ApiEndpointUsage apiEndpointUsageTest2 = new ApiEndpointUsage(20000, "/test2");
        apiUsage.add(apiEndpointUsageTest2);

        apiUsage.add(new ApiEndpointUsage(152618, "/freeEndpoint"));

        final List<InvoiceRequestSplit> invoiceRequestSplits = unlimitedRangeApiUsage.splitApiUsage(subscription, userApiUsage, invoice, items, apiUsage);

        assertThat(invoice.getSplit()).hasSize(3);
        assertThat(invoiceRequestSplits).hasSize(3);

        assertThat(invoiceRequestSplits.get(0).getApiUuid()).isEqualTo(API_ID);
        assertThat(invoiceRequestSplits.get(0).getPlanUuid()).isEqualTo(PLAN_ID);
        assertThat(invoiceRequestSplits.get(0).getProviderUuid()).isEqualTo(PROVIDER_ID);
        assertThat(invoiceRequestSplits.get(0).getTenantValue()).isNull();
        assertThat(invoiceRequestSplits.get(0).getProviderValue().toString()).isEqualTo("1.05");

        assertThat(invoiceRequestSplits.get(1).getApiUuid()).isEqualTo(API_ID);
        assertThat(invoiceRequestSplits.get(1).getPlanUuid()).isEqualTo(PLAN_ID);
        assertThat(invoiceRequestSplits.get(1).getProviderUuid()).isEqualTo(PROVIDER_ID);
        assertThat(invoiceRequestSplits.get(1).getTenantValue()).isNull();
        assertThat(invoiceRequestSplits.get(1).getProviderValue().toString()).isEqualTo("23.950000");

        assertThat(invoiceRequestSplits.get(2).getApiUuid()).isEqualTo(API_ID);
        assertThat(invoiceRequestSplits.get(2).getPlanUuid()).isEqualTo(PLAN_ID);
        assertThat(invoiceRequestSplits.get(2).getProviderUuid()).isEqualTo(PROVIDER_ID);
        assertThat(invoiceRequestSplits.get(2).getTenantValue()).isEqualTo(BigDecimal.valueOf(25).multiply(BigDecimal.valueOf(0.255500)).setScale(2, BigDecimal.ROUND_HALF_UP));
        assertThat(invoiceRequestSplits.get(2).getProviderValue()).isNull();
    }

}