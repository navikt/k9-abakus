package no.nav.k9.abakus.domene.iay;

import java.util.Optional;
import java.util.Set;

import no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType;
import no.nav.k9.abakus.felles.jpa.IntervallEntitet;

public class InntektBuilder {
    private final boolean oppdaterer;
    private Inntekt inntekt;

    private InntektBuilder(Inntekt inntekt, boolean oppdaterer) {
        this.inntekt = inntekt;
        this.oppdaterer = oppdaterer;
    }

    static InntektBuilder ny() {
        return new InntektBuilder(new Inntekt(), false);
    }

    static InntektBuilder oppdatere(Inntekt oppdatere) {
        return new InntektBuilder(oppdatere, true);
    }

    public static InntektBuilder oppdatere(Optional<Inntekt> oppdatere) {
        return oppdatere.map(InntektBuilder::oppdatere).orElseGet(InntektBuilder::ny);
    }

    public InntektBuilder medInntektsKilde(InntektskildeType inntektskildeType) {
        this.inntekt.setInntektsKilde(inntektskildeType);
        return this;
    }

    public InntektBuilder leggTilInntektspost(InntektspostBuilder builder) {
        Inntektspost inntektspost = builder.build();
        inntekt.leggTilInntektspost(inntektspost);
        return this;
    }

    public InntektBuilder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        this.inntekt.setArbeidsgiver(arbeidsgiver);
        return this;
    }

    public InntektspostBuilder getInntektspostBuilder() {
        return inntekt.getInntektspostBuilder();
    }

    boolean getErOppdatering() {
        return this.oppdaterer;
    }

    public void tilbakestillInntektsposterForPerioder(Set<IntervallEntitet> perioder) {
        this.inntekt.tilbakestillInntektsposterForPerioder(perioder);
    }

    public Inntekt build() {
        if (inntekt.hasValues()) {
            return inntekt;
        }
        throw new IllegalStateException();
    }
}
