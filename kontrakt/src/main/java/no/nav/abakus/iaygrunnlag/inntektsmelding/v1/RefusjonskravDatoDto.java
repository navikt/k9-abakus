package no.nav.abakus.iaygrunnlag.inntektsmelding.v1;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.abakus.iaygrunnlag.Aktør;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class RefusjonskravDatoDto {

    @JsonProperty(value = "arbeidsgiver", required = true)
    @Valid
    @NotNull
    private Aktør arbeidsgiver;

    @JsonProperty(value = "førsteDagMedRefusjonskrav", required = true)
    @NotNull
    @Valid
    private LocalDate førsteDagMedRefusjonskrav;

    @JsonProperty(value = "harRefusjonFraStart", required = true)
    @NotNull
    @Valid
    private Boolean harRefusjonFraStart;

    @JsonProperty(value = "førsteInnsendingAvRefusjonskrav", required = true)
    @NotNull
    @Valid
    private LocalDate førsteInnsendingAvRefusjonskrav;


    public RefusjonskravDatoDto(Aktør arbeidsgiver,
                                LocalDate førsteInnsendingAvRefusjonskrav,
                                LocalDate førsteDagMedRefusjonskrav,
                                boolean harRefusjonFraStart) {
        Objects.requireNonNull(arbeidsgiver, "arbeidsgiver");
        Objects.requireNonNull(arbeidsgiver, "førsteInnsendingAvRefusjonskrav");
        Objects.requireNonNull(arbeidsgiver, "førsteDagMedRefusjonskrav");
        this.harRefusjonFraStart = harRefusjonFraStart;
        this.arbeidsgiver = arbeidsgiver;
        this.førsteDagMedRefusjonskrav = førsteDagMedRefusjonskrav;
        this.førsteInnsendingAvRefusjonskrav = førsteInnsendingAvRefusjonskrav;
    }

    protected RefusjonskravDatoDto() {
        // for jackson
    }

    public Aktør getArbeidsgiver() {
        return arbeidsgiver;
    }

    public LocalDate getFørsteDagMedRefusjonskrav() {
        return førsteDagMedRefusjonskrav;
    }

    public LocalDate getFørsteInnsendingAvRefusjonskrav() {
        return førsteInnsendingAvRefusjonskrav;
    }

    public boolean harRefusjonFraStart() {
        return harRefusjonFraStart;
    }

    @Override
    public String toString() {
        return "RefusjonskravDatoDto{" + "arbeidsgiver=" + arbeidsgiver + ", førsteDagMedRefusjonskrav=" + førsteDagMedRefusjonskrav
            + ", harRefusjonFraStart=" + harRefusjonFraStart + ", førsteInnsendingAvRefusjonskrav=" + førsteInnsendingAvRefusjonskrav + '}';
    }
}
