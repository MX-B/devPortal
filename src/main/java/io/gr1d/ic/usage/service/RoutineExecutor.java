package io.gr1d.ic.usage.service;

import static org.springframework.util.CollectionUtils.isEmpty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.gr1d.core.util.Markers;
import io.gr1d.ic.usage.api.billing.model.InvoiceRequest;
import io.gr1d.ic.usage.api.billing.model.InvoiceRequestItem;
import io.gr1d.ic.usage.api.billing.model.InvoiceRequestSplit;
import io.gr1d.ic.usage.api.billing.model.InvoiceResponse;
import io.gr1d.ic.usage.api.bridge.model.User;
import io.gr1d.ic.usage.api.subscriptions.model.ApiGatewayTenantResponse;
import io.gr1d.ic.usage.api.subscriptions.model.SubscriptionResponse;
import io.gr1d.ic.usage.api.subscriptions.model.TenantResponse;
import io.gr1d.ic.usage.exception.ChargeRoutineException;
import io.gr1d.ic.usage.model.Execution;
import io.gr1d.ic.usage.model.audit.Invoice;
import io.gr1d.ic.usage.strategy.ApiUsage;
import io.gr1d.ic.usage.strategy.billing.ModalityStrategy;
import io.gr1d.ic.usage.strategy.billing.ModalityStrategyResolver;
import io.gr1d.ic.usage.strategy.usage.ApiUsageStrategy;
import io.gr1d.ic.usage.util.DateTimes;
import io.gr1d.ic.usage.util.ExceptionUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RoutineExecutor {

    private final UserService userService;
    private final BillingService billingService;
    private final InvoiceService invoiceService;
    private final SubscriptionService subscriptionService;
    private final ExecutionService executionService;
    private final ExecutionUserService executionUserService;
    private final ApiUsageStrategy apiUsageStrategy;
    private final ModalityStrategyResolver strategyResolver;

    @Autowired
    public RoutineExecutor(final UserService userService,
                           final BillingService billingService,
                           final InvoiceService invoiceService,
                           final SubscriptionService subscriptionService,
                           final ExecutionService executionService,
                           final ExecutionUserService executionUserService,
                           final ApiUsageStrategy apiUsageStrategy,
                           final ModalityStrategyResolver strategyResolver) {
        this.userService = userService;
        this.billingService = billingService;
        this.invoiceService = invoiceService;
        this.subscriptionService = subscriptionService;
        this.executionService = executionService;
        this.executionUserService = executionUserService;
        this.apiUsageStrategy = apiUsageStrategy;
        this.strategyResolver = strategyResolver;
    }

    @AllArgsConstructor
    private static class ExecutionContext {
        final Execution execution;
        final LocalDate referenceMonth;
        final LocalDate chargeDate;

        final ZonedDateTime startTs;
        final ZonedDateTime finishTs;
    }

    public Execution execute(final LocalDate referenceMonth, final LocalDate chargeDate,
                             final String startedBy, final String startTrigger, final String description) {

        final Execution execution = executionService.startExecution(referenceMonth, chargeDate, startedBy, startTrigger, description);

        try {
            final ZonedDateTime startTs = DateTimes.startOfMonth(referenceMonth);
            final ZonedDateTime finishTs = DateTimes.endOfMonth(referenceMonth);
            final ExecutionContext context = new ExecutionContext(execution, referenceMonth, chargeDate, startTs, finishTs);
            final Iterable<TenantResponse> tenants = subscriptionService.findAllTenants();

            final Iterable<ApiGatewayTenantResponse> apiGateways = subscriptionService.findAllApiGatewayTenant();

            final Map<String, ApiUsage> totalMetricsByApi = apiUsageStrategy.getTotalMetrics(apiGateways, context.startTs, context.finishTs);

            tenants.forEach(tenant -> executeForTenant(context, tenant, totalMetricsByApi));

            executionService.finishExecutionSuccess(execution);
        } catch (Exception e) {
            executionService.finishExecutionError(execution, ExceptionUtils.toString(e));
        }

        return execution;
    }

    private void executeForTenant(final ExecutionContext context, final TenantResponse tenant, final Map<String, ApiUsage> totalMetricsByApi) {
        log.info("Starting execution for tenant: {}", tenant.getRealm());

        final List<User> users = userService.findAllUsers(tenant.getRealm());
        users.forEach(user -> executeForUser(context, user, totalMetricsByApi));

        log.info("Finishing execution for tenant: {}", tenant.getRealm());
    }

    private void executeForUser(final ExecutionContext context, final User user, final Map<String, ApiUsage> totalMetricsByApi) {
        log.info("Checking if user ({}/{}) was already processed this month {}",
                user.getRealm(), user.getUserId(),
                context.execution.getReferenceMonth());

        final boolean alreadyProcessedForMonth = executionUserService.wasAlreadyProcessedForMonth(context.execution, user);
        if (alreadyProcessedForMonth) {
            log.info("User already processed for this reference month, skipping...");
            return;
        }

        try {
            log.info("Starting execution for user: {}/{}", user.getRealm(), user.getUserId());
            final Collection<ApiUsage> userUsage = apiUsageStrategy.getUsageForUser(user.getRealm(), user.getUserId(),
                    context.startTs, context.finishTs);
            final Invoice invoice = generateInvoiceForUser(context, user, userUsage, totalMetricsByApi);
            executionUserService.createSuccessExecution(context.execution, user, invoice);
            log.info("Finishing execution for user: {}/{}", user.getRealm(), user.getUserId());
        } catch (final Exception e) {
            executionUserService.createErrorExecution(context.execution, user, ExceptionUtils.toString(e));
            log.error("Error while trying processing user {}/{}", user.getRealm(), user.getUserId(), e);
        }
    }

    private Invoice generateInvoiceForUser(final ExecutionContext context, final User user, final Collection<ApiUsage> usageList, final Map<String, ApiUsage> totalMetricsByApi) {
        log.info("Generating invoice for user {}/{}", user.getRealm(), user.getUserId());
        if (!isEmpty(usageList)) {
            final InvoiceRequest invoiceRequest = new InvoiceRequest();
            invoiceRequest.setUserId(user.getUserId());
            invoiceRequest.setTenantRealm(user.getRealm());
            invoiceRequest.setChargeDate(context.chargeDate);
            invoiceRequest.setExpirationDate(context.chargeDate);
            invoiceRequest.setPeriodStart(context.startTs.toLocalDate());
            invoiceRequest.setPeriodEnd(context.finishTs.toLocalDate());
            invoiceRequest.setItems(new LinkedList<>());

            final Invoice invoice = new Invoice();
            invoice.setCreatedAt(LocalDateTime.now());
            invoice.setKeycloakId(user.getUserId());
            invoice.setTenantRealm(user.getRealm());
            invoice.setPeriodStart(context.startTs.toLocalDate());
            invoice.setPeriodEnd(context.finishTs.toLocalDate());

            for (final ApiUsage usage : usageList) {
                final SubscriptionResponse sub = subscriptionService.getUserApiSubscription(user.getRealm(),
                        usage.getGateway(), usage.getApiId(), user.getUserId(), context.referenceMonth);

                if (sub == null && totalMetricsByApi.containsKey(usage.getApiId())) {
                    log.error(Markers.NOTIFY_ADMIN, "Subscription not found for user {}/{} and gateway {}, apiId: {}",
                            user.getRealm(), user.getUserId(), usage.getGateway(), usage.getApiId());
                } else {
                    final List<InvoiceRequestItem> items = billApiUsage(invoiceRequest, invoice, usage, sub);
                    splitApiUsage(items, invoiceRequest, invoice, usage, sub, totalMetricsByApi.get(usage.getApiId()));
                }
            }

            log.info("Creating invoice: {}", invoice);

            final InvoiceResponse invoiceResponse = billingService.createInvoice(invoiceRequest);
            invoice.setInvoiceId(invoiceResponse.getUuid());
            invoice.setValue(invoiceResponse.getValue());

            invoiceService.save(invoice);
            log.info("Invoice created with uuid: {}", invoiceResponse.getUuid());

            return invoice;
        } else {
            log.info("No invoice generated because user didn't have any metrics");
            return null;
        }
    }

    private List<InvoiceRequestItem> billApiUsage(final InvoiceRequest invoiceRequest, final Invoice invoice,
                                                  final ApiUsage usage, final SubscriptionResponse sub) {
        final ModalityStrategy billingStrategy = strategyResolver.resolve(sub.getPlan().getModality());
        if (billingStrategy == null) {
            log.error(Markers.NOTIFY_ADMIN, String.format("Unknown API modality: %s", sub.getPlan().getModality()));
            throw new ChargeRoutineException(String.format("Unknown API modality: %s - sub=%s",
                    sub.getPlan().getModality(), sub.toString()));
        }

        final List<InvoiceRequestItem> newItems = billingStrategy.billApiUsage(sub, usage, invoice);
        invoiceRequest.getItems().addAll(newItems);

        return newItems;
    }

    private void splitApiUsage(final List<InvoiceRequestItem> items, final InvoiceRequest invoiceRequest,
                               final Invoice invoice, final ApiUsage userUsage, final SubscriptionResponse sub, final ApiUsage apiUsage) {

        if ("AUTO".equals(sub.getApi().getSplitMode()) && sub.getApi().getApiPlan() != null) {
            final ModalityStrategy splitStrategy = strategyResolver.resolve(sub.getApi().getApiPlan().getModality());
            if (splitStrategy == null) {
                log.error(String.format("Unknown API modality: %s", sub.getApi().getApiPlan().getModality()));
                throw new ChargeRoutineException(String.format("Unknown API modality: %s - sub=%s",
                        sub.getApi().getApiPlan().getModality(), sub.toString()));
            }

            final Collection<InvoiceRequestSplit> newSplitItems = splitStrategy.splitApiUsage(sub, userUsage, invoice, items, apiUsage);
            invoiceRequest.getSplit().addAll(newSplitItems);
        }
    }

}
