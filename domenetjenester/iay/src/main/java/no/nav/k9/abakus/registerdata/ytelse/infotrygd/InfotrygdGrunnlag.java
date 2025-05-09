package no.nav.k9.abakus.registerdata.ytelse.infotrygd;

import no.nav.k9.abakus.registerdata.ytelse.infotrygd.rest.sp.dto.grunnlag.request.GrunnlagRequest;
import no.nav.k9.abakus.registerdata.ytelse.infotrygd.rest.sp.dto.grunnlag.respons.Grunnlag;

import java.util.List;

public interface InfotrygdGrunnlag {
    List<Grunnlag> hentGrunnlag(GrunnlagRequest request);

    List<Grunnlag> hentGrunnlagFailSoft(GrunnlagRequest request);
}
