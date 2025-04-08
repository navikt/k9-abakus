package no.nav.abakus.iaygrunnlag.oppgittopptjening.v1;

import java.math.BigDecimal;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import no.nav.abakus.iaygrunnlag.Periode;

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
    public OppgittYtelseDto(@JsonProperty(value = "periode", required = true) Periode periode, BigDecimal ytelse) {
        Objects.requireNonNull(periode, "periode");
        Objects.requireNonNull(ytelse, "ytelse");
        this.periode = periode;
        this.ytelse = ytelse;
    }

    protected OppgittYtelseDto() {
        // default ctor
    }

    public Periode getPeriode() {
        return periode;
    }
    public BigDecimal getYtelse() {
        return ytelse;
    }

}
