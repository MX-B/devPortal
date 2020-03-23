package io.gr1d.ic.usage.api.subscriptions.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.Data;

@Data
public class ApiGatewayTenantResponse {

    private String uuid;
    private TenantResponse tenant;
    private ApiGatewayResponse apiGateway;
    private List<PlanResponse> plans;
    private String type;
    private BigDecimal percentageSplit;
    private Set<String> paymentMethods;

    private LocalDateTime createdAt;
    private LocalDateTime removedAt;
    
}
