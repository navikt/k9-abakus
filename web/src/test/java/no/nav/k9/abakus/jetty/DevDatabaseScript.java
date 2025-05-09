package no.nav.k9.abakus.jetty;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.ClassicConfiguration;

import javax.sql.DataSource;

class DevDatabaseScript {
    private static final String location = "classpath:/db/postgres/";

    static void clean(DataSource dataSource) {
        ClassicConfiguration conf = new ClassicConfiguration();
        conf.setDataSource(dataSource);
        conf.setLocationsAsStrings(location);
        conf.setBaselineOnMigrate(true);
        Flyway flyway = new Flyway(conf);
        try {
            flyway.clean();
        } catch (FlywayException fwe) {
            throw new IllegalStateException("Migrering feiler", fwe);
        }
    }
}
