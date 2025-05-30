package no.nav.k9.abakus.web.app.vedlikehold;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.abakus.iaygrunnlag.UuidDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Input request for å bytte en utgått aktørid med en aktiv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class VarigEndringRequest {

    @JsonProperty(value = "eksternReferanse", required = true)
    @Valid
    private UuidDto eksternReferanse;

    @JsonProperty(value = "orgnummer", required = true)
    @NotNull
    @Pattern(regexp = "^\\d{9}+$", message = "orgnr [${validatedValue}] har ikke gyldig verdi (9 siffer)")
    private String orgnummer;

    @JsonProperty(value = "endringDato", required = true)
    private LocalDate endringDato;

    @JsonProperty(value = "bruttoInntekt", required = true)
    @DecimalMin(value = "0.00", message = "beløp [${validatedValue}] må være >= {value}")
    private BigDecimal bruttoInntekt;

    @JsonProperty(value = "endringBegrunnelse")
    private String endringBegrunnelse;

    public VarigEndringRequest() {
        // Jackson
    }

    public UuidDto getEksternReferanse() {
        return eksternReferanse;
    }

    public String getOrgnummer() {
        return orgnummer;
    }

    public LocalDate getEndringDato() {
        return endringDato;
    }

    public BigDecimal getBruttoInntekt() {
        return bruttoInntekt;
    }

    public String getEndringBegrunnelse() {
        return endringBegrunnelse;
    }

}
