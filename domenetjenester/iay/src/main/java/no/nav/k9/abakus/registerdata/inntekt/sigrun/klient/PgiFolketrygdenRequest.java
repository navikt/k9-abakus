package no.nav.k9.abakus.registerdata.inntekt.sigrun.klient;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PgiFolketrygdenRequest(@JsonProperty("personident") String personident,
                            @JsonProperty("inntektsaar") String inntektsaar,
                            @JsonProperty("rettighetspakke") String rettighetspakke) {
}


