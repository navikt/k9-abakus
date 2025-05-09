package no.nav.k9.abakus.jetty;

import no.nav.k9.abakus.jetty.db.EnvironmentClass;
import no.nav.k9.felles.konfigurasjon.env.Environment;

public final class EnvironmentUtil {
    private EnvironmentUtil() {
    }

    public static EnvironmentClass getEnvironmentClass() {
        String cluster = Environment.current().getProperty("nais.cluster.name");
        if (cluster != null) {
            cluster = cluster.substring(0, cluster.indexOf('-')).toUpperCase();
            if ("DEV".equalsIgnoreCase(cluster)) {
                return EnvironmentClass.PREPROD;
            }
            return EnvironmentClass.valueOf(cluster);
        }
        return EnvironmentClass.PROD;
    }
}
