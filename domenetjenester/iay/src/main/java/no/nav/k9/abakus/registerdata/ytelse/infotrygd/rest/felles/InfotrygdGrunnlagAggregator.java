package no.nav.k9.abakus.registerdata.ytelse.infotrygd.rest.felles;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.k9.abakus.registerdata.ytelse.infotrygd.InfotrygdGrunnlag;
import no.nav.k9.abakus.registerdata.ytelse.infotrygd.rest.sp.dto.grunnlag.request.GrunnlagRequest;
import no.nav.k9.abakus.registerdata.ytelse.infotrygd.rest.sp.dto.grunnlag.respons.Grunnlag;
import no.nav.k9.felles.konfigurasjon.konfig.Tid;

@ApplicationScoped
public class InfotrygdGrunnlagAggregator {

    private List<InfotrygdGrunnlag> tjenester;

    InfotrygdGrunnlagAggregator() {
    }

    @Inject
    public InfotrygdGrunnlagAggregator(@Any Instance<InfotrygdGrunnlag> tjenester) {
        this.tjenester = tjenester.stream().collect(toList());
    }

    public List<Grunnlag> hentAggregertGrunnlag(String fnr, LocalDate fom, LocalDate tom) {
        var request = new GrunnlagRequest(fnr, fomEllerMin(fom), tomEllerMax(tom));
        return tjenester.stream().map(t -> t.hentGrunnlag(request)).flatMap(List::stream).collect(toList());
    }

    public List<Grunnlag> hentAggregertGrunnlagFailSoft(String fnr, LocalDate fom, LocalDate tom) {
        var request = new GrunnlagRequest(fnr, fomEllerMin(fom), tomEllerMax(tom));
        return tjenester.stream().map(t -> t.hentGrunnlagFailSoft(request)).flatMap(List::stream).collect(toList());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[tjenester=" + tjenester + "]";
    }

    public static LocalDate fomEllerMin(LocalDate fom) {
        return fom != null ? fom : Tid.TIDENES_BEGYNNELSE;
    }

    public static LocalDate tomEllerMax(LocalDate tom) {
        return tom != null ? tom : Tid.TIDENES_ENDE;
    }


}
