package io.gr1d.ic.usage.api;

import io.gr1d.core.healthcheck.CheckService;
import io.gr1d.ic.usage.api.billing.BillingApi;
import io.gr1d.ic.usage.api.bridge.BridgeApi;
import io.gr1d.ic.usage.api.subscriptions.SubscriptionsApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Healthchecks {

    @Bean
    CheckService billing(final BillingApi api, @Value("${gr1d.service.billing}") final String host) {
        return new CheckServiceApi("Billing API", api, host);
    }

    @Bean
    CheckService subscriptions(final SubscriptionsApi api, @Value("${gr1d.service.subscriptions}") final String host) {
        return new CheckServiceApi("Subscriptions API", api, host);
    }

    @Bean
    CheckService bridge(final BridgeApi api, @Value("${gr1d.service.bridge}") final String host) {
        return new CheckServiceApi("Bridge", api, host);
    }

}

