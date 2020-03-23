package io.gr1d.ic.usage.api.subscriptions.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * How an user is subscribed to an API, and data about that should be billed
 *
 * @author Rafael M. Lins
 * @author Sérgio Filho
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {
	private String uuid;
	private String apiUuid;
	private String apiName;
	private String userId;
	private String modality;
	private String name;
	private String description;
}
