package no.nav.k9.abakus.web.jetty.db;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.ClassicConfiguration;

import javax.sql.DataSource;

public final class DatabaseScript {

    private static final String location = "classpath:/db/postgres/";

    public static void migrate(final DataSource dataSource, String initSql) {
        ClassicConfiguration conf = new ClassicConfiguration();
        conf.setDataSource(dataSource);
        conf.setLocationsAsStrings(location);
        conf.setBaselineOnMigrate(true);
        if (initSql != null) {
            conf.setInitSql(initSql);
        }
        Flyway flyway = new Flyway(conf);
        try {
            flyway.migrate();
        } catch (FlywayException fwe) {
            throw new IllegalStateException("Migrering feiler", fwe);
        }
    }
}
