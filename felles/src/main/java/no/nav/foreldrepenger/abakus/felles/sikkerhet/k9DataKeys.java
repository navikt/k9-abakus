package no.nav.foreldrepenger.abakus.felles.sikkerhet;

import no.nav.vedtak.sikkerhet.abac.pdp.RessursDataKey;

enum k9DataKeys implements RessursDataKey {

    BEHANDLING_STATUS("no.nav.abac.attributter.resource.k9.sak.behandlingsstatus"),
    FAGSAK_STATUS("no.nav.abac.attributter.resource.k9.sak.saksstatus"),
    ;

    private final String key;

    k9DataKeys(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
