package io.gr1d.ic.usage.api.billing;

import io.gr1d.core.model.Gr1dPage;
import io.gr1d.ic.usage.api.HealthcheckApi;
import io.gr1d.ic.usage.api.KeycloakFeignConfiguration;
import io.gr1d.ic.usage.api.billing.model.InvoiceRequest;
import io.gr1d.ic.usage.api.billing.model.InvoiceResponse;
import io.gr1d.ic.usage.api.bridge.model.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@FeignClient(name = "billingApi", url = "${gr1d.service.billing}", decode404 = true, configuration = KeycloakFeignConfiguration.class)
public interface BillingApi extends HealthcheckApi {

    @PostMapping(value = "/invoice", produces = APPLICATION_JSON_UTF8_VALUE, consumes = APPLICATION_JSON_UTF8_VALUE)
    InvoiceResponse create(InvoiceRequest invoice);

    @GetMapping(value = "/user?tenant_realm={tenantRealm}&page={page}", produces = APPLICATION_JSON_UTF8_VALUE, consumes = APPLICATION_JSON_UTF8_VALUE)
    Gr1dPage<User> listUsers(@PathVariable("tenantRealm") String tenantRealm, @PathVariable("page") String page);

}
