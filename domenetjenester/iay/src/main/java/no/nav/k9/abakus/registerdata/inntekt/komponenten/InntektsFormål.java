package no.nav.k9.abakus.registerdata.inntekt.komponenten;

public enum InntektsFormål {

    FORMAAL_OMSORSGPENGER("Omsorgspenger"),
    FORMAAL_PLEIEPENGER_SYKT_BARN("PleiepengerSyktBarn"),
    FORMAAL_PLEIEPENGER_NÆRSTÅENDE("PleiepengerNaerstaaende"),
    FORMAAL_OPPLÆRINGSPENGER("Opplaeringspenger"),
    FORMAAL_PGI("PensjonsgivendeA-inntekt"),
    FORMAAL_UNGDOMSYTELSEN("Ung");


    private String kode;

    private InntektsFormål(String kode) {
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }
}
