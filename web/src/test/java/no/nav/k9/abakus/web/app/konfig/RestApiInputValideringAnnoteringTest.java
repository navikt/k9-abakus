package no.nav.k9.abakus.web.app.konfig;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.function.Function;

import jakarta.validation.Valid;
import jakarta.ws.rs.core.Context;

import no.nav.k9.abakus.dbstoette.CdiDbAwareTest;

import org.junit.jupiter.api.Test;


@CdiDbAwareTest
class RestApiInputValideringAnnoteringTest extends RestApiTester {

    private Function<Method, String> printKlasseOgMetodeNavn = (method -> String.format("%s.%s", method.getDeclaringClass(), method.getName()));

    /**
     * IKKE ignorer eller fjern denne testen, den sørger for at inputvalidering er i orden for REST-grensesnittene
     * <p>
     * Kontakt Team Humle hvis du trenger hjelp til å endre koden din slik at den går igjennom her
     */
    @Test
    void alle_felter_i_objekter_som_brukes_som_inputDTO_skal_enten_ha_valideringsannotering_eller_være_av_godkjent_type() throws Exception {
        for (Method method : finnAlleRestMetoder()) {
            for (int i = 0; i < method.getParameterCount(); i++) {
                Parameter parameter = method.getParameters()[i];
                if (parameter.getType().isEnum()) {
                    continue;
                }
                assertThat(method.getParameterTypes()[i].isAssignableFrom(String.class) && !parameter.isAnnotationPresent(
                    jakarta.validation.constraints.Pattern.class)).as(
                    "REST-metoder skal ikke har parameter som er String eller mer generelt uten at @Pattern brukes. Bruk DTO-er og valider. "
                        + printKlasseOgMetodeNavn.apply(method)).isFalse();
                assertThat(isRequiredAnnotationPresent(parameter)).as(
                        "Alle parameter for REST-metoder skal være annotert med @Valid. Var ikke det for " + printKlasseOgMetodeNavn.apply(method))
                    .withFailMessage("Fant parametere som mangler @Valid annotation '" + parameter.toString() + "'")
                    .isTrue();
            }
        }
    }

    private boolean isRequiredAnnotationPresent(Parameter parameter) {
        final Valid validAnnotation = parameter.getAnnotation(Valid.class);
        if (validAnnotation == null) {
            final Context contextAnnotation = parameter.getAnnotation(Context.class);
            return contextAnnotation != null;
        }
        return true;
    }

}
