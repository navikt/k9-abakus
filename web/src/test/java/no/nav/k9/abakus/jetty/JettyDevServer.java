package no.nav.k9.abakus.jetty;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.k9.felles.konfigurasjon.env.Environment;

import javax.sql.DataSource;

import java.io.IOException;
import java.sql.SQLException;

public class JettyDevServer extends JettyServer {

    private static final Environment ENV = Environment.current();
    private static final Logger log = LoggerFactory.getLogger(JettyDevServer.class);

    private JettyDevServer(int serverPort) {
        super(serverPort);
    }

    public static void main(String[] args) throws Exception {
        jettyServer(args).bootStrap();
    }

    static JettyDevServer jettyServer(String[] args) {
        if (args.length > 0) {
            return new JettyDevServer(Integer.parseUnsignedInt(args[0]));
        }
        return new JettyDevServer(ENV.getProperty("server.port", Integer.class, 8015));
    }

    @Override
    protected void migrerDatabaser() throws IOException {
        try {
            super.migrerDatabaser();
        } catch (IllegalStateException e) {
            log.info("Migreringer feilet, cleaner og prøver på nytt.");
            DataSource migreringDs = DatasourceUtil.createDatasource("defaultDS", DatasourceRole.ADMIN,
                getEnvironmentClass(), 1);
            try {
                DevDatabaseScript.clean(migreringDs);
            } finally {
                try {
                    migreringDs.getConnection().close();
                } catch (SQLException sqlException) {
                    log.warn("Klarte ikke stenge connection etter migrering", sqlException);
                }
            }
            super.migrerDatabaser();
        }
    }
}
