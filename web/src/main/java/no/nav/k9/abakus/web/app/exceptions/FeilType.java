package no.nav.k9.abakus.web.app.exceptions;

public enum FeilType {
    MANGLER_TILGANG_FEIL("MANGLER_TILGANG_FEIL"),
    TOMT_RESULTAT_FEIL("TOMT_RESULTAT_FEIL"),
    GENERELL_FEIL("GENERELL_FEIL");

    private final String navn;

    FeilType(String navn) {
        this.navn = navn;
    }

    @Override
    public String toString() {
        return navn;
    }
}
