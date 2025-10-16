package no.nav.k9.abakus.registerdata.inntekt.komponenten.impl;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.k9.abakus.registerdata.inntekt.komponenten.InntektTjeneste;
import no.nav.k9.abakus.registerdata.inntekt.komponenten.InntektsFilter;
import no.nav.k9.abakus.typer.AktørId;
import no.nav.k9.felles.integrasjon.rest.SystemUserOidcRestClient;

@ExtendWith(MockitoExtension.class)
class InntektTjenesteImplTest {
    private static final String AKTØRID = "0123456789012";
    private static final YearMonth GJELDENDE_MÅNED = YearMonth.now();
    private static final String SYKEPENGER = "sykepenger";
    private static final String ORGNR = "456";

    private SystemUserOidcRestClient restKlient = Mockito.mock(SystemUserOidcRestClient.class);
    private InntektTjeneste inntektTjeneste = new InntektTjeneste(restKlient, "dummyUrl");


    @Test
    void skal_kalle_consumer_og_oversette_response() {
        // Arrange
        var response = opprettResponse(InntektsFilter.SAMMENLIGNINGSGRUNNLAG);
        var responseB = opprettResponse(InntektsFilter.BEREGNINGSGRUNNLAG);

        when(restKlient.post(any(), any(), eq(InntektTjeneste.InntektBulkApiUt.class))).thenReturn(new InntektTjeneste.InntektBulkApiUt(List.of(response, responseB)));

        // Tre måneder siden
        var arbeidsInntektInformasjonMnd3 = opprettInntektsinformasjon(GJELDENDE_MÅNED.minusMonths(3), ORGNR);
        arbeidsInntektInformasjonMnd3.inntektListe().add(opprettInntekt(new BigDecimal(50), SYKEPENGER, "YtelseFraOffentlige"));
        response.data().add(arbeidsInntektInformasjonMnd3);

        // To måneder siden
        var arbeidsInntektInformasjonMnd2a = opprettInntektsinformasjon(GJELDENDE_MÅNED.minusMonths(2), ORGNR);
        arbeidsInntektInformasjonMnd2a.inntektListe().add(opprettInntekt(new BigDecimal(100), SYKEPENGER, "YtelseFraOffentlige"));
        response.data().add(arbeidsInntektInformasjonMnd2a);
        var arbeidsInntektInformasjonMnd2b = opprettInntektsinformasjon(GJELDENDE_MÅNED.minusMonths(2), ORGNR);
        arbeidsInntektInformasjonMnd2b.inntektListe().add(opprettInntekt(new BigDecimal(200), null, "Loennsinntekt"));
        response.data().add(arbeidsInntektInformasjonMnd2b);

        // En måned siden
        var arbeidsInntektInformasjonMnd1 = opprettInntektsinformasjon(GJELDENDE_MÅNED.minusMonths(1), ORGNR);
        arbeidsInntektInformasjonMnd1.inntektListe().add(opprettInntekt(new BigDecimal(400), null, "Loennsinntekt"));
        response.data().add(arbeidsInntektInformasjonMnd1);

        // Denne måneden
        var arbeidsInntektInformasjonMnd0 = opprettInntektsinformasjon(GJELDENDE_MÅNED, ORGNR);
        arbeidsInntektInformasjonMnd0.inntektListe().add(opprettInntekt(new BigDecimal(405), null, "Loennsinntekt"));
        response.data().add(arbeidsInntektInformasjonMnd0);

        InntektTjeneste.YearMonthPeriode periode = new InntektTjeneste.YearMonthPeriode(GJELDENDE_MÅNED.minusMonths(3), GJELDENDE_MÅNED);

        // Act
        var inntektsInformasjon = inntektTjeneste.finnInntekt(new AktørId(AKTØRID), periode, Set.of(InntektskildeType.INNTEKT_SAMMENLIGNING, InntektskildeType.INNTEKT_BEREGNING), YtelseType.PLEIEPENGER_NÆRSTÅENDE);

        // Assert
        verify(restKlient, times(1)).post(any(), any(), eq(InntektTjeneste.InntektBulkApiUt.class));

        assertThat(inntektsInformasjon.keySet()).hasSize(2);
        final var månedsinntekter = inntektsInformasjon.get(InntektskildeType.INNTEKT_SAMMENLIGNING).getMånedsinntekter();
        assertThat(månedsinntekter).hasSize(5);
        assertThat(månedsinntekter.getFirst().getBeløp()).isEqualTo(new BigDecimal(50));
        assertThat(månedsinntekter.get(0).getMåned()).isEqualTo(GJELDENDE_MÅNED.minusMonths(3));
        assertThat(månedsinntekter.get(0).isYtelse()).isTrue();
        assertThat(månedsinntekter.get(2).getBeløp()).isEqualTo(new BigDecimal(200));
        assertThat(månedsinntekter.get(2).getMåned()).isEqualTo(GJELDENDE_MÅNED.minusMonths(2));
        assertThat(månedsinntekter.get(2).getArbeidsgiver()).isEqualTo(ORGNR);
        assertThat(månedsinntekter.get(2).isYtelse()).isFalse();
    }

    private InntektTjeneste.InntektBulk opprettResponse(InntektsFilter filter) {
        return new InntektTjeneste.InntektBulk(filter.getKode(), new ArrayList<>());
    }

    private InntektTjeneste.Inntektsinformasjon opprettInntektsinformasjon(YearMonth måned, String underenhet) {
        return new InntektTjeneste.Inntektsinformasjon(måned, null, underenhet, new ArrayList<>());
    }

    private InntektTjeneste.Inntekt opprettInntekt(BigDecimal beløp, String beskrivelse, String inntektType) {
        return new InntektTjeneste.Inntekt(inntektType, beløp, beskrivelse, null, null);
    }

}
