package io.gr1d.ic.usage.strategy.usage;

import io.gr1d.core.strategy.StrategyResolver;
import org.springframework.stereotype.Service;

@Service
public class ApiUsageStrategyResolver {

    private final StrategyResolver resolver;

    public ApiUsageStrategyResolver(final StrategyResolver resolver) {
        this.resolver = resolver;
    }

    public ApiUsageStrategy resolve(final String name) {
        return resolver.resolve(ApiUsageStrategy.class, name);
    }
}
