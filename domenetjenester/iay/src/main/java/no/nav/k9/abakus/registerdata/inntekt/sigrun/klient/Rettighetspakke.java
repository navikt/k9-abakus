package no.nav.k9.abakus.registerdata.inntekt.sigrun.klient;

public enum Rettighetspakke {

    OMSORGSPENGER_PLEIEPENGER("navpleieogomsorgspenger"),
    AKTIVITETSPENGER ("navaktivitetspengerforunge"),
    ;

    private final String eksternkode;

    Rettighetspakke(String eksternkode) {
        this.eksternkode = eksternkode;
    }

    public String getEksternkode() {
        return eksternkode;
    }
}
