package no.nav.abakus.iaygrunnlag.arbeid.v1;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.abakus.iaygrunnlag.Aktør;
import no.nav.abakus.iaygrunnlag.ArbeidsforholdRefDto;
import no.nav.abakus.iaygrunnlag.kodeverk.ArbeidType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class YrkesaktivitetDto {

    @JsonProperty("arbeidsgiver")
    @Valid
    private Aktør arbeidsgiver;

    @JsonProperty("arbeidsforholdId")
    @Valid
    private ArbeidsforholdRefDto arbeidsforholdId;

    @JsonProperty("arbeidType")
    @NotNull
    private ArbeidType arbeidType;

    @JsonProperty("aktivitetsAvtaler")
    @Valid
    private List<AktivitetsAvtaleDto> aktivitetsAvtaler;

    @JsonProperty("permisjoner")
    @Valid
    private List<PermisjonDto> permisjoner;

    @JsonProperty("navnArbeidsgiverUtland")
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message = "Yrkesaktivitet#navnArbeidsgiverUtland [${validatedValue}] matcher ikke tillatt pattern [{regexp}]")
    @Valid
    private String navnArbeidsgiverUtland;

    protected YrkesaktivitetDto() {
        // default ctor
    }

    public YrkesaktivitetDto(ArbeidType arbeidType) {
        this.arbeidType = Objects.requireNonNull(arbeidType, "arbeidType");
    }

    @Deprecated(forRemoval = true) // bruk enum
    public YrkesaktivitetDto(String arbeidType) {
        this(ArbeidType.fraKode(Objects.requireNonNull(arbeidType, "arbeidType")));
    }

    @AssertTrue(message = "Må ha minst en av aktivitetsAvtaler eller permisjoner")
    private boolean isOk() {
        boolean ok = (aktivitetsAvtaler != null && !aktivitetsAvtaler.isEmpty()) || (permisjoner != null && !permisjoner.isEmpty());
        return ok;
    }

    public Optional<Aktør> getArbeidsgiver() {
        return Optional.ofNullable(arbeidsgiver);
    }

    public ArbeidsforholdRefDto getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public void setArbeidsforholdId(ArbeidsforholdRefDto arbeidsforholdId) {
        this.arbeidsforholdId = arbeidsforholdId;
    }

    public YrkesaktivitetDto medArbeidsforholdId(ArbeidsforholdRefDto arbeidsforholdId) {
        setArbeidsforholdId(arbeidsforholdId);
        return this;
    }

    public ArbeidType getType() {
        return arbeidType;
    }

    public List<AktivitetsAvtaleDto> getAktivitetsAvtaler() {
        return aktivitetsAvtaler;
    }

    public void setAktivitetsAvtaler(List<AktivitetsAvtaleDto> aktivitetsAvtaler) {
        this.aktivitetsAvtaler = aktivitetsAvtaler;
    }

    public YrkesaktivitetDto medAktivitetsAvtaler(List<AktivitetsAvtaleDto> aktivitetsAvtaler) {
        this.aktivitetsAvtaler = aktivitetsAvtaler;
        return this;
    }

    public List<PermisjonDto> getPermisjoner() {
        return permisjoner;
    }

    public void setPermisjoner(List<PermisjonDto> permisjoner) {
        this.permisjoner = permisjoner;
    }

    public YrkesaktivitetDto medPermisjoner(List<PermisjonDto> permisjoner) {
        this.permisjoner = permisjoner;
        return this;
    }

    public String getNavnArbeidsgiverUtland() {
        return navnArbeidsgiverUtland;
    }

    public YrkesaktivitetDto medNavnArbeidsgiverUtland(String navnArbeidsgiverUtland) {
        this.navnArbeidsgiverUtland = navnArbeidsgiverUtland;
        return this;
    }

    public YrkesaktivitetDto medArbeidsgiver(Aktør arbeidsgiver) {
        this.arbeidsgiver = arbeidsgiver;
        return this;
    }
}
