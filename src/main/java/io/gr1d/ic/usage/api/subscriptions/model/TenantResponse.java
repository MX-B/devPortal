package io.gr1d.ic.usage.api.subscriptions.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TenantResponse {

    private String uuid;
    private String realm;
    private String name;
    private String phone;
    private String logo;
    private String walletId;

    private LocalDateTime createdAt;
    private LocalDateTime removedAt;


}
