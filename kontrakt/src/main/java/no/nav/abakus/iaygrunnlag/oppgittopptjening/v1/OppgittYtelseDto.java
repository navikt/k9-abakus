package no.nav.abakus.iaygrunnlag.oppgittopptjening.v1;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.kodeverk.ArbeidType;
import no.nav.abakus.iaygrunnlag.kodeverk.Landkode;

import java.math.BigDecimal;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.ALWAYS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class OppgittYtelseDto {

    @JsonProperty(value = "periode", required = true)
    @Valid
    @NotNull
    private Periode periode;

    /**
     * Tillater kun positive verdier.
     */
    @JsonProperty("ytelse")
    @DecimalMin(value = "0.00", message = "beløp [${validatedValue}] må være >= {value}")
    @DecimalMax(value = "9999999999.00", message = "beløp [${validatedValue}] må være >= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal ytelse;

    @JsonCreator
    public OppgittYtelseDto(@JsonProperty(value = "periode", required = true) Periode periode) {
        Objects.requireNonNull(periode, "periode");
        this.periode = periode;
    }

    protected OppgittYtelseDto() {
        // default ctor
    }

    public Periode getPeriode() {
        return periode;
    }

    public OppgittYtelseDto medYtelse(BigDecimal ytelse) {
        setYtelse(ytelse);
        return this;
    }

    public BigDecimal getYtelse() {
        return ytelse;
    }

    public void setYtelse(BigDecimal ytelse) {
        this.ytelse = ytelse;
    }
}
