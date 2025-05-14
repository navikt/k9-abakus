package no.nav.k9.abakus.web.app.exceptions;

import static java.util.Collections.emptyList;
import static no.nav.k9.abakus.web.app.exceptions.FeilType.GENERELL_FEIL;

import java.util.Collection;

public record FeilDto(FeilType type, String feilmelding, Collection<FeltFeilDto> feltFeil) {

    public FeilDto(String feilmelding, FeilType type) {
        this(type, feilmelding, emptyList());
    }

    public FeilDto(FeilType type, String feilmelding) {
        this(type, feilmelding, emptyList());
    }

    public FeilDto(String feilmelding, Collection<FeltFeilDto> feltFeil) {
        this(GENERELL_FEIL, feilmelding, feltFeil);
    }

    public FeilDto(String feilmelding) {
        this(GENERELL_FEIL, feilmelding, emptyList());
    }

}
