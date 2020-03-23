package io.gr1d.ic.usage.response;

import io.gr1d.ic.usage.model.ExecutionUser;
import io.gr1d.ic.usage.model.audit.Invoice;
import lombok.Getter;

import static java.util.Optional.ofNullable;

@Getter
public class ExecutionUserResponse {

    private final String referenceMonth;
    private final String executionUuid;
    private final String status;
    private final String tenantRealm;
    private final String keycloakId;
    private final String errorReason;
    private final String invoiceId;

    public ExecutionUserResponse(final ExecutionUser execution) {
        referenceMonth = execution.getExecution().getReferenceMonth().toString();
        executionUuid = execution.getExecution().getUuid();
        status = execution.getStatus().getName();
        tenantRealm = execution.getTenantRealm();
        keycloakId = execution.getKeycloakId();
        errorReason = execution.getErrorReason();
        invoiceId = ofNullable(execution.getInvoice()).map(Invoice::getInvoiceId).orElse(null);
    }
}
