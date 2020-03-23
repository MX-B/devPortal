package io.gr1d.ic.usage.api.subscriptions;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import io.gr1d.core.model.Gr1dPage;
import io.gr1d.ic.usage.api.HealthcheckApi;
import io.gr1d.ic.usage.api.KeycloakFeignConfiguration;
import io.gr1d.ic.usage.api.bridge.model.User;
import io.gr1d.ic.usage.api.subscriptions.model.ApiGatewayResponse;
import io.gr1d.ic.usage.api.subscriptions.model.ApiGatewayTenantResponse;
import io.gr1d.ic.usage.api.subscriptions.model.SubscriptionResponse;
import io.gr1d.ic.usage.api.subscriptions.model.TenantResponse;

@FeignClient(name = "subscriptionsApi", url = "${gr1d.service.subscriptions}", configuration = KeycloakFeignConfiguration.class)
public interface SubscriptionsApi extends HealthcheckApi {

    @GetMapping("/subscription/tenant/{tenantRealm}/gateway/{gateway}/api/{apiId}/user/{userId}")
    SubscriptionResponse getUserApiSubscription(@PathVariable("tenantRealm") String tenantRealm,
                                                @PathVariable("gateway") String gateway,
                                                @PathVariable("apiId") String apiId,
                                                @PathVariable("userId") String userId,
                                                @RequestHeader("X-Date") String date);

    @GetMapping("/tenant?page={page}")
    Gr1dPage<TenantResponse> listTenants(@PathVariable("page") String page);

    @GetMapping("/apiGateway?page={page}")
    Gr1dPage<ApiGatewayResponse> listApiGateway(@PathVariable("page") String page);

    @GetMapping("/apiGatewayTenant?tenant_realm={tenant_realm}&page={page}")
    Gr1dPage<ApiGatewayTenantResponse> listApiGatewayTenantAndRealm(@PathVariable("tenant_realm") String tenant_realm, @PathVariable("page") String page);
    
    @GetMapping("/apiGatewayTenant?page={page}")
    Gr1dPage<ApiGatewayTenantResponse> listApiGatewayTenant(@PathVariable("page") String page);

    @GetMapping("/subscription/tenant/{tenantRealm}/users")
    List<User> listUsers(@PathVariable("tenantRealm") String tenantRealm);

}
