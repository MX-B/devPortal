package io.gr1d.ic.usage.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.gr1d.ic.usage.api.bridge.model.User;
import io.gr1d.ic.usage.api.subscriptions.SubscriptionsApi;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {

    private final SubscriptionsApi subscriptionApi;

    @Autowired
    public UserService(final SubscriptionsApi subscriptionApi) {
        this.subscriptionApi = subscriptionApi;
    }

    List<User> findAllUsers(final String tenantRealm) {
        return subscriptionApi.listUsers(tenantRealm);
    }
}
