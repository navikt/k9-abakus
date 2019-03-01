package no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.abakus.kodeverk.Kodeliste;

@Entity(name = "UtsettelseÅrsak")
@DiscriminatorValue(UtsettelseÅrsak.DISCRIMINATOR)
public class UtsettelseÅrsak extends Kodeliste {
    public static final String DISCRIMINATOR = "UTSETTELSE_AARSAK_TYPE";

    public static final UtsettelseÅrsak ARBEID = new UtsettelseÅrsak("ARBEID");
    public static final UtsettelseÅrsak FERIE = new UtsettelseÅrsak("LOVBESTEMT_FERIE");
    public static final UtsettelseÅrsak SYKDOM = new UtsettelseÅrsak("SYKDOM");
    public static final UtsettelseÅrsak INSTITUSJON_SØKER = new UtsettelseÅrsak("INSTITUSJONSOPPHOLD_SØKER");
    public static final UtsettelseÅrsak INSTITUSJON_BARN = new UtsettelseÅrsak("INSTITUSJONSOPPHOLD_BARNET");
    public static final UtsettelseÅrsak UDEFINERT = new UtsettelseÅrsak("-");

    UtsettelseÅrsak(String kode) {
        super(kode, DISCRIMINATOR);
    }

    UtsettelseÅrsak() {
    }
}
