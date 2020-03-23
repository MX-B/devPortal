package io.gr1d.ic.usage.api.subscriptions.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProviderResponse {

    private String uuid;
    private String name;
    private String phone;
    private String email;
    private String walletId;

    private LocalDateTime createdAt;
    private LocalDateTime removedAt;

}
