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
import java.util.ArrayList;
import java.util.List;

@Component("ModalityStrategy.FREE")
public class FreeApiUsage extends ModalityStrategy {

    private static final String I18N_UNLIMIT = "gr1d.billing.itemDescription.unlimit";
    private static final String I18N_LIMIT = "gr1d.billing.itemDescription.limit";

    @Override
    public List<InvoiceRequestItem> billApiUsage(SubscriptionResponse sub, ApiUsage apiUsage, Invoice invoice) {
        final List<InvoiceRequestItem> items = new ArrayList<>(apiUsage.getEndpoints().size());

        iterateEndpoints(sub, apiUsage, items, invoice, (planEndpointResponse, apiEndpointUsage) -> {
            final InvoiceRequestItem item = new InvoiceRequestItem();
            final String endpoint = planEndpointResponse.getEndpoint();

            item.setDescription(description(sub, planEndpointResponse, apiEndpointUsage));
            item.setItemId(apiUsage.getApiId());
            item.setQuantity(1);
            item.setUnitValue(BigDecimal.ZERO);
            item.setVirtualGood(true);
            item.setEndpoint(planEndpointResponse.getEndpoint());
            item.setApiUuid(sub.getApi().getApiUuid());
            item.setHits(apiEndpointUsage.getHits());
            item.setPlanUuid(sub.getPlan().getUuid());
            item.setProviderUuid(sub.getApi().getProvider().getUuid());

            items.add(item);
            invoice.createItem(sub, apiUsage.getHits(), item.getUnitValue(), endpoint);
        });

        return items;
    }

    @Override
    public List<InvoiceRequestSplit> splitApiUsage(final SubscriptionResponse sub, final ApiUsage userUsage,
                                                   final Invoice invoice, final List<InvoiceRequestItem> items, final ApiUsage apiUsage) {

        final List<InvoiceRequestSplit> splits = new ArrayList<>(1);
        tenantSplit(sub, invoice, items, splits);
        return splits;
    }

    private long getLimit(final PlanEndpointResponse planEndpointResponse) {
        return Long.parseLong(planEndpointResponse.getMetadata().get("limit"));
    }

    private String description(final SubscriptionResponse sub, final PlanEndpointResponse planEndpointResponse, final ApiEndpointUsage apiUsage) {
        long limit = getLimit(planEndpointResponse);
        if (limit > 0) {
            return translate(I18N_LIMIT, new Object[] { sub.getApi().getName(), apiUsage.getUri(), sub.getPlan().getName(), limit, apiUsage.getHits() });
        } else {
            return translate(I18N_UNLIMIT, new Object[] { sub.getApi().getName(), apiUsage.getUri(), sub.getPlan().getName(), apiUsage.getHits() });
        }
    }

}
