package io.gr1d.ic.usage.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public interface HealthcheckApi {

    @RequestMapping(path = "/hc", method = RequestMethod.GET)
    void healthcheck();

}
