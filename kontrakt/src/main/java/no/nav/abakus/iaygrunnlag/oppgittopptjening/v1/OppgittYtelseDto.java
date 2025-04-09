package no.nav.abakus.iaygrunnlag.oppgittopptjening.v1;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import no.nav.abakus.iaygrunnlag.Periode;

record OppgittYtelseDto(@Valid @NotNull Periode periode,
                        @DecimalMin(value = "0.00", message = "beløp [${validatedValue}] må være >= {value}") @DecimalMax(value = "9999999999.00", message = "beløp [${validatedValue}] må være >= {value}") @Digits(integer = 10, fraction = 2) BigDecimal ytelse) {

}
