package no.nav.k9.abakus.registerdata.ytelse.infotrygd.rest.sp.dto.grunnlag.respons;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAlias;


public record Arbeidsforhold(@JsonAlias("arbeidsgiverOrgnr") Orgnummer orgnr, @JsonAlias("inntektForPerioden") Integer inntekt,
                             Inntektsperiode inntektsperiode, Boolean refusjon, LocalDate refusjonTom) {
}
