package no.nav.k9.abakus.web.app.vedlikehold;

class BegrunnelseVasker {

    private BegrunnelseVasker() {
    }

    public static String vask(String uvasket) {
        return uvasket.replace("\n", "").replace("\r", "").replace("\u00a0", " ");
    }
}
