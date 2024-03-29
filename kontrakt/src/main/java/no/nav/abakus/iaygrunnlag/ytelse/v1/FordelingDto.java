package no.nav.abakus.iaygrunnlag.ytelse.v1;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.abakus.iaygrunnlag.Aktør;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektPeriodeType;

/**
 * Angir hyppighet og størrelse for ytelse.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class FordelingDto {

    /**
     * Tillater kun positive verdier. Max verdi håndteres av mottager.
     */
    @JsonProperty(value = "beløp", required = true)
    @Valid
    @NotNull
    @DecimalMin(value = "0.00", message = "[${validatedValue}] må være >= {value}")
    private BigDecimal beløp;

    /**
     * Angir hvilken periode beløp gjelder for.
     */
    @JsonProperty(value = "inntektPeriodeType", required = true)
    @NotNull
    private InntektPeriodeType inntektPeriodeType;

    /**
     * Kan være null.
     */
    @JsonProperty(value = "arbeidsgiver")
    @Valid
    private Aktør arbeidsgiver;

    /**
     * Kan være null.
     */
    @JsonProperty(value = "erRefusjon")
    @Valid
    private Boolean erRefusjon;

    protected FordelingDto() {
    }

    public FordelingDto(Aktør arbeidsgiver, InntektPeriodeType inntektPeriodeType, int beløp, Boolean erRefusjon) {
        this(arbeidsgiver, inntektPeriodeType, BigDecimal.valueOf(beløp), erRefusjon);
    }

    public FordelingDto(Aktør arbeidsgiver, InntektPeriodeType inntektPeriodeType, BigDecimal beløp, Boolean erRefusjon) {
        Objects.requireNonNull(beløp);
        this.arbeidsgiver = arbeidsgiver;
        this.inntektPeriodeType = inntektPeriodeType;
        this.beløp = beløp.setScale(2, RoundingMode.HALF_UP);
        this.erRefusjon = erRefusjon;
    }

    public Aktør getArbeidsgiver() {
        return arbeidsgiver;
    }

    public BigDecimal getBeløp() {
        return beløp;
    }

    public InntektPeriodeType getHyppighet() {
        return inntektPeriodeType;
    }

    public Boolean getErRefusjon() {
        return erRefusjon;
    }
}
