package no.nav.abakus.iaygrunnlag.inntektsmelding.v1;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.kodeverk.NaturalytelseType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.ALWAYS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class NaturalytelseDto {

    @JsonProperty(value = "periode", required = true)
    @Valid
    @NotNull
    private Periode periode;

    @JsonProperty(value = "naturalytelseType", required = true)
    @NotNull
    private NaturalytelseType type;

    /**
     * Tillater kun positive verdier.  Max verdi håndteres av mottager.
     */
    @JsonProperty(value = "beløpPerMnd")
    @DecimalMin(value = "0.00", message = "beløp [${validatedValue}] må være >= {value}")
    private BigDecimal beløpPerMnd;

    protected NaturalytelseDto() {
        // default ctor
    }

    public NaturalytelseDto(Periode periode, NaturalytelseType type, BigDecimal beløpPerMnd) {
        Objects.requireNonNull(periode, "periode");
        Objects.requireNonNull(type, "type");
        this.periode = periode;
        this.type = type;
        this.beløpPerMnd = beløpPerMnd == null ? null : beløpPerMnd.setScale(2, RoundingMode.HALF_UP);
    }

    public NaturalytelseDto(Periode periode, NaturalytelseType type, int beløpPerMnd) {
        this(periode, type, BigDecimal.valueOf(beløpPerMnd));
    }

    public Periode getPeriode() {
        return periode;
    }

    public BigDecimal getBeløpPerMnd() {
        return beløpPerMnd;
    }

    public NaturalytelseType getType() {
        return type;
    }
}
