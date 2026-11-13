package no.nav.k9.abakus.web.app.konfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import no.nav.k9.abakus.dbstoette.CdiDbAwareTest;
import no.nav.k9.abakus.web.app.healthcheck.HealthCheckRestService;
import no.nav.k9.abakus.web.app.metrics.PrometheusRestService;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursResourceType;

import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Request;
import no.nav.k9.felles.sikkerhet.abac.AbacDto;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessurs;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionType;
import no.nav.k9.felles.sikkerhet.abac.TilpassetAbacAttributt;

@CdiDbAwareTest
class RestApiAbacTest {

    private static String PREV_LB_URL;

    /**
     * IKKE ignorer denne testen, sikrer at REST-endepunkter får tilgangskontroll
     * <p>
     * Kontakt Team Humle hvis du trenger hjelp til å endre koden din slik at den går igjennom her *
     */
    @Test
    void test_at_alle_restmetoder_er_annotert_med_BeskyttetRessurs() throws Exception {
        for (var restMethod : RestApiTester.finnAlleRestMetoder()) {
            if (restMethod.getAnnotation(BeskyttetRessurs.class) == null) {
                fail("Mangler @" + BeskyttetRessurs.class.getSimpleName() + "-annotering på " + restMethod);
            }
        }
    }

    @Test
    void sjekk_at_ingen_metoder_er_annotert_med_dummy_verdier() {
        for (var metode : RestApiTester.finnAlleRestMetoder()) {
            assertAtIngenBrukerDummyVerdierPåBeskyttetRessurs(metode);
        }
    }

    /**
     * IKKE ignorer denne testen, helper til med at input til tilgangskontroll blir riktig
     * <p>
     * Kontakt Team Humle hvis du trenger hjelp til å endre koden din slik at den går igjennom her *
     */
    @Test
    void test_at_minst_en_input_parametre_til_restmetoder_implementer_AbacDto() {
        var feilmelding = "Parameter på %s.%s av type %s må implementere " + AbacDto.class.getSimpleName() + ".\n";
        var feilmeldinger = new StringBuilder();

        for (var restMethode : RestApiTester.finnAlleRestMetoder()) {
            for (var parameter : restMethode.getParameters()) {

                if (Collection.class.isAssignableFrom(parameter.getType())) {
                    var type = (ParameterizedType) parameter.getParameterizedType();
                    @SuppressWarnings("rawtypes") Class<?> aClass = (Class) (type.getActualTypeArguments()[0]);
                    if (!AbacDto.class.isAssignableFrom(aClass) && !parameter.isAnnotationPresent(TilpassetAbacAttributt.class)
                        && !IgnorerteInputTyper.ignore(aClass)) {
                        feilmeldinger.append(String.format(feilmelding, restMethode.getDeclaringClass().getSimpleName(), restMethode.getName(),
                            aClass.getSimpleName()));
                    }
                } else {
                    if (!AbacDto.class.isAssignableFrom(parameter.getType()) && !parameter.isAnnotationPresent(TilpassetAbacAttributt.class)
                        && !IgnorerteInputTyper.ignore(parameter.getType())) {
                        feilmeldinger.append(String.format(feilmelding, restMethode.getDeclaringClass().getSimpleName(), restMethode.getName(),
                            parameter.getType().getSimpleName()));
                    }
                }
            }
        }
        if (feilmeldinger.length() > 0) {
            fail("Følgende inputparametre til REST-tjenester mangler AbacDto-impl\n" + feilmeldinger);
        }
    }

    @Test
    void godkjente_rutefjerninger_fjerner_ikke_bevarte_ruter() {
        var ruter = finnRuter(RestApiTester.finnAlleRestTjenester());

        assertThat(ruter).doesNotContain(
            "GET /iay/grunnlag/v1",
            "GET /iay/grunnlag/v1/arbeidsforhold-referanser",
            "POST /arbeidsforhold/v1/arbeidstaker/raw",
            "POST /iay/inntektsmeldinger/v1/hentDiff",
            "POST /iay/oppgitt/v1/motta-og-nullstill-overstyring",
            "POST /ytelse/v1/hent-vedtak-ytelse",
            "POST /forvaltning/settVarigEndring",
            "POST /forvaltning/eliminerInntektsmelding",
            "POST /arbeidsforhold/v1/referanse",
            "POST /registerdata/v1/innhent/status");
        assertThat(ruter).contains(
            "POST /iay/grunnlag/v1",
            "POST /arbeidsforhold/v1/arbeidstaker",
            "POST /iay/oppgitt/v1/overstyr");

        assertThat(finnDriftRuter(RestApiTester.finnAlleRestTjenester())).containsExactlyInAnyOrder(
            "POST /diagnostikk/grunnlag",
            "POST /rapportering/generer",
            "POST /forvaltning/lagre-vedtak-med-feil",
            "POST /forvaltning/dump-vedtak-med-feil",
            "POST /forvaltning/antall-vedtak-med-feil",
            "POST /forvaltning/vaskBegrunnelse",
            "POST /forvaltning/oppdaterAktoerId");
    }

    @Test
    void interne_helse_og_metrics_ruter_er_uendret() {
        var config = new InternalApiConfig();
        assertThat(config.getClasses()).containsExactlyInAnyOrder(HealthCheckRestService.class, PrometheusRestService.class);
        assertThat(finnRuter(RestApiTester.finnAlleRestTjenester(config))).containsExactlyInAnyOrder(
            "GET /health/isAlive",
            "GET /isAlive",
            "GET /health/isReady",
            "GET /isReady",
            "GET /health/preStop",
            "GET /metrics/prometheus");
    }

    private Set<String> finnDriftRuter(Collection<Class<?>> restTjenester) {
        return restTjenester.stream()
            .flatMap(klasse -> Arrays.stream(klasse.getDeclaredMethods())
                .filter(metode -> Modifier.isPublic(metode.getModifiers()))
                .filter(metode -> metode.isAnnotationPresent(BeskyttetRessurs.class))
                .filter(metode -> metode.getAnnotation(BeskyttetRessurs.class).resource() == BeskyttetRessursResourceType.DRIFT)
                .flatMap(metode -> finnRute(klasse, metode).stream()))
            .collect(Collectors.toSet());
    }

    private Set<String> finnRuter(Collection<Class<?>> restTjenester) {
        return restTjenester.stream()
            .flatMap(klasse -> Arrays.stream(klasse.getDeclaredMethods())
                .filter(metode -> Modifier.isPublic(metode.getModifiers()))
                .flatMap(metode -> finnRute(klasse, metode).stream()))
            .collect(Collectors.toSet());
    }

    private Collection<String> finnRute(Class<?> klasse, Method metode) {
        var klassePath = klasse.getAnnotation(Path.class).value();
        var metodePath = metode.isAnnotationPresent(Path.class) ? metode.getAnnotation(Path.class).value() : "";
        var path = normaliserPath(klassePath, metodePath);
        var ruter = new ArrayList<String>();
        for (Annotation annotation : metode.getAnnotations()) {
            var httpMethod = annotation.annotationType().getAnnotation(HttpMethod.class);
            if (httpMethod != null) {
                ruter.add(httpMethod.value() + " " + path);
            }
        }
        return ruter;
    }

    private String normaliserPath(String... paths) {
        return Arrays.stream(paths)
            .flatMap(path -> Arrays.stream(path.split("/")))
            .filter(segment -> !segment.isBlank())
            .collect(Collectors.joining("/", "/", ""));
    }

    private void assertAtIngenBrukerDummyVerdierPåBeskyttetRessurs(Method metode) {
        Class<?> klasse = metode.getDeclaringClass();
        BeskyttetRessurs annotation = metode.getAnnotation(BeskyttetRessurs.class);
        if (annotation != null) {
            if (annotation.action() == BeskyttetRessursActionType.DUMMY) {
                fail(klasse.getSimpleName() + "." + metode.getName() + " Ikke bruk DUMMY-verdi for action()");
            }
            if (annotation.resource() == BeskyttetRessursResourceType.DUMMY) {
                fail(klasse.getSimpleName() + "." + metode.getName() + " Ikke bruk DUMMY-verdi for resource()");
            }
        }
    }

    /**
     * Disse typene slipper naturligvis krav om impl av {@link AbacDto}
     */
    enum IgnorerteInputTyper {
        BOOLEAN(Boolean.class),
        SERVLET(HttpServletRequest.class),
        ENUM(Enum.class),
        UUID(UUID.class),
        REQUEST(Request.class),
        ;

        private Class<?> clazz;

        IgnorerteInputTyper(Class<?> clazz) {
            this.clazz = clazz;
        }

        static boolean ignore(Class<?> klasse) {
            return Arrays.stream(IgnorerteInputTyper.values()).anyMatch(e -> e.clazz.isAssignableFrom(klasse));
        }
    }
}
