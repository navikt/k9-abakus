package no.nav.k9.abakus.web.jetty;

import no.nav.k9.abakus.web.jetty.JettyWebKonfigurasjon;

public class JettyDevKonfigurasjon extends JettyWebKonfigurasjon {
    private static final int SSL_SERVER_PORT = 8915;
    private static int DEFAULT_DEV_SERVER_PORT = 8015;

    JettyDevKonfigurasjon(){
        super(DEFAULT_DEV_SERVER_PORT);
    }

    @Override
    public int getSslPort() {
        return SSL_SERVER_PORT;
    }
}
