package no.nav.k9.abakus.web.app;

import java.util.Set;

/** Kan plugge inn flere sett av klasser dynamisk i {@link ApplicationConfig}. vha. CDI dynamisk oppslag. */
public interface RestClasses {
    Set<Class<?>> getRestClasses();
}
