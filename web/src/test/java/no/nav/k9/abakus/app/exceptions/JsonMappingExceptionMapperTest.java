package no.nav.k9.abakus.app.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

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
