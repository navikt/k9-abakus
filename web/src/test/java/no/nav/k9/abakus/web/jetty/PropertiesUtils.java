package no.nav.k9.abakus.web.jetty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesUtils.class);

    private static String VTP_FILNAVN_LOCAL = "application-vtp.properties";

    private PropertiesUtils() {
    }

    static void initProperties() {
        loadPropertyFile(new File(VTP_FILNAVN_LOCAL));
    }

    private static void loadPropertyFile(File devFil) {
        if (devFil.exists()) {
            Properties prop = new Properties();
            try (InputStream inputStream = new FileInputStream(devFil)) {
                prop.load(inputStream);
            } catch (IOException e) {
                LOGGER.error("Kunne ikke finne properties-fil", e);
            }
            System.getProperties().putAll(prop);
        } else {
            LOGGER.warn("Fant ikke [{}], laster ikke properites derfra ", devFil);
        }
    }
}
