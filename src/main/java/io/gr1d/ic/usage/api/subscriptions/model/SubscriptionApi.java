package io.gr1d.ic.usage.api.subscriptions.model;

import lombok.Value;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 * How an user is subscribed to an API, and data about that should be billed
 * 
 * @author Rafael M. Lins
 * @author Sérgio Filho
 *
 */
@Value
public class SubscriptionApi {
	private String uuid;
	private LocalDateTime createdAt;
	private String name;
	private int percent;
	private SubscriptionProvider provider;
	private Collection<SubscriptionPlan> plans;
}
