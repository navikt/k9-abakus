package no.nav.k9.abakus.web.app;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.ApplicationPath;
import no.nav.k9.abakus.web.app.exceptions.KnownExceptionMappers;
import no.nav.k9.abakus.web.app.jackson.JacksonJsonConfig;

@ApplicationPath(ApplicationConfig.API_URI)
public class ApplicationConfig extends ResourceConfig {

    public static final String API_URI = "/api";

    public ApplicationConfig() {

        OpenAPI oas = new OpenAPI();
        Info info = new Info()
            .title("Vedtaksløsningen - k9-abakus")
            .version("1.0")
            .description("REST grensesnitt for Vedtaksløsningen.");

        oas.info(info)
            .addServersItem(new Server()
                .url("/k9/abakus"));
        SwaggerConfiguration oasConfig = new SwaggerConfiguration()
            .openAPI(oas)
            .prettyPrint(true)
            .scannerClass("io.swagger.v3.jaxrs2.integration.JaxrsAnnotationScanner")
            .resourcePackages(Stream.of("no.nav.k9.", "no.nav.vedtak")
                .collect(Collectors.toSet()));

        try {
            new JaxrsOpenApiContextBuilder<>()
                .openApiConfiguration(oasConfig)
                .buildContext(true)
                .read();
        } catch (OpenApiConfigurationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);

        register(OpenApiResource.class);

        register(new JacksonJsonConfig());

        registerInstances(new LinkedHashSet<>(new KnownExceptionMappers().getExceptionMappers()));

        property(ServerProperties.PROCESSING_RESPONSE_ERRORS_ENABLED, true);


        registerRestTjenester();
    }

    protected void registerRestTjenester() {
        /* Legger til dynamisk fra alle som tilbyr rest tjenester. (Dvs. vil være flere tilbydere for Mock/Komponenttest som har ekstra tjenester). Mulig bytte til @Provider?*/
        CDI.current().select(RestClasses.class).forEach(r -> registerClasses(r.getRestClasses()));
    }

}
