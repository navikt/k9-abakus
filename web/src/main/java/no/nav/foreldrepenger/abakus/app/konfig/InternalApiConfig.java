package no.nav.foreldrepenger.abakus.app.konfig;

import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import no.nav.foreldrepenger.abakus.app.metrics.PrometheusRestService;
import no.nav.foreldrepenger.abakus.app.healthcheck.HealthCheckRestService;

@ApplicationPath(InternalApiConfig.API_URL)
public class InternalApiConfig extends Application {

    public static final String API_URL = "internal";

    public InternalApiConfig() {
        // CDI
    }

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(PrometheusRestService.class, HealthCheckRestService.class);
    }

}