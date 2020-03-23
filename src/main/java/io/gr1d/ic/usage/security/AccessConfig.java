package io.gr1d.ic.usage.security;

import io.gr1d.auth.keycloak.ConfigSecurity;
import io.gr1d.auth.keycloak.EndpointConfiguration;
import io.gr1d.core.healthcheck.HealthCheckController;
import io.gr1d.ic.usage.controller.ExecutionController;
import io.gr1d.ic.usage.controller.InvoiceController;
import org.springframework.stereotype.Component;

@Component
public class AccessConfig implements EndpointConfiguration {

    private static final String USER = "user";
    private static final String ADMIN = "admin";

    @Override
    public void configure(final ConfigSecurity config) throws Exception {
        config
                .allow(HealthCheckController.class, "completeHealthCheck", ADMIN)
                .allow(ExecutionController.class, "execute", ADMIN)
                .allow(ExecutionController.class, "executionHistory", USER, ADMIN)
                .allow(ExecutionController.class, "historyPerUser", USER, ADMIN)
                .allow(InvoiceController.class, "list", USER, ADMIN)
                .allow(InvoiceController.class, "find", USER, ADMIN);
    }

}
