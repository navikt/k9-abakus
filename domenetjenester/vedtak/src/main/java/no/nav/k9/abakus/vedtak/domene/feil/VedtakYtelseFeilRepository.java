package no.nav.k9.abakus.vedtak.domene.feil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Query;

import jakarta.persistence.Tuple;

import org.hibernate.jpa.HibernateHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import no.nav.k9.abakus.vedtak.domene.VedtakYtelse;

@ApplicationScoped
public class VedtakYtelseFeilRepository {

    private static final Logger LOG = LoggerFactory.getLogger(VedtakYtelseFeilRepository.class);
    private EntityManager entityManager;

    VedtakYtelseFeilRepository() {
        // CDI
    }

    @Inject
    public VedtakYtelseFeilRepository(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager");
        this.entityManager = entityManager;
    }



    public void lagre(VedtakYtelseFeilDump vedtakYtelse) {
        entityManager.persist(vedtakYtelse);
        entityManager.flush();
    }

    public List<VedtakYtelse> hentVedtakMedFeil() {
        Query query = entityManager.createNativeQuery("""
            SELECT v FROM VEDTAK_YTELSE v
            inner join VE_YTELSE_ANVIST va on v.id = va.ytelse_id
            WHERE va.utbetalingsgrad_prosent = 0 and va.dagsats > 0
            AND v.aktiv = 'J' and v.ytelse_type in ('PSB', 'PPN')
            """, VedtakYtelse.class);
        query.setHint(HibernateHints.HINT_READ_ONLY, true);

        return new ArrayList<>(query.getResultList());
    }

    public List<Tuple> hentDump() {
        Query query = entityManager.createNativeQuery("""
            SELECT v FROM VEDTAK_YTELSE_FEIL_DUMP v
            """, Tuple.class);
        query.setHint(HibernateHints.HINT_READ_ONLY, true);

        return new ArrayList<>(query.getResultList());
    }

}
