package no.nav.k9.abakus.iay;

import no.nav.k9.abakus.domene.iay.GrunnlagReferanse;
import no.nav.k9.abakus.domene.iay.InntektArbeidYtelseRepository;
import no.nav.k9.abakus.domene.iay.søknad.OppgittOpptjeningBuilder;
import no.nav.k9.abakus.kobling.KoblingReferanse;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class OppgittOpptjeningTjeneste {

    private InntektArbeidYtelseRepository repository;

    public OppgittOpptjeningTjeneste() {
    }

    @Inject
    public OppgittOpptjeningTjeneste(InntektArbeidYtelseRepository repository) {
        this.repository = repository;
    }

    public GrunnlagReferanse lagre(KoblingReferanse koblingReferanse, OppgittOpptjeningBuilder builder) {
        return repository.lagre(koblingReferanse, builder);
    }

    public GrunnlagReferanse lagreOgNullstillOverstyring(KoblingReferanse koblingReferanse, OppgittOpptjeningBuilder builder) {
        return repository.lagreOgNullstillOverstyring(koblingReferanse, builder);
    }

    public GrunnlagReferanse lagreOverstyring(KoblingReferanse koblingReferanse, OppgittOpptjeningBuilder builder) {
        return repository.lagreOverstyring(koblingReferanse, builder);
    }

    public GrunnlagReferanse lagrePrJournalpostId(KoblingReferanse koblingReferanse, OppgittOpptjeningBuilder builder) {
        return repository.lagrePrJournalpostId(koblingReferanse, builder);
    }
}
