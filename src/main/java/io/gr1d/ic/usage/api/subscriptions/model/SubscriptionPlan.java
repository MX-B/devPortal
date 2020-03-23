package io.gr1d.ic.usage.api.subscriptions.model;

import lombok.Value;

import java.util.Map;

@Value
public class SubscriptionPlan {
	private String uuid;
	private String modality;
	private String name;
	private String description;
	private Map<String, String> metadata;
}
