package no.nav.foreldrepenger.abakus.registerdata.tjeneste.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class ReferanseDto {

    @NotNull
    @Pattern(regexp = "[-|\\w|\\d]*")
    private String referanse;

    public ReferanseDto(String referanse) {
        this.referanse = referanse;
    }

    public ReferanseDto() {
    }

    public String getReferanse() {
        return referanse;
    }

    public void setReferanse(String referanse) {
        this.referanse = referanse;
    }
}
