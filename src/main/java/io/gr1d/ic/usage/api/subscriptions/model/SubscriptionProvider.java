package io.gr1d.ic.usage.api.subscriptions.model;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class SubscriptionProvider {
	private String name;
	private String uuid;
	private String walletId;
	private LocalDateTime createdAt;
}
