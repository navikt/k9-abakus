package no.nav.k9.abakus.jetty;


import static no.nav.k9.felles.konfigurasjon.env.Cluster.NAIS_CLUSTER_NAME;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jetty.ee9.security.ConstraintSecurityHandler;
import org.eclipse.jetty.ee9.security.jaspi.DefaultAuthConfigFactory;
import org.eclipse.jetty.ee9.security.jaspi.JaspiAuthenticatorFactory;
import org.eclipse.jetty.ee9.security.jaspi.provider.JaspiAuthConfigProvider;
import org.eclipse.jetty.ee9.webapp.MetaData;
import org.eclipse.jetty.ee9.webapp.MetaInfConfiguration;
import org.eclipse.jetty.ee9.webapp.WebAppContext;
import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.jaas.JAASLoginService;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.zaxxer.hikari.HikariDataSource;

import jakarta.security.auth.message.config.AuthConfigFactory;
import no.nav.k9.abakus.app.ApplicationConfig;
import no.nav.k9.abakus.jetty.AppKonfigurasjon;
import no.nav.k9.abakus.jetty.EnvironmentUtil;
import no.nav.k9.abakus.jetty.JettyWebKonfigurasjon;
import no.nav.k9.abakus.jetty.db.DatabaseScript;
import no.nav.k9.abakus.jetty.db.DatasourceRole;
import no.nav.k9.abakus.jetty.db.DatasourceUtil;
import no.nav.k9.abakus.jetty.db.EnvironmentClass;
import no.nav.k9.felles.konfigurasjon.env.Environment;
import no.nav.k9.felles.oidc.OidcApplication;
import no.nav.k9.felles.sikkerhet.jaspic.OidcAuthModule;

public class JettyServer {

    private static final Logger log = LoggerFactory.getLogger(JettyServer.class);
    private static final Environment ENV = Environment.current();
    private AppKonfigurasjon appKonfigurasjon;

    public JettyServer() {
        this(new JettyWebKonfigurasjon());
    }

    public JettyServer(int serverPort) {
        this(new JettyWebKonfigurasjon(serverPort));
    }

    protected JettyServer(AppKonfigurasjon appKonfigurasjon) {
        this.appKonfigurasjon = appKonfigurasjon;
    }

    public static void main(String[] args) throws Exception {
        System.setProperty(NAIS_CLUSTER_NAME, ENV.clusterName());
        JettyServer jettyServer;
        if (args.length > 0) {
            int serverPort = Integer.parseUnsignedInt(args[0]);
            jettyServer = new JettyServer(serverPort);
        } else {
            final String portString = System.getenv("PORT");
            if (portString != null && !portString.trim().equals("")) {
                jettyServer = new JettyServer(Integer.parseInt(portString));
            } else {
                jettyServer = new JettyServer();
            }
        }

        Server server = jettyServer.bootStrap();
        server.join();
    }

    public Server bootStrap() throws Exception {
        konfigurerMiljø();
        migrerDatabaser();
        konfigurer();

        return startServer(appKonfigurasjon);
    }

    private Server startServer(AppKonfigurasjon appKonfigurasjon) throws Exception {
        // https://jetty.org/docs/jetty/12/programming-guide/arch/threads.html
        QueuedThreadPool threadPool = new QueuedThreadPool();

        Server server = new Server(threadPool);

        server.setConnectors(createConnectors(appKonfigurasjon, server).toArray(new Connector[]{}));
        WebAppContext webAppContext = createContext(appKonfigurasjon, server);
        server.setHandler(new Handler.Sequence(
            new ClearMdcHandler(),
            webAppContext.get()
        ));
        server.start();
        return server;
    }

    protected void konfigurer() throws Exception {
        konfigurerSikkerhet();
        konfigurerJndi();
    }

    protected void konfigurerSikkerhet() {
        var factory = new DefaultAuthConfigFactory();

        factory.registerConfigProvider(new JaspiAuthConfigProvider(new OidcAuthModule()),
            "HttpServlet",
            "server /k9/oppdrag",
            "OIDC Authentication");

        AuthConfigFactory.setFactory(factory);
    }

    protected void konfigurerMiljø() {
        // må være << antall db connectdions. Summen av runner threads + kall fra ulike
        // løsninger bør ikke overgå antall conns (vi isåfall kunne
        // medføre connection timeouts)
        System.setProperty("task.manager.runner.threads", "3");

        //øker kø-størrelse og antall task som polles om gangen, gjør at systemet oppnår bedre ytelse når det finnes mange klare tasks
        System.setProperty("task.manager.tasks.queue.size", "20");
        System.setProperty("task.manager.polling.tasks.size", "10");
        System.setProperty("task.manager.polling.scrolling.select.size", "10");
    }

    protected void migrerDatabaser() throws IOException {
        EnvironmentClass environmentClass = getEnvironmentClass();
        String initSql = String.format("SET ROLE \"%s\"", DatasourceUtil.getDbRole("defaultDS", DatasourceRole.ADMIN));
        if (EnvironmentClass.LOCALHOST.equals(environmentClass)) {
            // TODO: Ønsker egentlig ikke dette, men har ikke satt opp skjema lokalt
            // til å ha en admin bruker som gjør migrering og en annen som gjør CRUD
            // operasjoner
            initSql = null;
        }
        try (HikariDataSource migreringDs = DatasourceUtil.createDatasource("defaultDS", DatasourceRole.ADMIN, environmentClass, 2)) {
            DatabaseScript.migrate(migreringDs, initSql);
        }
    }

    protected void konfigurerJndi() throws Exception {
        new EnvEntry("jdbc/defaultDS",
            DatasourceUtil.createDatasource("defaultDS", DatasourceRole.USER, getEnvironmentClass(), 8));

    }


    protected EnvironmentClass getEnvironmentClass() {
        return EnvironmentUtil.getEnvironmentClass();
    }

    @SuppressWarnings("resource")
    protected List<Connector> createConnectors(AppKonfigurasjon appKonfigurasjon, Server server) {
        List<Connector> connectors = new ArrayList<>();
        ServerConnector httpConnector = new ServerConnector(server,
            new HttpConnectionFactory(createHttpConfiguration()));
        httpConnector.setPort(appKonfigurasjon.getServerPort());
        connectors.add(httpConnector);

        return connectors;
    }

    @SuppressWarnings("resource")
    protected WebAppContext createContext(AppKonfigurasjon appKonfigurasjon, Server server) throws IOException {
        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setParentLoaderPriority(true);

        // må hoppe litt bukk for å hente web.xml fra classpath i stedet for fra
        // filsystem.
        String descriptor = ResourceFactory.of(server).newClassLoaderResource("/WEB-INF/web.xml").getURI().toURL().toExternalForm();
        webAppContext.setDescriptor(descriptor);
        webAppContext.setBaseResource(createResourceCollection(server));
        webAppContext.setContextPath(appKonfigurasjon.getContextPath());

        /*
         * lar jetty scanne flere jars for web resources (eks. WebFilter/WebListener
         * annotations),
         * men bare de som matchr pattern for raskere oppstart
         */
        webAppContext.setAttribute(MetaInfConfiguration.WEBINF_JAR_PATTERN, "^.*jersey-.*.jar$|^.*felles-sikkerhet.*.jar$");

        webAppContext.setSecurityHandler(createSecurityHandler());
        updateMetaData(server, webAppContext.getMetaData());
        webAppContext.setThrowUnavailableOnStartupException(true);
        return webAppContext;
    }

    protected HttpConfiguration createHttpConfiguration() {
        // Create HTTP Config
        HttpConfiguration httpConfig = new HttpConfiguration();

        // Add support for X-Forwarded headers
        httpConfig.addCustomizer(new org.eclipse.jetty.server.ForwardedRequestCustomizer());

        return httpConfig;
    }

    private org.eclipse.jetty.ee9.security.SecurityHandler createSecurityHandler() {
        ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        securityHandler.setAuthenticatorFactory(new JaspiAuthenticatorFactory());

        JAASLoginService loginService = new JAASLoginService();
        loginService.setName("jetty-login");
        loginService.setLoginModuleName("jetty-login");
        loginService.setIdentityService(new DefaultIdentityService());
        securityHandler.setLoginService(loginService);

        return securityHandler;
    }

    private void updateMetaData(Server server, MetaData metaData) {
        // Find path to class-files while starting jetty from development environment.
        List<Class<?>> appClasses = getApplicationClasses();

        List<Resource> resources = appClasses.stream()
            .map(c ->ResourceFactory.of(server).newResource(c.getProtectionDomain().getCodeSource().getLocation()))
            .collect(Collectors.toList());

        metaData.setWebInfClassesResources(resources);
    }

    protected List<Class<?>> getApplicationClasses() {
        return Arrays.asList(ApplicationConfig.class, OidcApplication.class);
    }

    protected Resource createResourceCollection(Server server) {
        return ResourceFactory.combine(
            //ResourceFactory.of(server).newClassLoaderResource("META-INF/resources/webjars/"),
            ResourceFactory.of(server).newClassLoaderResource("web"));
    }

    /**
     * brukes for å slette tilstand i MDC på starten av en request
     */
    private static class ClearMdcHandler extends Handler.Abstract{
        @Override
        public boolean handle(Request request, Response response, Callback callback) {
            MDC.clear();
            return false;
        }
    }

}
