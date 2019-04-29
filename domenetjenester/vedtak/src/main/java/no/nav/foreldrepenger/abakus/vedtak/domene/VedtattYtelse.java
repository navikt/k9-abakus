package no.nav.foreldrepenger.abakus.vedtak.domene;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

import no.nav.foreldrepenger.abakus.kodeverk.RelatertYtelseTilstand;
import no.nav.foreldrepenger.abakus.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.abakus.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Fagsystem;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface VedtattYtelse {

    AktørId getAktør();

    RelatertYtelseType getYtelseType();

    TemaUnderkategori getBehandlingsTema();

    RelatertYtelseTilstand getStatus();

    DatoIntervallEntitet getPeriode();

    Saksnummer getSaksnummer();

    UUID getVedtakReferanse();

    Fagsystem getKilde();

    Collection<YtelseAnvist> getYtelseAnvist();

    LocalDateTime getVedtattTidspunkt();
}
