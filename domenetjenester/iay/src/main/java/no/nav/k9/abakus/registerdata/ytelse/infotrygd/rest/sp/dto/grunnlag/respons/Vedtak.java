package no.nav.k9.abakus.registerdata.ytelse.infotrygd.rest.sp.dto.grunnlag.respons;

public record Vedtak(Periode periode, int utbetalingsgrad, String arbeidsgiverOrgnr, Boolean erRefusjon, Integer dagsats) {

}
