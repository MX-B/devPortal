package io.gr1d.ic.usage.api.bridge.model;

import lombok.Data;

import java.util.List;

@Data
public class BridgeUserApplication {

    private String uuid;
    private String name;
    private List<BridgeUserApi> apis;

}
