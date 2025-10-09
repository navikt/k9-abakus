package no.nav.k9.abakus.registerdata.inntekt;

import java.util.concurrent.StructuredTaskScope;

import org.slf4j.MDC;

import io.opentelemetry.context.Context;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.k9.felles.sikkerhet.loginmodule.ContainerLogin;
import no.nav.k9.sikkerhet.oidc.token.impl.ContextTokenProvider;

@Dependent
public class SystemuserThreadLogin {

    private final ContextTokenProvider tokenProvider;
    private final String clientScope;
    private final ThreadLocal<ContainerLogin> threadContainerLogin = new ThreadLocal<>();

    @Inject
    public SystemuserThreadLogin(ContextTokenProvider tokenProvider, @KonfigVerdi(value = "CLIENT_SCOPE", required = false) String clientScope) {
        this.tokenProvider = tokenProvider;
        this.clientScope = clientScope;
    }

    protected void login() {
        if (threadContainerLogin.get() != null) {
            throw new IllegalStateException("Allerede innlogget i denne tråden");
        }
        ContainerLogin containerLogin = new ContainerLogin(tokenProvider, clientScope);
        containerLogin.login();
        threadContainerLogin.set(containerLogin);
    }

    protected void logout() {
        threadContainerLogin.get().logout();
        threadContainerLogin.remove();
    }

    public StructuredTaskScope.Subtask<Object> fork(StructuredTaskScope<Object, Void> scope, Runnable task) {
        //MDC er for å få med logg kontekst (saksnummer etc)
        //wrap er for å ta med opentelemetry context
        //login er for å kjøre som systembruker

        String parentMdcContext = MDC.get("prosess");
        Runnable wrappedLoggedInTask = Context.current().wrap(() -> {
            MDC.put("prosess", parentMdcContext);
            login();
            task.run();
            logout();
        });
        return scope.fork(wrappedLoggedInTask);

    }
}
