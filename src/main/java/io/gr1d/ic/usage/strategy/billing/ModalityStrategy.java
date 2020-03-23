package io.gr1d.ic.usage.strategy.billing;

import io.gr1d.ic.usage.api.billing.model.InvoiceRequestItem;
import io.gr1d.ic.usage.api.billing.model.InvoiceRequestSplit;
import io.gr1d.ic.usage.api.subscriptions.model.PlanEndpointResponse;
import io.gr1d.ic.usage.api.subscriptions.model.SubscriptionResponse;
import io.gr1d.ic.usage.model.audit.Invoice;
import io.gr1d.ic.usage.strategy.ApiEndpointUsage;
import io.gr1d.ic.usage.strategy.ApiUsage;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

@Slf4j
public abstract class ModalityStrategy {

    private static final String I18N_NOT_CHARGED = "gr1d.billing.itemDescription.notCharged";
    static final Locale DEFAULT_LOCALE = new Locale("pt", "BR");

    public abstract List<InvoiceRequestItem> billApiUsage(SubscriptionResponse sub, ApiUsage apiUsage, Invoice invoice);

    public abstract List<InvoiceRequestSplit> splitApiUsage(SubscriptionResponse sub, ApiUsage userUsage,
                                                            Invoice invoice, List<InvoiceRequestItem> items, ApiUsage apiUsage);

    @FunctionalInterface
    interface EndpointProcessor {
        void process(final PlanEndpointResponse subscription, final ApiEndpointUsage usage);
    }

    void iterateEndpoints(final SubscriptionResponse sub, final ApiUsage apiUsage,
                                    final List<InvoiceRequestItem> items, final Invoice invoice,
                                    final EndpointProcessor processor) {
        apiUsage.getEndpoints().forEach(apiEndpointUsage -> {
            final Optional<PlanEndpointResponse> response = sub.getPlan().getPlanEndpoints().stream()
                    .filter(planEndpoint -> planEndpoint.getEndpoint().equals(apiEndpointUsage.getUri()))
                    .findFirst();

            final String endpoint = apiEndpointUsage.getUri();

            if (response.isPresent()) {
                processor.process(response.get(), apiEndpointUsage);
            } else {
                log.info("Endpoint {}, not mapped, skipping", endpoint);
                final String description = translate(I18N_NOT_CHARGED, new Object[] { sub.getApi().getName(), endpoint,
                        apiEndpointUsage.getHits() });

                final InvoiceRequestItem item = new InvoiceRequestItem();
                item.setDescription(description);
                item.setItemId(apiUsage.getApiId());
                item.setQuantity(apiEndpointUsage.getHits());
                item.setEndpoint(endpoint);
                item.setUnitValue(BigDecimal.ZERO);
                item.setApiUuid(sub.getApi().getApiUuid());
                item.setHits(apiEndpointUsage.getHits());
                item.setPlanUuid(sub.getPlan().getUuid());
                item.setProviderUuid(sub.getApi().getProvider().getUuid());
                items.add(item);

                invoice.createItem(sub, apiEndpointUsage.getHits(), item.getUnitValue(), apiEndpointUsage.getUri());
            }
        });
    }

    void tenantSplit(final SubscriptionResponse sub, final Invoice invoice, final List<InvoiceRequestItem> items, final List<InvoiceRequestSplit> splits) {
        final BigDecimal totalValue = items.stream()
                .map(InvoiceRequestItem::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        final BigDecimal providerSplitValue = splits.stream()
                .filter(split -> split.getProviderValue() != null)
                .map(InvoiceRequestSplit::getProviderValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        final BigDecimal remainderValue = totalValue.subtract(providerSplitValue);

        if (remainderValue.compareTo(BigDecimal.ZERO) > 0) {
            final InvoiceRequestSplit splitTenant = new InvoiceRequestSplit();
            splitTenant.setApiUuid(sub.getApi().getApiUuid());
            splitTenant.setPlanUuid(sub.getApi().getApiPlan().getUuid());
            splitTenant.setProviderUuid(sub.getApi().getProvider().getUuid());
            splitTenant.setTenantValue(remainderValue.multiply(sub.getPercentageTenantSplit()
                    .divide(BigDecimal.valueOf(100)))
                    .setScale(2, RoundingMode.HALF_EVEN));

            splits.add(splitTenant);
            invoice.addSplit(splitTenant);
        }
    }

    String translate(final String messageKey, final Object[] arguments) {
        try {
            final ResourceBundle messages = ResourceBundle.getBundle("messages", DEFAULT_LOCALE);
            final String text = messages.getString(messageKey);
            return new MessageFormat(text, DEFAULT_LOCALE).format(arguments);
        } catch (final Exception e) {
            return messageKey;
        }
    }
}
