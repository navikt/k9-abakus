package no.nav.k9.abakus.web.jetty.db;
public enum EnvironmentClass {
    LOCALHOST, PREPROD, PROD;

    public String mountPath() {
        return "postgresql/" + name().toLowerCase() + "-fss";
    }
}
