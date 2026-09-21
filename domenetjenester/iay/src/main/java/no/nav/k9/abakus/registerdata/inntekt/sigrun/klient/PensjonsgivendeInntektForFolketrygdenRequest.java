package no.nav.k9.abakus.registerdata.inntekt.sigrun.klient;

public record PensjonsgivendeInntektForFolketrygdenRequest(
    String personident,
    String inntektsaar,
    String rettighetspakke) {

    public PensjonsgivendeInntektForFolketrygdenRequest(String personident, String inntektsaar, Rettighetspakke rettighetspakke) {
        this(personident, inntektsaar, rettighetspakke.getEksternkode());
    }
}
