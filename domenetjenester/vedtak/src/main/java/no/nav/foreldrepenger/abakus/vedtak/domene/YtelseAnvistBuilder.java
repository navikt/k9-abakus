package no.nav.k9.abakus.vedtak.domene;

import java.math.BigDecimal;

import no.nav.k9.abakus.felles.jpa.IntervallEntitet;
import no.nav.k9.abakus.typer.Beløp;
import no.nav.k9.abakus.typer.Stillingsprosent;

public class YtelseAnvistBuilder {
    private final YtelseAnvist ytelseAnvist;

    YtelseAnvistBuilder(YtelseAnvist ytelseAnvist) {
        this.ytelseAnvist = ytelseAnvist;
    }

    public static YtelseAnvistBuilder ny() {
        return new YtelseAnvistBuilder(new YtelseAnvist());
    }

    public YtelseAnvistBuilder medBeløp(BigDecimal beløp) {
        if (beløp != null) {
            this.ytelseAnvist.setBeløp(new Beløp(beløp));
        }
        return this;
    }

    public YtelseAnvistBuilder medDagsats(BigDecimal dagsats) {
        if (dagsats != null) {
            this.ytelseAnvist.setDagsats(new Beløp(dagsats));
        }
        return this;
    }

    public YtelseAnvistBuilder medAnvistPeriode(IntervallEntitet intervallEntitet) {
        this.ytelseAnvist.setAnvistPeriode(intervallEntitet);
        return this;
    }

    public YtelseAnvistBuilder leggTilFordeling(VedtakYtelseAndelBuilder vedtakYtelseFordelingBuilder) {
        this.ytelseAnvist.leggTilFordeling(vedtakYtelseFordelingBuilder.build());
        return this;
    }

    public YtelseAnvistBuilder medUtbetalingsgradProsent(BigDecimal utbetalingsgradProsent) {
        if (utbetalingsgradProsent != null) {
            this.ytelseAnvist.setUtbetalingsgradProsent(Stillingsprosent.utbetalingsgrad(utbetalingsgradProsent));
        }
        return this;
    }

    public YtelseAnvist build() {
        return ytelseAnvist;
    }

}
