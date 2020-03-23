package io.gr1d.ic.usage.api.bridge;

import io.gr1d.ic.usage.api.HealthcheckApi;
import io.gr1d.ic.usage.api.bridge.model.BridgeUser;
import io.gr1d.ic.usage.api.bridge.model.BridgeUserApi;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * @author Raúl Sola
 */
@FeignClient(name = "bridgeApi", url = "${gr1d.service.bridge}")
public interface BridgeApi extends HealthcheckApi {

    @GetMapping("/metrics/user/{userId}?from={from}&to={to}")
    BridgeUser getMetrics(@RequestHeader("X-Tenant") String tenantRealm,
                          @PathVariable("userId") String userId,
                          @PathVariable("from") String from,
                          @PathVariable("to") String to);

    @GetMapping("/metrics/gateway/{gatewayId}/api/{apiId}/endpoints?from={from}&to={to}")
    BridgeUserApi getTotalMetricsByApi(@RequestHeader("X-Tenant") String tenantRealm,
                                       @PathVariable("gatewayId") String gatewayId,
                                       @PathVariable("apiId") String apiId,
                                       @PathVariable("from") String from,
                                       @PathVariable("to") String to);

}
