package no.nav.k9.abakus.vedtak.domene;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.hibernate.jpa.HibernateHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.k9.abakus.felles.jpa.IntervallEntitet;
import no.nav.k9.abakus.typer.AktørId;
import no.nav.k9.abakus.typer.Saksnummer;
import no.nav.k9.felles.jpa.HibernateVerktøy;

@ApplicationScoped
public class VedtakYtelseRepository {

    private static final Logger LOG = LoggerFactory.getLogger(VedtakYtelseRepository.class);
    private EntityManager entityManager;

    VedtakYtelseRepository() {
        // CDI
    }

    @Inject
    public VedtakYtelseRepository(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager");
        this.entityManager = entityManager;
    }

    public VedtakYtelseBuilder opprettBuilderFor(AktørId aktørId, Saksnummer saksnummer, Fagsystem fagsystem, YtelseType ytelseType) {
        return VedtakYtelseBuilder.oppdatere(hentYtelseFor(aktørId, saksnummer, fagsystem, ytelseType))
            .medAktør(aktørId)
            .medSaksnummer(saksnummer)
            .medKilde(fagsystem)
            .medYtelseType(ytelseType);
    }

    public void lagre(VedtakYtelseBuilder builder) {
        var ytelse = builder.build();
        Optional<VedtakYtelse> vedtakYtelse = hentYtelseFor(ytelse.getAktør(), ytelse.getSaksnummer(), ytelse.getKilde(), ytelse.getYtelseType());
        if (vedtakYtelse.isPresent() && (builder.erOppdatering() || manglerEksisterendeVedtakAndeler(vedtakYtelse.get(), ytelse))) {
            // Deaktiver eksisterende innslag
            VedtakYtelse ytelseEntitet = vedtakYtelse.get();
            ytelseEntitet.setAktiv(false);
            entityManager.persist(ytelseEntitet);
            entityManager.flush();
        } else if (!builder.erOppdatering()) {
            ytelse.setAktiv(false);
        }
        if (ytelse.getAktiv()) {
            entityManager.persist(ytelse);
            for (YtelseAnvist ytelseAnvist : ytelse.getYtelseAnvist()) {
                entityManager.persist(ytelseAnvist);
                ytelseAnvist.getAndeler().forEach(entityManager::persist);
            }
            entityManager.flush();
        } else {
            LOG.info("Forkaster vedtak siden en sitter på nyere vedtak. {} er eldre enn {}", ytelse, vedtakYtelse);
        }
    }

    // Lagrer vedtak med samme vedtakstidspunkt dersom ny har andeler og gammel mangler andeler
    private boolean manglerEksisterendeVedtakAndeler(VedtakYtelse eksisterende, VedtakYtelse ny) {
        var eksisterendeHarAndeler = eksisterende.getYtelseAnvist().stream().anyMatch(a -> !a.getAndeler().isEmpty());
        var nyHarAndeler = ny.getYtelseAnvist().stream().anyMatch(a -> !a.getAndeler().isEmpty());
        return eksisterende.getVedtattTidspunkt().equals(ny.getVedtattTidspunkt()) && nyHarAndeler && !eksisterendeHarAndeler;
    }

    private Optional<VedtakYtelse> hentYtelseFor(AktørId aktørId, Saksnummer saksnummer, Fagsystem fagsystem, YtelseType ytelseType) {
        Objects.requireNonNull(aktørId, "aktørId");
        Objects.requireNonNull(saksnummer, "saksnummer");
        Objects.requireNonNull(fagsystem, "fagsystem");
        Objects.requireNonNull(ytelseType, "ytelseType");

        TypedQuery<VedtakYtelse> query = entityManager.createQuery("""
            SELECT v FROM VedtakYtelseEntitet v
            WHERE v.aktørId = :aktørId
            AND v.saksnummer = :saksnummer
            AND v.kilde = :fagsystem
            AND v.ytelseType = :ytelse
            AND v.aktiv = true
            """, VedtakYtelse.class);
        query.setParameter("aktørId", aktørId);
        query.setParameter("saksnummer", saksnummer);
        query.setParameter("fagsystem", fagsystem);
        query.setParameter("ytelse", ytelseType);

        return HibernateVerktøy.hentUniktResultat(query);
    }

    public List<VedtakYtelse> hentYtelserForIPeriode(AktørId aktørId, LocalDate fom, LocalDate tom) {
        TypedQuery<VedtakYtelse> query = entityManager.createQuery("""
            SELECT v FROM VedtakYtelseEntitet v
            WHERE v.aktørId = :aktørId
            AND v.periode.fomDato <= :tom AND v.periode.tomDato >= :fom
            AND v.aktiv = true
            """, VedtakYtelse.class);
        query.setParameter("aktørId", aktørId);
        query.setParameter("fom", fom);
        query.setParameter("tom", tom);
        query.setHint(HibernateHints.HINT_READ_ONLY, true);

        return new ArrayList<>(query.getResultList());
    }

    public List<VedtakYtelse> hentYtelserForIPeriode(AktørId aktørId, IntervallEntitet periode) {
        return hentYtelserForIPeriode(aktørId, periode.getFomDato(), periode.getTomDato());
    }
}
