package io.gr1d.ic.usage.strategy.usage;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import io.gr1d.ic.usage.api.subscriptions.model.ApiGatewayTenantResponse;
import io.gr1d.ic.usage.strategy.ApiEndpointUsage;
import io.gr1d.ic.usage.strategy.ApiUsage;

public interface ApiUsageStrategy {

    Collection<ApiUsage> getUsageForUser(String tenantRealm, String user, ZonedDateTime from, ZonedDateTime to);

    ApiUsage getTotalMetricsByApi(String tenant, String gatewayId, String apiId, ZonedDateTime from, ZonedDateTime to);

    default Map<String, ApiUsage> getTotalMetrics(final Iterable<ApiGatewayTenantResponse> apiGatewayResponses, final ZonedDateTime from, final ZonedDateTime to) {

        final Map<String, ApiUsage> apiUsages = new HashMap<>();

        apiGatewayResponses.forEach(apiGatewayResponse -> {
            final ApiUsage metrics = this.getTotalMetricsByApi(apiGatewayResponse.getTenant().getRealm(), apiGatewayResponse.getApiGateway().getGateway().getExternalId().toLowerCase(), apiGatewayResponse.getApiGateway().getProductionExternalId(), from, to);

            if (metrics != null) {
                if (apiUsages.containsKey(metrics.getApiId())) {

                    final ApiUsage apiUsage = apiUsages.get(metrics.getApiId());
                    metrics.getEndpoints().forEach(apiEndpointUsage -> {
                        Optional<ApiEndpointUsage> endpointUsage = apiUsage.getEndpoints().stream().filter(aeu -> aeu.getUri().equals(apiEndpointUsage.getUri())).findFirst();

                        if (endpointUsage.isPresent()) {
                            apiEndpointUsage.setHits(apiEndpointUsage.getHits() + endpointUsage.get().getHits());
                        }
                    });
                    apiUsage.setHits(apiUsage.getHits() + metrics.getHits());
                } else {
                    apiUsages.put(metrics.getApiId(), metrics);
                }
            }
        });

        return apiUsages;
    }
}
