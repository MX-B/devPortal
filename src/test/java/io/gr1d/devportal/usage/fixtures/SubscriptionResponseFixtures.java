package io.gr1d.devportal.usage.fixtures;

import br.com.six2six.fixturefactory.Fixture;
import br.com.six2six.fixturefactory.Rule;
import br.com.six2six.fixturefactory.function.AtomicFunction;
import br.com.six2six.fixturefactory.loader.TemplateLoader;
import io.gr1d.ic.usage.api.subscriptions.model.*;

import java.util.UUID;

public class SubscriptionResponseFixtures implements TemplateLoader {

    @Override
    public void load() {
        final RandomUuid randomUuid = new RandomUuid();
        Fixture.of(PlanResponse.class).addTemplate("valid", new Rule() {
            {
                add("uuid", randomUuid);
                add("modality", "DEFAULT");
                add("value", random(Long.class, range(1000, 50000)));
                add("name", firstName());
            }
        });

        Fixture.of(TenantResponse.class).addTemplate("valid", new Rule() {
            {
                add("uuid", randomUuid);
                add("walletId", randomUuid);
                add("realm", firstName());
                add("name", firstName());
            }
        });

        Fixture.of(GatewayResponse.class).addTemplate("valid", new Rule() {
            {
                add("uuid", randomUuid);
                add("externalId", randomUuid);
                add("name", firstName());
            }
        });

        Fixture.of(ProviderResponse.class).addTemplate("valid", new Rule() {
            {
                add("uuid", randomUuid);
                add("name", firstName());
            }
        });

        Fixture.of(ApiGatewayResponse.class).addTemplate("valid", new Rule() {
            {
                add("uuid", randomUuid);
                add("apiUuid", randomUuid);
                add("externalId", randomUuid);
                add("name", firstName());
                add("gateway", one(GatewayResponse.class, "valid"));
                add("provider", one(ProviderResponse.class, "valid"));
            }
        });

        Fixture.of(SubscriptionResponse.class).addTemplate("valid", new Rule() {
            {
                add("uuid", randomUuid);
                add("userId", randomUuid);

                add("plan", one(PlanResponse.class, "valid"));
                add("tenant", one(TenantResponse.class, "valid"));
                add("api", one(ApiGatewayResponse.class, "valid"));
            }
        });

        Fixture.of(PlanEndpointResponse.class).addTemplate("valid", new Rule() {
            {
                add("uuid", randomUuid);
                add("externalId", randomUuid);
                add("endpoint", "/test");
            }
        });
    }

    public static class RandomUuid implements AtomicFunction {

        @Override
        @SuppressWarnings("unchecked")
        public String generateValue() {
            return UUID.randomUUID().toString();
        }
    }

}
