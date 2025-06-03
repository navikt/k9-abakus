package no.nav.k9.abakus.felles.sikkerhet;

import no.nav.k9.felles.sikkerhet.abac.AbacAttributtType;

public enum AbakusAbacAttributtType implements AbacAttributtType {
    YTELSETYPE("ytelsetype", true);

    private final String sporingsloggEksternKode;
    private final boolean maskerOutput;

    AbakusAbacAttributtType() {
        this.sporingsloggEksternKode = null;
        this.maskerOutput = false;
    }

    AbakusAbacAttributtType(String sporingsloggEksternKode) {
        this.sporingsloggEksternKode = sporingsloggEksternKode;
        this.maskerOutput = false;
    }

    AbakusAbacAttributtType(String sporingsloggEksternKode, boolean maskerOutput) {
        this.sporingsloggEksternKode = sporingsloggEksternKode;
        this.maskerOutput = maskerOutput;
    }

    public String getSporingsloggKode() {
        return this.sporingsloggEksternKode;
    }

    public boolean getMaskerOutput() {
        return this.maskerOutput;
    }
}
