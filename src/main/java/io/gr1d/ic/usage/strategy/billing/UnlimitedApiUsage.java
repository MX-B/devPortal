package io.gr1d.ic.usage.strategy.billing;

import io.gr1d.ic.usage.api.billing.model.InvoiceRequestItem;
import io.gr1d.ic.usage.api.billing.model.InvoiceRequestSplit;
import io.gr1d.ic.usage.api.subscriptions.model.PlanEndpointResponse;
import io.gr1d.ic.usage.api.subscriptions.model.PlanResponse;
import io.gr1d.ic.usage.api.subscriptions.model.SubscriptionResponse;
import io.gr1d.ic.usage.model.audit.Invoice;
import io.gr1d.ic.usage.strategy.ApiEndpointUsage;
import io.gr1d.ic.usage.strategy.ApiUsage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static java.math.RoundingMode.HALF_DOWN;

@Component("ModalityStrategy.UNLIMITED")
public class UnlimitedApiUsage extends ModalityStrategy {

    private static final String I18N = "gr1d.billing.itemDescription.unlimited";
    private static final String I18N_MIN_VALUE = "gr1d.billing.itemDescription.unlimitedRange.minValue";
    private static final BigDecimal MICRO = new BigDecimal(1000000);
    private static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

    @Override
    public List<InvoiceRequestItem> billApiUsage(final SubscriptionResponse sub, final ApiUsage apiUsage, final Invoice invoice) {
        final List<InvoiceRequestItem> items = new ArrayList<>(apiUsage.getEndpoints().size());
        final AtomicReference<BigDecimal> amountValue = new AtomicReference<>(new BigDecimal(0).setScale(6));

        iterateEndpoints(sub, apiUsage, items, invoice, (planEndpointResponse, apiEndpointUsage) -> {
            final InvoiceRequestItem item = new InvoiceRequestItem();

            item.setDescription(description(sub, planEndpointResponse, apiEndpointUsage));
            item.setItemId(apiUsage.getApiId());
            item.setQuantity(apiEndpointUsage.getHits());
            item.setUnitValue(getHitValue(planEndpointResponse));
            item.setVirtualGood(true);
            item.setEndpoint(planEndpointResponse.getEndpoint());
            item.setApiUuid(sub.getApi().getApiUuid());
            item.setHits(apiEndpointUsage.getHits());
            item.setPlanUuid(sub.getPlan().getUuid());
            item.setProviderUuid(sub.getApi().getProvider().getUuid());

            items.add(item);
            invoice.createItem(sub, apiEndpointUsage.getHits(), item.getUnitValue()
                    .multiply(BigDecimal.valueOf(item.getQuantity())), planEndpointResponse.getEndpoint());
            amountValue.set(amountValue.get().add(item.getUnitValue().multiply(BigDecimal.valueOf(item.getQuantity()))));
        });

        final BigDecimal minValue = sub.getPlan().getValue() != null ? BigDecimal.valueOf(sub.getPlan().getValue()).divide(ONE_HUNDRED, HALF_DOWN).setScale(6) : BigDecimal.ZERO;

        if (amountValue.get().compareTo(minValue) < 0) {
            final InvoiceRequestItem item = new InvoiceRequestItem();

            item.setItemId(apiUsage.getApiId());
            item.setQuantity(1L);
            item.setUnitValue(minValue.subtract(amountValue.get()).setScale(6));
            item.setDescription(descriptionMinValue(sub, minValue));
            item.setHits(apiUsage.getHits());
            item.setApiUuid(sub.getApi().getApiUuid());
            item.setPlanUuid(sub.getPlan().getUuid());
            item.setProviderUuid(sub.getApi().getProvider().getUuid());

            items.add(item);
            invoice.createItem(sub, apiUsage.getHits(), item.getUnitValue(), null);
        }

        return items;
    }

    @Override
    public List<InvoiceRequestSplit> splitApiUsage(final SubscriptionResponse sub, final ApiUsage userUsage,
                                                   final Invoice invoice, final List<InvoiceRequestItem> items, final ApiUsage apiUsage) {

        final List<InvoiceRequestSplit> splits = new ArrayList<>(userUsage.getEndpoints().size());
        final PlanResponse apiPlan = sub.getApi().getApiPlan();
        final AtomicReference<BigDecimal> amountValue = new AtomicReference<>(new BigDecimal(0).setScale(6));

        userUsage.getEndpoints().forEach(apiEndpointUsage -> {
            final Optional<PlanEndpointResponse> responseApiPlan = apiPlan.getPlanEndpoints().stream()
                    .filter(planEndpoint -> planEndpoint.getEndpoint().equals(apiEndpointUsage.getUri()))
                    .findFirst();

            final Optional<ApiEndpointUsage> apiUsageEndpoint = apiUsage.getEndpoints().stream()
                    .filter(apiEndpointUsage1 -> apiEndpointUsage1.getUri().equals(apiEndpointUsage.getUri())).findFirst();

            if (responseApiPlan.isPresent()) {
                final InvoiceRequestSplit split = new InvoiceRequestSplit();
                split.setApiUuid(sub.getApi().getApiUuid());
                split.setPlanUuid(apiPlan.getUuid());
                split.setProviderUuid(sub.getApi().getProvider().getUuid());
                split.setProviderValue(getHitValue(responseApiPlan.get())
                        .multiply(BigDecimal.valueOf(apiEndpointUsage.getHits()))
                        .setScale(2, RoundingMode.HALF_EVEN));

                splits.add(split);
                invoice.addSplit(split);

                amountValue.set(amountValue.get().add(getHitValue(responseApiPlan.get())
                        .multiply(BigDecimal.valueOf(apiUsageEndpoint.get().getHits()))
                        .setScale(2, RoundingMode.HALF_EVEN)));
            }

        });

        final BigDecimal minValue = apiPlan.getValue() != null
                ? BigDecimal.valueOf(apiPlan.getValue()).divide(ONE_HUNDRED, HALF_DOWN).setScale(6) : BigDecimal.ZERO;

        if (amountValue.get().compareTo(minValue) < 0) {

            final BigDecimal toMinValue = minValue.subtract(amountValue.get()).setScale(6);
            final BigDecimal hitValue = toMinValue.divide(BigDecimal.valueOf(apiUsage.getHits()), HALF_DOWN).setScale(6);
            final long userHits = userUsage.getHits();

            final InvoiceRequestSplit split = new InvoiceRequestSplit();
            split.setApiUuid(sub.getApi().getApiUuid());
            split.setPlanUuid(apiPlan.getUuid());
            split.setProviderUuid(sub.getApi().getProvider().getUuid());
            split.setProviderValue(hitValue.multiply(BigDecimal.valueOf(userHits)));

            splits.add(split);
            invoice.addSplit(split);
        }

        tenantSplit(sub, invoice, items, splits);

        return splits;
    }

    private BigDecimal getHitValue(final PlanEndpointResponse sub) {
        return new BigDecimal(sub.getMetadata().get("hit_value")).divide(MICRO, MathContext.DECIMAL32).setScale(6);
    }

    private String description(final SubscriptionResponse sub, final PlanEndpointResponse planEndpointResponse, final ApiEndpointUsage apiUsage) {
        return translate(I18N, new Object[]{sub.getApi().getName(), planEndpointResponse.getEndpoint(), apiUsage.getHits()});
    }

    private String descriptionMinValue(final SubscriptionResponse subscription, final BigDecimal minValue) {
        return translate(I18N_MIN_VALUE, new Object[]{subscription.getApi().getName(), NumberFormat.getCurrencyInstance(DEFAULT_LOCALE).format(minValue)});
    }

}
