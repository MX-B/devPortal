package io.gr1d.ic.usage.api.subscriptions.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApiGatewayResponse {

    private String uuid;
    private String apiUuid;
    private PlanResponse apiPlan;
    private String splitMode;
    private String name;
    private String externalId;
    private String productionExternalId;
    private GatewayResponse gateway;
    private ProviderResponse provider;

    private LocalDateTime createdAt;
    private LocalDateTime removedAt;

}
