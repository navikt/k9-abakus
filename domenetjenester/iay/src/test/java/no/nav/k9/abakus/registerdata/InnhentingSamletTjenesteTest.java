package no.nav.k9.abakus.registerdata;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.k9.abakus.felles.jpa.IntervallEntitet;
import no.nav.k9.abakus.registerdata.ytelse.dagpenger.DagpengerBruttoUtbetaling;
import no.nav.k9.abakus.registerdata.ytelse.dagpenger.DpSakRestKlient;

import no.nav.k9.abakus.typer.PersonIdent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class InnhentingSamletTjenesteTest {


    private InnhentingSamletTjeneste tjeneste;

    @Mock
    private DpSakRestKlient klient;

    @BeforeEach
    void setUp() {
        tjeneste = new InnhentingSamletTjeneste(
            null, null, null, null, klient, true);
    }

    @Test
    void skalMappePerioderFraDagpengerRiktig() {
        var utbetalinger = new ArrayList<DagpengerBruttoUtbetaling>();
        utbetalinger.add(DagpengerBruttoUtbetaling.DagpengerBruttoUtbetalingerBuilder.ny()
            .medFraOgMedDato(LocalDate.of(2026, 2, 4))
            .medTilOgMedDato(LocalDate.of(2026, 2, 4))
            .medKilde(Fagsystem.DPSAK)
            .medSats(100)
            .medUtbetaltBeløp(50)
            .build());
        utbetalinger.add(DagpengerBruttoUtbetaling.DagpengerBruttoUtbetalingerBuilder.ny()
            .medFraOgMedDato(LocalDate.of(2026, 2, 5))
            .medTilOgMedDato(LocalDate.of(2026, 2, 5))
            .medKilde(Fagsystem.DPSAK)
            .medSats(100)
            .medUtbetaltBeløp(50)
            .build());
        utbetalinger.add(DagpengerBruttoUtbetaling.DagpengerBruttoUtbetalingerBuilder.ny()
            .medFraOgMedDato(LocalDate.of(2026, 2, 6))
            .medTilOgMedDato(LocalDate.of(2026, 2, 6))
            .medKilde(Fagsystem.DPSAK)
            .medSats(100)
            .medUtbetaltBeløp(50)
            .build());


        utbetalinger.add(DagpengerBruttoUtbetaling.DagpengerBruttoUtbetalingerBuilder.ny()
            .medFraOgMedDato(LocalDate.of(2026, 2, 9))
            .medTilOgMedDato(LocalDate.of(2026, 2, 9))
            .medKilde(Fagsystem.DPSAK)
            .medSats(100)
            .medUtbetaltBeløp(50)
            .build());
        utbetalinger.add(DagpengerBruttoUtbetaling.DagpengerBruttoUtbetalingerBuilder.ny()
            .medFraOgMedDato(LocalDate.of(2026, 2, 10))
            .medTilOgMedDato(LocalDate.of(2026, 2, 10))
            .medKilde(Fagsystem.DPSAK)
            .medSats(100)
            .medUtbetaltBeløp(50)
            .build());
        utbetalinger.add(DagpengerBruttoUtbetaling.DagpengerBruttoUtbetalingerBuilder.ny()
            .medFraOgMedDato(LocalDate.of(2026, 2, 11))
            .medTilOgMedDato(LocalDate.of(2026, 2, 11))
            .medKilde(Fagsystem.DPSAK)
            .medSats(100)
            .medUtbetaltBeløp(50)
            .build());
        utbetalinger.add(DagpengerBruttoUtbetaling.DagpengerBruttoUtbetalingerBuilder.ny()
            .medFraOgMedDato(LocalDate.of(2026, 2, 12))
            .medTilOgMedDato(LocalDate.of(2026, 2, 12))
            .medKilde(Fagsystem.DPSAK)
            .medSats(100)
            .medUtbetaltBeløp(50)
            .build());
        utbetalinger.add(DagpengerBruttoUtbetaling.DagpengerBruttoUtbetalingerBuilder.ny()
            .medFraOgMedDato(LocalDate.of(2026, 2, 13))
            .medTilOgMedDato(LocalDate.of(2026, 2, 13))
            .medKilde(Fagsystem.DPSAK)
            .medSats(100)
            .medUtbetaltBeløp(50)
            .build());

        utbetalinger.add(DagpengerBruttoUtbetaling.DagpengerBruttoUtbetalingerBuilder.ny()
            .medFraOgMedDato(LocalDate.of(2026, 2, 16))
            .medTilOgMedDato(LocalDate.of(2026, 3, 1))
            .medKilde(Fagsystem.ARENA)
            .medSats(100)
            .medUtbetaltBeløp(200)
            .build());

        utbetalinger.add(DagpengerBruttoUtbetaling.DagpengerBruttoUtbetalingerBuilder.ny()
            .medFraOgMedDato(LocalDate.of(2026, 3, 2))
            .medTilOgMedDato(LocalDate.of(2026, 3, 6))
            .medKilde(Fagsystem.DPSAK)
            .medSats(100)
            .medUtbetaltBeløp(50)
            .build());

        Mockito.when(klient.hentBruttoUtbetalinger(any(), any(), any())).thenReturn(utbetalinger);

        var resultat = tjeneste.hentDagpengerRettighetsperioder(PersonIdent.fra("113"), new IntervallEntitet());

        assertThat(resultat.size()).isEqualTo(4);
        var resultatIterator = resultat.iterator();
        var førstePeriode = resultatIterator.next();
        assertThat(førstePeriode.getFraOgMedDato()).isEqualTo(LocalDate.of(2026, 2, 4));
        assertThat(førstePeriode.getTilOgMedDato()).isEqualTo(LocalDate.of(2026, 2, 6));
        assertThat(førstePeriode.getsats()).isEqualTo(300);
        assertThat(førstePeriode.getUtbetaltBeløp()).isEqualTo(150);

        var andrePeriode = resultatIterator.next();
        assertThat(andrePeriode.getFraOgMedDato()).isEqualTo(LocalDate.of(2026, 2, 9));
        assertThat(andrePeriode.getTilOgMedDato()).isEqualTo(LocalDate.of(2026, 2, 13));
        assertThat(andrePeriode.getsats()).isEqualTo(500);
        assertThat(andrePeriode.getUtbetaltBeløp()).isEqualTo(250);

        var tredjePeriode = resultatIterator.next();
        assertThat(tredjePeriode.getFraOgMedDato()).isEqualTo(LocalDate.of(2026, 2, 16));
        assertThat(tredjePeriode.getTilOgMedDato()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(tredjePeriode.getsats()).isEqualTo(100);
        assertThat(tredjePeriode.getUtbetaltBeløp()).isEqualTo(200);

        var fjerdePeriode = resultatIterator.next();
        assertThat(fjerdePeriode.getFraOgMedDato()).isEqualTo(LocalDate.of(2026, 3, 2));
        assertThat(fjerdePeriode.getTilOgMedDato()).isEqualTo(LocalDate.of(2026, 3, 6));
    }

}
