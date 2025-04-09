package no.nav.abakus.iaygrunnlag.oppgittopptjening.v1;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.abakus.iaygrunnlag.JsonObjectMapper;
import no.nav.abakus.iaygrunnlag.Periode;

class OppgittYtelseDtoSerDesTest {

    @Test
    void skal_serialisere_og_deserialisere_oppgitt_ytelse() throws JsonProcessingException {
        final var oppgittYtelseDto = new OppgittYtelseDto(new Periode(LocalDate.parse("2023-01-01"), LocalDate.parse("2023-12-31")),
            new BigDecimal("1000.00"));
        var ser = JsonObjectMapper.getMapper().writeValueAsString(oppgittYtelseDto);
        var des = JsonObjectMapper.getMapper().readValue(ser, OppgittYtelseDto.class);

        assertThat(des.ytelse()).isEqualTo(oppgittYtelseDto.ytelse());
        assertThat(des.periode()).isEqualTo(oppgittYtelseDto.periode());
    }
}
