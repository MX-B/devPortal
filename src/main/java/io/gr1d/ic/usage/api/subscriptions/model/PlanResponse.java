package io.gr1d.ic.usage.api.subscriptions.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PlanResponse {

    private String uuid;
    private String modality;
    private String name;
    private String description;
    private Long value;
    private List<PlanEndpointResponse> planEndpoints = new ArrayList<>();


}
