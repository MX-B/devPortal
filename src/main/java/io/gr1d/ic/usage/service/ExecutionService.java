package io.gr1d.ic.usage.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.gr1d.ic.usage.model.Execution;
import io.gr1d.ic.usage.model.ExecutionStatus;
import io.gr1d.ic.usage.repository.ExecutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static java.util.Optional.ofNullable;

@Service
public class ExecutionService {

    private final ObjectMapper mapper;
    private final ExecutionRepository repository;

    @Autowired
    public ExecutionService(final ObjectMapper mapper, final ExecutionRepository repository) {
        this.mapper = mapper;
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Execution startExecution(final LocalDate referenceMonth, final LocalDate chargeDate,
                                    final String startedBy, final String startTrigger, final String description) {

        final Execution execution = new Execution();
        execution.setReferenceMonth(Integer.parseInt(referenceMonth.format(DateTimeFormatter.ofPattern("yyyyMM"))));
        execution.setStartedAt(LocalDateTime.now());
        execution.setStartedBy(ofNullable(startedBy).orElse("system"));
        execution.setStartTrigger(startTrigger);
        execution.setStatus(ExecutionStatus.STARTED);
        execution.setDescription(description);
        execution.setParameters(paramsMap(chargeDate));

        return repository.save(execution);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finishExecutionSuccess(final Execution execution) {
        execution.setStatus(ExecutionStatus.SUCCESS);
        execution.setFinishedAt(LocalDateTime.now());
        repository.save(execution);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finishExecutionError(final Execution execution, final String errorReason) {
        execution.setStatus(ExecutionStatus.ERROR);
        execution.setErrorReason(errorReason);
        execution.setFinishedAt(LocalDateTime.now());
        repository.save(execution);
    }

    private String paramsMap(final LocalDate chargeDate) {
        final Map<String, Object> params = new HashMap<>(6);
        params.put("charge_date", chargeDate);

        try {
            return mapper.writeValueAsString(params);
        } catch (final Exception e) {
            return params.toString();
        }
    }
}
