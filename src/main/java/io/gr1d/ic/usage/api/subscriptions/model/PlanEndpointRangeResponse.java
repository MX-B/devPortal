package io.gr1d.ic.usage.api.subscriptions.model;

import lombok.Data;

@Data
public class PlanEndpointRangeResponse {

    private Long initRange;
    private Long finalRange;
    private Long value;

}
