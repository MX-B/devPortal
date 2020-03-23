package io.gr1d.ic.usage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import io.gr1d.ic.usage.api.bridge.model.User;
import io.gr1d.ic.usage.model.Execution;
import io.gr1d.ic.usage.model.ExecutionStatus;
import io.gr1d.ic.usage.model.ExecutionUser;
import io.gr1d.ic.usage.model.audit.Invoice;
import io.gr1d.ic.usage.repository.ExecutionUserRepository;

@Service
public class ExecutionUserService {

    private final ExecutionUserRepository repository;

    @Autowired
    public ExecutionUserService(final ExecutionUserRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createSuccessExecution(final Execution execution, final User user, final Invoice invoice) {
        final ExecutionUser executionUser = new ExecutionUser();
        executionUser.setExecution(execution);
        executionUser.setTenantRealm(user.getRealm());
        executionUser.setKeycloakId(user.getUserId());
        executionUser.setInvoice(invoice);
        executionUser.setStatus(ExecutionStatus.SUCCESS);

        repository.save(executionUser);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createErrorExecution(final Execution execution, final User user, final String errorReason) {
        final ExecutionUser executionUser = new ExecutionUser();
        executionUser.setExecution(execution);
        executionUser.setTenantRealm(user.getRealm());
        executionUser.setKeycloakId(user.getUserId());
        executionUser.setErrorReason(errorReason);
        executionUser.setStatus(ExecutionStatus.ERROR);

        repository.save(executionUser);
    }

    boolean wasAlreadyProcessedForMonth(final Execution execution, final User user) {
        return repository.countSuccessExecutionForUser(execution.getReferenceMonth(), user.getRealm(), user.getUserId()) > 0;
    }

}
