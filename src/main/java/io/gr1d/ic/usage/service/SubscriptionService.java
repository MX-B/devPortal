package io.gr1d.ic.usage.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import feign.FeignException;
import io.gr1d.core.util.IterablePage;
import io.gr1d.ic.usage.api.subscriptions.SubscriptionsApi;
import io.gr1d.ic.usage.api.subscriptions.model.ApiGatewayResponse;
import io.gr1d.ic.usage.api.subscriptions.model.ApiGatewayTenantResponse;
import io.gr1d.ic.usage.api.subscriptions.model.SubscriptionResponse;
import io.gr1d.ic.usage.api.subscriptions.model.TenantResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SubscriptionService {

    private final SubscriptionsApi subscriptionApi;

    @Autowired
    public SubscriptionService(final SubscriptionsApi subscriptionApi) {
        this.subscriptionApi = subscriptionApi;
    }

    public Iterable<TenantResponse> findAllTenants() {
        return new IterablePage<>(page -> subscriptionApi.listTenants(String.valueOf(page)));
    }

    public Iterable<ApiGatewayResponse> findAllApiGateway() {
        return new IterablePage<>(page -> subscriptionApi.listApiGateway(String.valueOf(page)));
    }

    public SubscriptionResponse getUserApiSubscription(final String tenantRealm, final String gateway,
                                                final String apiId, final String userId,
                                                final LocalDate referenceDate) {
        try {
            return subscriptionApi.getUserApiSubscription(tenantRealm, gateway, apiId, userId, referenceDate.format(DateTimeFormatter.ISO_DATE));
        } catch (FeignException e) {
            if (e.status() == 404) {
                return null;
            }
            throw e;
        }
    }

    public Iterable<ApiGatewayTenantResponse> findAllApiGatewayTenant() {
      return new IterablePage<>(page -> subscriptionApi.listApiGatewayTenant(String.valueOf(page)));
    }

}
