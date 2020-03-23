package io.gr1d.ic.usage.strategy.billing;

import io.gr1d.core.strategy.StrategyResolver;
import org.springframework.stereotype.Service;

@Service
public class ModalityStrategyResolver {

    private final StrategyResolver resolver;

    public ModalityStrategyResolver(final StrategyResolver resolver) {
        this.resolver = resolver;
    }

    public ModalityStrategy resolve(final String modality) {
        return resolver.resolve(ModalityStrategy.class, modality);
    }
}
