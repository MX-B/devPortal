package io.gr1d.ic.usage.strategy.usage;

import static java.util.Collections.emptyList;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import feign.FeignException;
import io.gr1d.core.util.Markers;
import io.gr1d.ic.usage.api.bridge.BridgeApi;
import io.gr1d.ic.usage.api.bridge.model.BridgeUser;
import io.gr1d.ic.usage.api.bridge.model.BridgeUserApi;
import io.gr1d.ic.usage.strategy.ApiEndpointUsage;
import io.gr1d.ic.usage.strategy.ApiUsage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("ApiUsageStrategy.Bridge")
public class BridgeApiUsageStrategy implements ApiUsageStrategy {

    private final BridgeApi api;

    @Autowired
    public BridgeApiUsageStrategy(final BridgeApi api) {
        this.api = api;
    }

    @Override
    public Collection<ApiUsage> getUsageForUser(final String tenantRealm, final String user, final ZonedDateTime from, final ZonedDateTime to) {
        log.debug("Looking for API usage of user {} on Bridge", user);

        final String start = from.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "T00:00:00.000-03:00";
        final String end = to.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "T23:59:59.999-03:00";
        final Map<String, ApiUsage> usagePerApi = new HashMap<>();
        final BridgeUser bridgeUser = api.getMetrics(tenantRealm, user, start, end);

        if (bridgeUser != null && bridgeUser.getApplications() != null) {
            bridgeUser.getApplications().forEach(bridgeUserApplication -> bridgeUserApplication.getApis().forEach(bridgeUserApi -> {
                if (!usagePerApi.containsKey(bridgeUserApi.getUuid())) {
                    usagePerApi.put(bridgeUserApi.getUuid(), new ApiUsage(tenantRealm, bridgeUserApi.getUuid(), from, to, bridgeUserApi.getGateway()));
                }
                bridgeUserApi.getEndpoints().forEach(bridgeUserApiEndpoint ->
                        usagePerApi.get(bridgeUserApi.getUuid()).add(new ApiEndpointUsage(new Long(bridgeUserApiEndpoint.getTotal()), bridgeUserApiEndpoint.getUri())));
            }));
            return usagePerApi.values();
        }
        return emptyList();
    }

    @Override
    public ApiUsage getTotalMetricsByApi(final String tenant, final String gatewayId, final String apiId, final ZonedDateTime from, final ZonedDateTime to) {
        log.debug("Looking for API usage {} on Bridge", apiId);

        final String start = from.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "T00:00:00.000-03:00";
        final String end = to.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "T23:59:59.999-03:00";

        try {
            final BridgeUserApi metricsByApi = api.getTotalMetricsByApi(tenant, gatewayId, apiId, start, end);

            if (metricsByApi != null) {
                final ApiUsage apiUsage = new ApiUsage(null, metricsByApi.getUuid(), from, to, null);
                metricsByApi.getEndpoints().forEach(bridgeUserApiEndpoint -> apiUsage.add(new ApiEndpointUsage(new Long(bridgeUserApiEndpoint.getTotal()), bridgeUserApiEndpoint.getUri())));
                return apiUsage;
            } else {
                return null;
            }
        } catch (FeignException e) {
            log.error(Markers.NOTIFY_ADMIN, "Metrics not found for apiId {}, gateway: {}", apiId, gatewayId);
            throw e;
        }
    }

}
