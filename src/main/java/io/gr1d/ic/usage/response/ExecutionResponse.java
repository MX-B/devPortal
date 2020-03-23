package io.gr1d.ic.usage.response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.gr1d.ic.usage.model.Execution;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
public class ExecutionResponse {

    private static final Gson gson = new GsonBuilder().create();

    private final String referenceMonth;
    private final LocalDateTime startedAt;
    private final LocalDateTime finishedAt;
    private final String status;
    private final String startedBy;
    private final String startTrigger;
    private final String description;
    private final String errorReason;
    private final Map parameters;

    public ExecutionResponse(final Execution execution) {
        referenceMonth = execution.getReferenceMonth().toString();
        startedAt = execution.getStartedAt();
        finishedAt = execution.getFinishedAt();
        status = execution.getStatus().getName();
        startedBy = execution.getStartedBy();
        startTrigger = execution.getStartTrigger();
        description = execution.getDescription();
        errorReason = execution.getErrorReason();
        parameters = gson.fromJson(execution.getParameters(), Map.class);
    }
}
