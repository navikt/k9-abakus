package no.nav.foreldrepenger.abakus.typer;

import static no.nav.vedtak.util.Objects.check;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.diff.IndexKey;
import no.nav.foreldrepenger.abakus.diff.TraverseValue;

/**
 * Stillingsprosent slik det er oppgitt i arbeidsavtalen
 */
@Embeddable
public class Stillingsprosent implements Serializable, IndexKey, TraverseValue {
    private static final Logger log = LoggerFactory.getLogger(Stillingsprosent.class);

    private static final RoundingMode AVRUNDINGSMODUS = RoundingMode.HALF_EVEN;

    @Column(name = "verdi", scale = 2, nullable = false)
    @ChangeTracked
    private BigDecimal verdi;

    protected Stillingsprosent() {
        // for hibernate
    }

    public Stillingsprosent(BigDecimal verdi) {
        this.verdi = verdi == null ? null : fiksNegativTilAbsolutt(verdi);
        validerRange(this.verdi);
    }

    // Beleilig å kunne opprette gjennom int
    public Stillingsprosent(Integer verdi) {
        this(new BigDecimal(verdi));
    }

    // Beleilig å kunne opprette gjennom string
    public Stillingsprosent(String verdi) {
        this(new BigDecimal(verdi));
    }

    @Override
    public String getIndexKey() {
        return skalertVerdi().toString();
    }

    public BigDecimal getVerdi() {
        return verdi;
    }

    private BigDecimal skalertVerdi() {
        return verdi.setScale(2, AVRUNDINGSMODUS);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        Stillingsprosent other = (Stillingsprosent) obj;
        return Objects.equals(skalertVerdi(), other.skalertVerdi());
    }

    @Override
    public int hashCode() {
        return Objects.hash(skalertVerdi());
    }

    @Override
    public String toString() {
        return "Stillingsprosent{" +
            "verdi=" + verdi +
            ", skalertVerdi=" + skalertVerdi() +
            '}';
    }

    private static void validerRange(BigDecimal verdi) {
        if (verdi == null) {
            return;
        } else if (verdi.compareTo(BigDecimal.valueOf(100)) > 0) {
            log.info("[IAY] Prosent (yrkesaktivitet, permisjon) kan ikke være større enn 100. Verdi fra AA-reg: {}", verdi);
        }
        check(verdi.compareTo(BigDecimal.ZERO) >= 0, "Prosent må være >= 0"); //$NON-NLS-1$
        check(verdi.compareTo(BigDecimal.valueOf(500)) <= 0, "Prosent må være <= 500"); //$NON-NLS-1$
    }

    private BigDecimal fiksNegativTilAbsolutt(BigDecimal verdi) {
        if (null != verdi && verdi.compareTo(BigDecimal.ZERO) < 0) {
            log.info("[IAY] Prosent (yrkesaktivitet, permisjon) kan ikke være mindre enn 0, absolutt verdi brukes isteden. Verdi fra AA-reg: {}", verdi);
            verdi = verdi.abs();
        }
        return verdi;
    }

    public boolean erNulltall() {
        return verdi != null && verdi.intValue() == 0;
    }
}
