package no.nav.k9.abakus.registerdata.inntekt.komponenten;

import java.util.Optional;

public enum InntektsFilter {

    OPPTJENINGSGRUNNLAG("PensjonsgivendeA-Inntekt", InntektsFormål.FORMAAL_PGI),
    BEREGNINGSGRUNNLAG("8-28", null),
    SAMMENLIGNINGSGRUNNLAG("8-30", null),
    UNGDOMSYTELSEKONTROLLGRUNNLAG("Ung", InntektsFormål.FORMAAL_UNGDOMSYTELSEN);

    private String kode;
    private InntektsFormål formål;

    InntektsFilter(String kode, InntektsFormål formål) {
        this.kode = kode;
        this.formål = formål;
    }

    public String getKode() {
        return kode;
    }

    public Optional<InntektsFormål> getFormål() {
        return Optional.ofNullable(formål);
    }
}
