package no.nav.k9.abakus.registerdata.inntekt.komponenten;

public enum InntektsFormål {

    FORMAAL_FORELDREPENGER("Foreldrepenger"),
    FORMAAL_PGI("PensjonsgivendeA-inntekt");

    private String kode;

    private InntektsFormål(String kode) {
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }
}
