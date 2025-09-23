package no.nav.k9.abakus.web.jetty.abac;

import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.k9.felles.sikkerhet.abac.PdpRequest;
import no.nav.k9.felles.sikkerhet.abac.PdpRequestMedBerørtePersonerForAuditlogg;

import java.util.Set;
import java.util.stream.Collectors;

public class AbakusPdpRequest extends PdpRequestMedBerørtePersonerForAuditlogg {

    private Set<YtelseType> ytelseTyper;

    public Set<YtelseType> getYtelseTyper() {
        return ytelseTyper;
    }

    public void setYtelseTyper(Set<YtelseType> ytelseTyper) {
        this.ytelseTyper = ytelseTyper;
    }

    public void setYtelseTypeKoder(Set<String> ytelseTypeKoder) {
        this.ytelseTyper =ytelseTypeKoder.stream()
            .map(YtelseType::fraKode)
            .collect(Collectors.toSet());
    }
}
