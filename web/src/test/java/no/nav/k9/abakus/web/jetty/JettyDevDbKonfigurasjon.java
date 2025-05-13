package no.nav.k9.abakus.web.jetty;

public class JettyDevDbKonfigurasjon {

    private String datasource = "defaultDS";
    private String url = "jdbc:postgresql://127.0.0.1:5432/k9abakus?reWriteBatchedInserts=true";
    private String user = "k9abakus";
    private String password = user;

    JettyDevDbKonfigurasjon() {
    }

    public String getDatasource() {
        return datasource;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

}
