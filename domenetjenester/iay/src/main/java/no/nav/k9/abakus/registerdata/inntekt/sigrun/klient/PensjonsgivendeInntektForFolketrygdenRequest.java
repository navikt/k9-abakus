package no.nav.k9.abakus.registerdata.inntekt.sigrun.klient;

public record PensjonsgivendeInntektForFolketrygdenRequest(
    String personident,
    String inntektsaar,
    String rettighetspakke) {

    public PensjonsgivendeInntektForFolketrygdenRequest(String personident, String inntektsaar) {
        this(personident, inntektsaar, "navpleieogomsorgspenger");
    }
}
