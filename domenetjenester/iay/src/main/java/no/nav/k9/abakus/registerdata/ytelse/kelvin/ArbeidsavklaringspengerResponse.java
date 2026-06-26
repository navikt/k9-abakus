package no.nav.k9.abakus.registerdata.ytelse.kelvin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ArbeidsavklaringspengerResponse(List<AAPVedtak> vedtak) {

    public record AAPVedtak(Integer barnMedStonad,
                            Integer barnetillegg,
                            Integer beregningsgrunnlag,
                            Integer dagsats,
                            Integer dagsatsEtterUføreReduksjon,
                            Kildesystem kildesystem,
                            AAPPeriode periode,
                            String saksnummer,
                            String status,
                            String vedtakId,
                            LocalDate vedtaksdato,
                            List<AAPUtbetaling> utbetaling) { }

    // fraOgMedDato og tilOgMedDato er nullable i kontrakten til Kelvin, men kan bare være null for vedtak fra Arena
    public record AAPPeriode(LocalDate fraOgMedDato, LocalDate tilOgMedDato) {}

    // Dagsats fra kilde = KELVIN er redusert med utbetalingsgrad slik at belop = (dagsats + barnetillegg) * virkedager
    // Barnetillegg her skal være multiplisert med antall barn. Fra kilde KELVIN er den redusert med utbetalingsgrad
    // utbetalingsgrad vil alltid være null for vedtak fra Arena, men vil alltid ha verdi for vedtak fra Kelvin
    public record AAPUtbetaling(AAPPeriode periode,
                                Integer belop,
                                Integer dagsats,
                                Integer barnetillegg,
                                AAPReduksjon reduksjon,
                                Integer utbetalingsgrad) {
    }

    public record AAPReduksjon(BigDecimal annenReduksjon, BigDecimal timerArbeidet) { }

    public enum Kildesystem {
        ARENA, KELVIN
    }

}

