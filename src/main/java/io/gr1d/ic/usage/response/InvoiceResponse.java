package io.gr1d.ic.usage.response;

import io.gr1d.ic.usage.model.audit.Invoice;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class InvoiceResponse {

    private final String keycloakId;
    private final String invoiceId;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final LocalDateTime createdAt;
    private final BigDecimal value;

    public InvoiceResponse(final Invoice invoice) {
        keycloakId = invoice.getKeycloakId();
        invoiceId = invoice.getInvoiceId();
        periodStart = invoice.getPeriodStart();
        periodEnd = invoice.getPeriodEnd();
        createdAt = invoice.getCreatedAt();
        value = invoice.getValue();
    }
}
