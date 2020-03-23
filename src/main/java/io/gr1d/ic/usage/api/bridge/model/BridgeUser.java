package io.gr1d.ic.usage.api.bridge.model;

import lombok.Data;

import java.util.List;

@Data
public class BridgeUser {

    private String uuid;
    private List<BridgeUserApplication> applications;

}
