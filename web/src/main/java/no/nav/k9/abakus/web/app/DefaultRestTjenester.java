package no.nav.k9.abakus.web.app;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.k9.abakus.iay.tjeneste.ArbeidsforholdRestTjeneste;
import no.nav.k9.abakus.iay.tjeneste.GrunnlagRestTjeneste;
import no.nav.k9.abakus.iay.tjeneste.InntektsmeldingerRestTjeneste;
import no.nav.k9.abakus.iay.tjeneste.OppgittOpptjeningRestTjeneste;
import no.nav.k9.abakus.iay.tjeneste.OppgittOpptjeningV2RestTjeneste;
import no.nav.k9.abakus.registerdata.tjeneste.RegisterdataRestTjeneste;
import no.nav.k9.abakus.vedtak.tjeneste.YtelseRestTjeneste;
import no.nav.k9.abakus.web.app.diagnostikk.DiagnostikkRestTjeneste;
import no.nav.k9.abakus.web.app.diagnostikk.rapportering.RapporteringRestTjeneste;
import no.nav.k9.abakus.web.app.rest.ekstern.EksternDelingAvYtelserRestTjeneste;
import no.nav.k9.abakus.web.app.vedlikehold.ForvaltningRestTjeneste;
import no.nav.k9.prosesstask.rest.ProsessTaskRestTjeneste;

/**
 * Rest tjenester.
 * <p>
 * TODO: burde vært automatisk oppdaget av Jersey.
 */
@ApplicationScoped
class DefaultRestTjenester implements RestClasses {

    @Override
    public Set<Class<?>> getRestClasses() {
        Set<Class<?>> classes = new LinkedHashSet<>();
        classes.add(ArbeidsforholdRestTjeneste.class);
        classes.add(DiagnostikkRestTjeneste.class);
        classes.add(EksternDelingAvYtelserRestTjeneste.class);
        classes.add(ForvaltningRestTjeneste.class);
        classes.add(GrunnlagRestTjeneste.class);
        classes.add(InntektsmeldingerRestTjeneste.class);
        classes.add(OppgittOpptjeningRestTjeneste.class);
        classes.add(OppgittOpptjeningV2RestTjeneste.class);
        classes.add(ProsessTaskRestTjeneste.class);
        classes.add(RapporteringRestTjeneste.class);
        classes.add(RegisterdataRestTjeneste.class);
        classes.add(YtelseRestTjeneste.class);
        return Collections.unmodifiableSet(classes);
    }

}
