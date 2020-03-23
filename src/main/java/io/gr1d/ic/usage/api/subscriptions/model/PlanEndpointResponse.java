package io.gr1d.ic.usage.api.subscriptions.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class PlanEndpointResponse {

    private String uuid;
    private String externalId;
    private String endpoint;
    private Map<String, String> metadata;
    private List<PlanEndpointRangeResponse> ranges = new ArrayList<>();

}
