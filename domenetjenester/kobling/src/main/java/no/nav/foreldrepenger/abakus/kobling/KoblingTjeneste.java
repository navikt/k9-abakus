package no.nav.foreldrepenger.abakus.kobling;

import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.kobling.repository.KoblingRepository;

@ApplicationScoped
public class KoblingTjeneste {

    private KoblingRepository repository;

    KoblingTjeneste() {
    }

    @Inject
    public KoblingTjeneste(KoblingRepository repository) {
        this.repository = repository;
    }

    public Optional<Kobling> hentFor(UUID referanse) {
        return repository.hentForReferanse(referanse);
    }

    public void lagre(Kobling kobling) {
        repository.lagre(kobling);
    }

    public Kobling hent(Long koblingId) {
        return repository.hent(koblingId);
    }
}
