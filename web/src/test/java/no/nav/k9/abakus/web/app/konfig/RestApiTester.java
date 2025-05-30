package no.nav.k9.abakus.web.app.konfig;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import no.nav.k9.abakus.dbstoette.CdiDbAwareTest;
import no.nav.k9.abakus.web.app.ApplicationConfig;

public class RestApiTester {

    static final List<Class<?>> UNNTATT = Collections.singletonList(OpenApiResource.class);

    static Collection<Method> finnAlleRestMetoder() {
        List<Method> liste = new ArrayList<>();
        for (Class<?> klasse : finnAlleRestTjenester()) {
            for (Method method : klasse.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers())) {
                    liste.add(method);
                }
            }
        }
        return liste;
    }

    static Collection<Class<?>> finnAlleRestTjenester() {
        List<Class<?>> klasser = new ArrayList<>();
        klasser.addAll(finnAlleRestTjenester(new ApplicationConfig()));
        return klasser;
    }

    static Collection<Class<?>> finnAlleRestTjenester(Application config) {
        return config.getClasses()
            .stream()
            .filter(c -> c.getAnnotation(Path.class) != null)
            .filter(c -> !UNNTATT.contains(c))
            .collect(Collectors.toList());
    }
}
