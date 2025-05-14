package no.nav.k9.abakus.web.app.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import no.nav.k9.abakus.web.app.exceptions.FeilDto;
import no.nav.k9.abakus.web.app.exceptions.JsonMappingExceptionMapper;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;

class JsonMappingExceptionMapperTest {

    @Test
    void skal_mappe_InvalidTypeIdException() {
        var mapper = new JsonMappingExceptionMapper();
        var resultat = mapper.toResponse(new InvalidTypeIdException(null, "Ukjent type-kode", null, "23525"));
        var dto = (FeilDto) resultat.getEntity();
        assertThat(dto.feilmelding()).isEqualTo("JSON-mapping feil: Ukjent type-kode");
        assertThat(dto.feltFeil()).isEmpty();
    }
}
