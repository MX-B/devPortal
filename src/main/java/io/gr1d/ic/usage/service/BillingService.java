package io.gr1d.ic.usage.service;

import io.gr1d.ic.usage.api.billing.BillingApi;
import io.gr1d.ic.usage.api.billing.model.InvoiceRequest;
import io.gr1d.ic.usage.api.billing.model.InvoiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BillingService {

    private final BillingApi billingApi;

    @Autowired
    public BillingService(final BillingApi billingApi) {
        this.billingApi = billingApi;
    }

    InvoiceResponse createInvoice(final InvoiceRequest request) {
        return billingApi.create(request);
    }

}
