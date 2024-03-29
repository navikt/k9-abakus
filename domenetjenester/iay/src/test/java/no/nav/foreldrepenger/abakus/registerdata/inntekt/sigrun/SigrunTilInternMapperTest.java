package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.BeregnetSkatt;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.summertskattegrunnlag.SSGGrunnlag;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.summertskattegrunnlag.SSGResponse;

class SigrunTilInternMapperTest {
    @Test
    void skal_mappe_og_beregne_slå_sammen_lønn_fra_beregent_med_lønn_fra_summertskattegrunnlag() {
        LocalDate iDag = LocalDate.now();
        Map<Year, List<BeregnetSkatt>> beregnet = new HashMap<>();
        Map<Year, Optional<SSGResponse>> summertskattegrunnlag = new HashMap<>();

        Year iÅr = Year.of(iDag.getYear());
        beregnet.put(iÅr, List.of(new BeregnetSkatt(TekniskNavnMapper.PERSONINNTEKT_LØNN, "5000")));
        summertskattegrunnlag.put(iÅr, Optional.of(new SSGResponse(List.of(),
            List.of(new SSGGrunnlag(TekniskNavnMapper.LØNNSINNTEKT_MED_TRYGDEAVGIFTSPLIKT_OMFATTET_AV_LØNNSTREKKORDNINGEN, "5000")), null)));

        Map<IntervallEntitet, Map<InntektspostType, BigDecimal>> map = SigrunTilInternMapper.mapFraSigrunTilIntern(beregnet, summertskattegrunnlag);
        LocalDate førsteDagIÅr = LocalDate.of(iÅr.getValue(), 1, 1);
        LocalDate sisteDagIÅr = LocalDate.of(iÅr.getValue(), 12, 31);

        assertThat(map.get(IntervallEntitet.fraOgMedTilOgMed(førsteDagIÅr, sisteDagIÅr)).get(InntektspostType.LØNN)).isEqualByComparingTo(
            new BigDecimal(10000));
    }

    @Test
    void skal_mappe_og_beregne_når_det_bare_finnes_innslag_fra_summertskattegrunnlag() {
        LocalDate iDag = LocalDate.now();
        Map<Year, List<BeregnetSkatt>> beregnet = new HashMap<>();
        Map<Year, Optional<SSGResponse>> summertskattegrunnlag = new HashMap<>();

        Year iÅr = Year.of(iDag.getYear());
        summertskattegrunnlag.put(iÅr, Optional.of(new SSGResponse(List.of(),
            List.of(new SSGGrunnlag(TekniskNavnMapper.LØNNSINNTEKT_MED_TRYGDEAVGIFTSPLIKT_OMFATTET_AV_LØNNSTREKKORDNINGEN, "5000")), null)));

        Map<IntervallEntitet, Map<InntektspostType, BigDecimal>> map = SigrunTilInternMapper.mapFraSigrunTilIntern(beregnet, summertskattegrunnlag);
        LocalDate førsteDagIÅr = LocalDate.of(iÅr.getValue(), 1, 1);
        LocalDate sisteDagIÅr = LocalDate.of(iÅr.getValue(), 12, 31);

        assertThat(map.get(IntervallEntitet.fraOgMedTilOgMed(førsteDagIÅr, sisteDagIÅr)).get(InntektspostType.LØNN)).isEqualByComparingTo(
            new BigDecimal(5000));
    }

    @Test
    void skal_mappe_og_beregne_slå_sammen_lønn_fra_beregent_med_lønn_fra_summertskattegrunnlag_og_håndtere_skatteoppgjørsdato() {
        LocalDate iDag = LocalDate.now();
        Map<Year, List<BeregnetSkatt>> beregnet = new HashMap<>();
        Map<Year, Optional<SSGResponse>> summertskattegrunnlag = new HashMap<>();

        Year iÅr = Year.of(iDag.getYear());
        beregnet.put(iÅr, List.of(new BeregnetSkatt(TekniskNavnMapper.PERSONINNTEKT_LØNN, "5000"),
            new BeregnetSkatt(TekniskNavnMapper.SKATTEOPPGJØRSDATO, "2018-10-04")));
        summertskattegrunnlag.put(iÅr, Optional.of(new SSGResponse(List.of(),
            List.of(new SSGGrunnlag(TekniskNavnMapper.LØNNSINNTEKT_MED_TRYGDEAVGIFTSPLIKT_OMFATTET_AV_LØNNSTREKKORDNINGEN, "5000")), null)));

        Map<IntervallEntitet, Map<InntektspostType, BigDecimal>> map = SigrunTilInternMapper.mapFraSigrunTilIntern(beregnet, summertskattegrunnlag);
        LocalDate førsteDagIÅr = LocalDate.of(iÅr.getValue(), 1, 1);
        LocalDate sisteDagIÅr = LocalDate.of(iÅr.getValue(), 12, 31);

        assertThat(map.get(IntervallEntitet.fraOgMedTilOgMed(førsteDagIÅr, sisteDagIÅr)).get(InntektspostType.LØNN)).isEqualByComparingTo(
            new BigDecimal(10000));
    }

    @Test
    void skal_mappe_og_beregne_slå_sammen_lønn_fra_beregent_med_lønn_fra_summertskattegrunnlag_med_forskjellige_lønnstyper() {
        LocalDate iDag = LocalDate.now();
        Map<Year, List<BeregnetSkatt>> beregnet = new HashMap<>();
        Map<Year, Optional<SSGResponse>> summertskattegrunnlag = new HashMap<>();

        Year iÅr = Year.of(iDag.getYear());
        beregnet.put(iÅr, List.of(new BeregnetSkatt(TekniskNavnMapper.PERSONINNTEKT_FISKE_FANGST_FAMILIEBARNEHAGE, "5000")));
        summertskattegrunnlag.put(iÅr, Optional.of(new SSGResponse(List.of(),
            List.of(new SSGGrunnlag(TekniskNavnMapper.LØNNSINNTEKT_MED_TRYGDEAVGIFTSPLIKT_OMFATTET_AV_LØNNSTREKKORDNINGEN, "5000")), null)));

        Map<IntervallEntitet, Map<InntektspostType, BigDecimal>> map = SigrunTilInternMapper.mapFraSigrunTilIntern(beregnet, summertskattegrunnlag);
        LocalDate førsteDagIÅr = LocalDate.of(iÅr.getValue(), 1, 1);
        LocalDate sisteDagIÅr = LocalDate.of(iÅr.getValue(), 12, 31);

        assertThat(map.get(IntervallEntitet.fraOgMedTilOgMed(førsteDagIÅr, sisteDagIÅr)).get(InntektspostType.LØNN)).isEqualByComparingTo(
            new BigDecimal(5000));
        assertThat(map.get(IntervallEntitet.fraOgMedTilOgMed(førsteDagIÅr, sisteDagIÅr))
            .get(InntektspostType.NÆRING_FISKE_FANGST_FAMBARNEHAGE)).isEqualByComparingTo(new BigDecimal(5000));
    }

    @Test
    void skal_mappe_og_beregne_slå_sammen_lønn_fra_beregent_ganger2_med_lønn_fra_summertskattegrunnlag() {
        LocalDate iDag = LocalDate.now();
        Map<Year, List<BeregnetSkatt>> beregnet = new HashMap<>();
        Map<Year, Optional<SSGResponse>> summertskattegrunnlag = new HashMap<>();

        Year iÅr = Year.of(iDag.getYear());
        beregnet.put(iÅr, List.of(new BeregnetSkatt(TekniskNavnMapper.PERSONINNTEKT_LØNN, "5000"),
            new BeregnetSkatt(TekniskNavnMapper.PERSONINNTEKT_BARE_PENSJONSDEL, "5000"),
            new BeregnetSkatt(TekniskNavnMapper.SKATTEOPPGJØRSDATO, "2018-10-04")));
        summertskattegrunnlag.put(iÅr, Optional.of(new SSGResponse(List.of(),
            List.of(new SSGGrunnlag(TekniskNavnMapper.LØNNSINNTEKT_MED_TRYGDEAVGIFTSPLIKT_OMFATTET_AV_LØNNSTREKKORDNINGEN, "5000")), null)));

        Map<IntervallEntitet, Map<InntektspostType, BigDecimal>> map = SigrunTilInternMapper.mapFraSigrunTilIntern(beregnet, summertskattegrunnlag);
        LocalDate førsteDagIÅr = LocalDate.of(iÅr.getValue(), 1, 1);
        LocalDate sisteDagIÅr = LocalDate.of(iÅr.getValue(), 12, 31);

        assertThat(map.get(IntervallEntitet.fraOgMedTilOgMed(førsteDagIÅr, sisteDagIÅr)).get(InntektspostType.LØNN)).isEqualByComparingTo(
            new BigDecimal(15000));
    }
}
