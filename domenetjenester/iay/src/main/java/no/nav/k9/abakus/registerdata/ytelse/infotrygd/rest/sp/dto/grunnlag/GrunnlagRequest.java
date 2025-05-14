package no.nav.k9.abakus.registerdata.ytelse.infotrygd.rest.sp.dto.grunnlag.request;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public record GrunnlagRequest(List<String> fnr, LocalDate fom, LocalDate tom) {

    public GrunnlagRequest {
        if (fnr == null || fnr.isEmpty()) {
            throw new IllegalArgumentException("Ikke angitt fnr");
        }
        Objects.requireNonNull(fom);
        Objects.requireNonNull(tom);
    }

    public GrunnlagRequest(String fnr, LocalDate fom, LocalDate tom) {
        this(List.of(fnr), fom, tom);
    }

}
