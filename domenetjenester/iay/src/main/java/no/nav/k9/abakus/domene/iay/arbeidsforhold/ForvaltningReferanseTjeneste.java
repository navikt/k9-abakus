package no.nav.k9.abakus.domene.iay.arbeidsforhold;

public class ForvaltningReferanseTjeneste {

    public static void leggTilReferanse(ArbeidsforholdInformasjon informasjon, ArbeidsforholdReferanse arbeidsforholdReferanse) {
        informasjon.leggTilNyReferanse(arbeidsforholdReferanse);
    }

}
