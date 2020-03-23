package io.gr1d.ic.usage.api.subscriptions.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SubscriptionResponse {

    private String uuid;
    private ApiGatewayResponse api;
    private String userId;
    private PlanResponse plan;
    private TenantResponse tenant;
    private BigDecimal percentageTenantSplit;

    private LocalDate subscriptionStart;
    private LocalDate subscriptionEnd;

}
