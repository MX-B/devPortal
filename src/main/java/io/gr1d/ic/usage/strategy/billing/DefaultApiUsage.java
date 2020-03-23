package io.gr1d.ic.usage.strategy.billing;

import io.gr1d.ic.usage.api.billing.model.InvoiceRequestItem;
import io.gr1d.ic.usage.api.billing.model.InvoiceRequestSplit;
import io.gr1d.ic.usage.api.subscriptions.model.PlanEndpointResponse;
import io.gr1d.ic.usage.api.subscriptions.model.SubscriptionResponse;
import io.gr1d.ic.usage.model.audit.Invoice;
import io.gr1d.ic.usage.strategy.ApiEndpointUsage;
import io.gr1d.ic.usage.strategy.ApiUsage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.math.RoundingMode.HALF_DOWN;

@Component("ModalityStrategy.DEFAULT")
public class DefaultApiUsage extends ModalityStrategy {

    private static final String I18N_BASE_VALUE = "gr1d.billing.itemDescription.baseValue";
    private static final String I18N_EXTRA_USAGE = "gr1d.billing.itemDescription.extraUsage";
    private static final BigDecimal MICRO = new BigDecimal(1000000);
    private static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

    @Override
    public List<InvoiceRequestItem> billApiUsage(final SubscriptionResponse sub, final ApiUsage apiUsage, final Invoice invoice) {
        final List<InvoiceRequestItem> items = new ArrayList<>(apiUsage.getEndpoints().size() * 2);
        final InvoiceRequestItem baseValue = new InvoiceRequestItem();

        baseValue.setDescription(descriptionBaseValue(sub, apiUsage));
        baseValue.setItemId(apiUsage.getApiId());
        baseValue.setQuantity(1);
        baseValue.setUnitValue(BigDecimal.valueOf(sub.getPlan().getValue()).divide(ONE_HUNDRED, HALF_DOWN).setScale(6));
        baseValue.setApiUuid(sub.getApi().getApiUuid());
        baseValue.setHits(apiUsage.getHits());
        baseValue.setPlanUuid(sub.getPlan().getUuid());
        baseValue.setProviderUuid(sub.getApi().getProvider().getUuid());

        items.add(baseValue);
        invoice.createItem(sub, apiUsage.getHits(), baseValue.getUnitValue(), null);

        iterateEndpoints(sub, apiUsage, items, invoice, (planEndpointResponse, apiEndpointUsage) -> {
            final long baseHits = getHits(planEndpointResponse);
            final long extraUsage = baseHits > 0 ? apiEndpointUsage.getHits() - baseHits : 0;
            final String endpoint = planEndpointResponse.getEndpoint();

            if (extraUsage > 0) {
                final InvoiceRequestItem item = new InvoiceRequestItem();
                item.setDescription(descriptionExtraUsage(planEndpointResponse, sub, baseHits, apiEndpointUsage.getHits(), extraUsage));
                item.setItemId(apiUsage.getApiId());
                item.setQuantity(extraUsage);
                item.setEndpoint(endpoint);
                item.setUnitValue(getHitValue(planEndpointResponse));
                item.setApiUuid(sub.getApi().getApiUuid());
                item.setHits(apiEndpointUsage.getHits());
                item.setPlanUuid(sub.getPlan().getUuid());
                item.setProviderUuid(sub.getApi().getProvider().getUuid());
                items.add(item);

                invoice.createItem(sub, apiEndpointUsage.getHits(), item.getUnitValue()
                        .multiply(BigDecimal.valueOf(item.getQuantity())), endpoint);
            } else {
                final InvoiceRequestItem item = new InvoiceRequestItem();
                item.setDescription(descriptionExtraUsage(planEndpointResponse, sub, baseHits, apiEndpointUsage.getHits(), extraUsage < 0 ? 0 : extraUsage));
                item.setItemId(apiUsage.getApiId());
                item.setQuantity(apiEndpointUsage.getHits());
                item.setEndpoint(endpoint);
                item.setUnitValue(BigDecimal.ZERO);
                item.setApiUuid(sub.getApi().getApiUuid());
                item.setHits(apiEndpointUsage.getHits());
                item.setPlanUuid(sub.getPlan().getUuid());
                item.setProviderUuid(sub.getApi().getProvider().getUuid());
                items.add(item);

                invoice.createItem(sub, apiEndpointUsage.getHits(), item.getUnitValue(), endpoint);
            }
        });

        return items;
    }

    @Override
    public List<InvoiceRequestSplit> splitApiUsage(final SubscriptionResponse sub, final ApiUsage userUsage, final Invoice invoice,
                                                   final List<InvoiceRequestItem> items, final ApiUsage apiUsage) {

        final List<InvoiceRequestSplit> splits = new ArrayList<>(userUsage.getEndpoints().size());
        final InvoiceRequestSplit baseSplit = new InvoiceRequestSplit();

        baseSplit.setApiUuid(sub.getApi().getApiUuid());
        baseSplit.setPlanUuid(sub.getApi().getApiPlan().getUuid());
        baseSplit.setProviderUuid(sub.getApi().getProvider().getUuid());
        baseSplit.setProviderValue(BigDecimal.valueOf(sub.getApi().getApiPlan().getValue()).divide(ONE_HUNDRED, HALF_DOWN).setScale(6));

        splits.add(baseSplit);
        invoice.addSplit(baseSplit);

        userUsage.getEndpoints().forEach(apiEndpointUsage -> {
            final Optional<PlanEndpointResponse> responseApiPlan = sub.getApi().getApiPlan().getPlanEndpoints().stream()
                    .filter(planEndpoint -> planEndpoint.getEndpoint().equals(apiEndpointUsage.getUri()))
                    .findFirst();

            final Optional<ApiEndpointUsage> apiUsageEndpoint = apiUsage.getEndpoints().stream()
                    .filter(apiEndpointUsage1 -> apiEndpointUsage1.getUri().equals(apiEndpointUsage.getUri())).findFirst();

            if (responseApiPlan.isPresent() && apiUsageEndpoint.isPresent()) {

                final long totalHits = apiUsageEndpoint.get().getHits();
                final long baseHits = getHits(responseApiPlan.get());
                final long extraUsage = baseHits > 0 ? totalHits - baseHits : 0;

                if (extraUsage > 0) {

                    final BigDecimal totalValueExtra = getHitValue(responseApiPlan.get()).multiply(BigDecimal.valueOf(extraUsage));
                    final BigDecimal hitValue = totalValueExtra.divide(BigDecimal.valueOf(totalHits), HALF_DOWN).setScale(6);
                    final long userHits = apiEndpointUsage.getHits();

                    final InvoiceRequestSplit split = new InvoiceRequestSplit();
                    split.setApiUuid(sub.getApi().getApiUuid());
                    split.setPlanUuid(sub.getPlan().getUuid());
                    split.setProviderUuid(sub.getApi().getProvider().getUuid());
                    split.setProviderValue(hitValue.multiply(BigDecimal.valueOf(userHits)));

                    splits.add(split);
                    invoice.addSplit(split);
                }
            }

        });

        tenantSplit(sub, invoice, items, splits);

        return splits;
    }

    private BigDecimal getHitValue(final PlanEndpointResponse sub) {
        return new BigDecimal(sub.getMetadata().get("hit_value")).divide(MICRO, MathContext.DECIMAL32).setScale(6);
    }

    private long getHits(final PlanEndpointResponse sub) {
        return Long.parseLong(sub.getMetadata().get("hits"));
    }


    private String descriptionBaseValue(final SubscriptionResponse subscription, final ApiUsage apiUsage) {
        return translate(I18N_BASE_VALUE, new Object[]{subscription.getApi().getName(), subscription.getPlan().getName(), apiUsage.getHits()});
    }

    private String descriptionExtraUsage(final PlanEndpointResponse planEndpointResponse, final SubscriptionResponse subscription, final long baseHits, final long hits, final long extraUsage) {
        return translate(I18N_EXTRA_USAGE, new Object[]{subscription.getApi().getName(), planEndpointResponse.getEndpoint(), baseHits, hits, extraUsage});
    }

}
