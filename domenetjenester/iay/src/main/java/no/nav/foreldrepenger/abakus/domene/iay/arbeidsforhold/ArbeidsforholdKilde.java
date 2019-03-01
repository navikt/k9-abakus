package no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold;

public enum ArbeidsforholdKilde {
    AAREGISTERET("AA-Registeret"),
    INNTEKTSKOMPONENTEN("A-Inntekt"),
    INNTEKTSMELDING("Inntektsmelding"),
    SAKSBEHANDLER("Saksbehandler");

    private String navn;

    ArbeidsforholdKilde(String navn) {
        this.navn = navn;
    }

    public String getNavn() {
        return navn;
    }
}
