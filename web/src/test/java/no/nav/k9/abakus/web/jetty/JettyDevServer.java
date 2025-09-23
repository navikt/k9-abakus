package no.nav.k9.abakus.web.jetty;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.k9.abakus.web.jetty.db.EnvironmentClass;

public class JettyDevServer extends JettyServer {

    private static final Logger log = LoggerFactory.getLogger(JettyDevServer.class);

    public static void main(String[] args) throws Exception {
        JettyDevServer devServer = new JettyDevServer();
        Server server = devServer.bootStrap();
        server.join();
    }

    public JettyDevServer() {
        super(new JettyDevKonfigurasjon());
    }

    @Override
    protected EnvironmentClass getEnvironmentClass() {
        return EnvironmentClass.LOCALHOST;
    }


    @Override
    protected void konfigurerMiljø() {
        System.setProperty("develop-local", "true");
        PropertiesUtils.initProperties();

        JettyDevDbKonfigurasjon konfig = new JettyDevDbKonfigurasjon();
        System.setProperty("defaultDS.url", konfig.getUrl());
        System.setProperty("defaultDS.username", konfig.getUser()); // benyttes kun hvis vault.enable=false
        System.setProperty("defaultDS.password", konfig.getPassword()); // benyttes kun hvis vault.enable=false

        System.setProperty("task.manager.runner.threads", "3");
        System.setProperty("task.manager.tasks.queue.size", "20");
        System.setProperty("task.manager.polling.tasks.size", "10");
        System.setProperty("task.manager.polling.scrolling.select.size", "10");
    }

    @Override
    protected void konfigurerSikkerhet() {
        super.konfigurerSikkerhet();
        // truststore avgjør hva vi stoler på av sertifikater når vi gjør utadgående TLS
        // kall
        initCryptoStoreConfig("truststore", "javax.net.ssl.trustStore", "javax.net.ssl.trustStorePassword",
            "changeit");

    }

    private static String initCryptoStoreConfig(String storeName, String storeProperty, String storePasswordProperty,
                                                String defaultPassword) {
        String defaultLocation = getProperty("user.home", ".") + "/.modig/" + storeName + ".jks";

        String storePath = getProperty(storeProperty, defaultLocation);
        File storeFile = new File(storePath);
        if (!storeFile.exists()) {
            throw new IllegalStateException("Finner ikke " + storeName + " i " + storePath
                + "\n\tKonfigurer enten som System property \'" + storeProperty + "\' eller environment variabel \'"
                + storeProperty.toUpperCase().replace('.', '_') + "\'");
        }
        String password = getProperty(storePasswordProperty, defaultPassword);
        if (password == null) {
            throw new IllegalStateException(
                "Passord for å aksessere store " + storeName + " i " + storePath + " er null");
        }

        System.setProperty(storeProperty, storeFile.getAbsolutePath());
        System.setProperty(storePasswordProperty, password);
        return storePath;
    }

    private static String getProperty(String key, String defaultValue) {
        String val = System.getProperty(key, defaultValue);
        if (val == null) {
            val = System.getenv(key.toUpperCase().replace('.', '_'));
            val = val == null ? defaultValue : val;
        }
        return val;
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
