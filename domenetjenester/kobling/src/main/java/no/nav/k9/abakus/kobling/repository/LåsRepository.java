package no.nav.k9.abakus.kobling.repository;

import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import no.nav.k9.abakus.kobling.Kobling;
import no.nav.k9.abakus.kobling.KoblingLås;
import no.nav.k9.felles.exception.TekniskException;

@ApplicationScoped
public class LåsRepository {
    private EntityManager entityManager;

    LåsRepository() {
        // CDI
    }

    @Inject
    public LåsRepository(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager");
        this.entityManager = entityManager;
    }

    /**
     * Initialiser lås og ta lock på tilhørende database rader.
     */
    public KoblingLås taLås(Long koblingId) {
        if (koblingId != null) {
            final LockModeType lockModeType = LockModeType.PESSIMISTIC_WRITE;
            lås(koblingId, lockModeType);
            KoblingLås lås = new KoblingLås(koblingId);
            return lås;
        } else {
            return new KoblingLås(null);
        }

    }

    private Long lås(final Long behandlingId, LockModeType lockModeType) {
        Object[] result = (Object[]) entityManager.createQuery("select k.id, k.versjon from Kobling k where k.id=:id and k.aktiv=true")
            .setParameter("id", behandlingId)
            .setLockMode(lockModeType).getSingleResult();
        return (Long) result[0];
    }

    /**
     * Verifiser lås ved å sjekke mot underliggende lager.
     */
    public void oppdaterLåsVersjon(KoblingLås lås) {
        if (lås.getKoblingId() != null) {
            verifisertLås(lås.getKoblingId());
        } // else NO-OP (for ny behandling uten id)
    }

    private Object verifisertLås(Long id) {
        LockModeType lockMode = LockModeType.PESSIMISTIC_FORCE_INCREMENT;
        Object entity = entityManager.find(Kobling.class, id);
        if (entity == null) {
            throw new TekniskException("FP-131239", String.format("Fant ikke entitet for låsing [%s], id=%s.", Kobling.class.getSimpleName(), id));
        } else {
            entityManager.lock(entity, lockMode);
        }
        return entity;
    }

}
