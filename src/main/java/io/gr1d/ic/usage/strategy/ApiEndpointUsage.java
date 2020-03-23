package io.gr1d.ic.usage.strategy;

import lombok.Data;

@Data
public class ApiEndpointUsage {
	private String uri;
	private long hits;

	public ApiEndpointUsage(final long hits, final String uri) {
		this.hits = hits;
		this.uri = uri;
	}

	public ApiEndpointUsage add(final long hits) {
		this.hits += hits;
		return this;
	}
}
