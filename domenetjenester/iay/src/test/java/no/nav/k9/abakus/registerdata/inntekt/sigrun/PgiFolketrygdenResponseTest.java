package no.nav.k9.abakus.registerdata.inntekt.sigrun;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.k9.felles.integrasjon.rest.DefaultJsonMapper;

import org.junit.jupiter.api.Test;

import no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType;
import no.nav.k9.abakus.registerdata.inntekt.sigrun.klient.PgiFolketrygdenResponse;
import no.nav.k9.abakus.registerdata.inntekt.sigrun.klient.SigrunPgiFolketrygdenMapper;

class PgiFolketrygdenResponseTest {

    // Offisielt https://skatteetaten.github.io/api-dokumentasjon/api/pgi_folketrygden?tab=Eksempler
    // Erstattet FASTLAND / "pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage": 85000 med null for testformål
    private static final String SKATT_EKSEMPEL = """
        {
            "norskPersonidentifikator": "02116049964",
            "inntektsaar": 2019,
            "pensjonsgivendeInntekt": [
                {
                    "skatteordning": "FASTLAND",
                    "datoForFastsetting": "2020-09-27",
                    "pensjonsgivendeInntektAvLoennsinntekt": 698219,
                    "pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel": null,
                    "pensjonsgivendeInntektAvNaeringsinntekt": 150000,
                    "pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage": null
                },
                {
                    "skatteordning": "SVALBARD",
                    "datoForFastsetting": "2020-09-27",
                    "pensjonsgivendeInntektAvLoennsinntekt": 492160,
                    "pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel": null,
                    "pensjonsgivendeInntektAvNaeringsinntekt": 2530000,
                    "pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage": null
                }
            ]
        }
        """;

    @Test
    void skal_mappe_og_beregne_slå_sammen_lønn_fra_offisiell_fra_pensjonsgivendeinntektforfolketrygden() throws JsonProcessingException {
        var respons = DefaultJsonMapper.MAPPER.readValue(SKATT_EKSEMPEL, PgiFolketrygdenResponse.class);

        var intern = SigrunPgiFolketrygdenMapper.mapFraPgiResponseTilIntern(List.of(respons));

        assertThat(intern.values().stream().findFirst().map(m -> m.get(InntektspostType.SELVSTENDIG_NÆRINGSDRIVENDE)))
            .hasValueSatisfying(v -> assertThat(v).isEqualByComparingTo(BigDecimal.valueOf(2680000)));
        assertThat(intern.values().stream().findFirst().map(m -> m.get(InntektspostType.LØNN)))
            .hasValueSatisfying(v -> assertThat(v).isEqualByComparingTo(BigDecimal.valueOf(1190379)));
        assertThat(intern.values().stream().findFirst().map(m -> m.get(InntektspostType.NÆRING_FISKE_FANGST_FAMBARNEHAGE))).isEmpty();
    }

    // Litt fiktiv ettersom det ikke skal komme flere element med samme inntektsår og det kun hentes ett inntektsår av gangen
    private static final String DOLLY_RESPONSE = """
        [
          {
             "norskPersonidentifikator": "24909099443",
             "inntektsaar": 2022,
             "pensjonsgivendeInntekt": [
               {
                  "skatteordning": "FASTLAND",
                  "datoForFastsetting": "2023-05-01T19:58:17",
                  "pensjonsgivendeInntektAvLoennsinntekt": "",
                  "pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel": null,
                  "pensjonsgivendeInntektAvNaeringsinntekt": "80000",
                  "pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage": null
               }
             ]
          },
          {
             "norskPersonidentifikator": "24909099443",
             "inntektsaar": 2022,
             "pensjonsgivendeInntekt": [
               {
                  "skatteordning": "FASTLAND",
                  "datoForFastsetting": "2023-05-01T19:58:17",
                  "pensjonsgivendeInntektAvLoennsinntekt": "",
                  "pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel": null,
                  "pensjonsgivendeInntektAvNaeringsinntekt": "100000",
                  "pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage": null
               }
             ]
          }
        ]
        """;

    @Test
    void skal_mappe_og_beregne_lønn_fra_dolly_med_lønn_fra_pensjonsgivendeinntektforfolketrygden() throws JsonProcessingException {
        var responseStub = DefaultJsonMapper.MAPPER.readValue(DOLLY_RESPONSE, PgiFolketrygdenResponse[].class);

        var intern = SigrunPgiFolketrygdenMapper.mapFraPgiResponseTilIntern(Arrays.stream(responseStub).toList());

        assertThat(intern.values().stream().findFirst().map(m -> m.get(InntektspostType.SELVSTENDIG_NÆRINGSDRIVENDE)))
            .hasValueSatisfying(v -> assertThat(v).isEqualByComparingTo(BigDecimal.valueOf(180000)));
        assertThat(intern.values().stream().findFirst().map(m -> m.get(InntektspostType.LØNN))).isEmpty();
        assertThat(intern.values().stream().findFirst().map(m -> m.get(InntektspostType.NÆRING_FISKE_FANGST_FAMBARNEHAGE))).isEmpty();
    }

}
