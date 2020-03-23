package io.gr1d.ic.usage.api.bridge.model;

import lombok.Data;

import java.util.List;

@Data
public class BridgeUserApi {

    private String uuid;
    private String name;
    private String gateway;
    private List<BridgeUserApiEndpoint> endpoints;

}
