package no.nav.abakus.iaygrunnlag.inntektsmelding.v1;

import java.util.Objects;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.kodeverk.UtsettelseÅrsakType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.ALWAYS, content = Include.ALWAYS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class UtsettelsePeriodeDto {

    @JsonProperty(value = "periode", required = true)
    @Valid
    @NotNull
    private Periode periode;

    @JsonProperty(value = "utsettelseÅrsak")
    private UtsettelseÅrsakType utsettelseÅrsak;

    protected UtsettelsePeriodeDto() {
    }

    public UtsettelsePeriodeDto(Periode periode) {
        this(periode, null);
    }

    public UtsettelsePeriodeDto(Periode periode, UtsettelseÅrsakType utsettelseÅrsak) {
        Objects.requireNonNull(periode, "periode");
        this.periode = periode;
        this.utsettelseÅrsak = utsettelseÅrsak;
    }

    public Periode getPeriode() {
        return periode;
    }

    public UtsettelseÅrsakType getUtsettelseÅrsakDto() {
        return utsettelseÅrsak;
    }

    public void setUtsettelseÅrsakDto(UtsettelseÅrsakType utsettelseÅrsakDto) {
        this.utsettelseÅrsak = utsettelseÅrsakDto;
    }

    public UtsettelsePeriodeDto medUtsettelseÅrsak(UtsettelseÅrsakType utsettelseÅrsakDto) {
        this.utsettelseÅrsak = utsettelseÅrsakDto;
        return this;
    }
}
