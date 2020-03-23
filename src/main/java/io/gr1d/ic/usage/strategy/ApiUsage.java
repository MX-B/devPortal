package io.gr1d.ic.usage.strategy;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
public class ApiUsage {
    private String tenantRealm;
    private String apiId;
    private String gateway;
    private long hits;
    private ZonedDateTime periodStart;
    private ZonedDateTime periodEnd;
    private List<ApiEndpointUsage> endpoints = new ArrayList<>();

    public ApiUsage(final long hits, final String apiId) {
        this.hits = hits;
        this.apiId = apiId;
    }

    public ApiUsage(final String tenantRealm, final String apiId, final ZonedDateTime periodStart, final ZonedDateTime periodEnd, final String gateway) {
        this.tenantRealm = tenantRealm;
        this.apiId = apiId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.gateway = gateway;
    }

    public ApiUsage add(final ApiEndpointUsage endpoint) {

        final AtomicBoolean exists = new AtomicBoolean(false);

        endpoints.stream().filter(apiEndpointUsage -> apiEndpointUsage.getUri().equals(endpoint.getUri())).forEach(apiEndpointUsage -> {
            apiEndpointUsage.add(endpoint.getHits());
            exists.set(true);
        });

        if (!exists.get()) {
            endpoints.add(endpoint);
        }

        this.hits += endpoint.getHits();
        return this;
    }

    public ApiUsage add(final long hits) {
        this.hits += hits;
        return this;
    }
}
