package no.nav.k9.abakus.registerdata.arbeidsforhold.rest;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriBuilderException;
import no.nav.k9.felles.integrasjon.rest.OidcRestClient;
import no.nav.k9.felles.integrasjon.rest.ScopedRestIntegration;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;

/*
 * Dokumentasjon https://confluence.adeo.no/display/FEL/AAREG+-+Tjeneste+REST+aareg.api
 * Swagger https://aareg-services-q2.dev.intern.nav.no/swagger-ui/index.html?urls.primaryName=aareg.api.v1#/arbeidstaker/finnArbeidsforholdPrArbeidstaker
 * Swagger V2 https://aareg-services-q2.dev.intern.nav.no/swagger-ui/index.html?urls.primaryName=aareg.api.v2#/arbeidstaker/finnArbeidsforholdPrArbeidstaker
 */

@ApplicationScoped
@ScopedRestIntegration(scopeKey = "aareg.scopes", defaultScope = "api://prod-fss.arbeidsforhold.aareg-services-nais/.default")
public class AaregRestKlient {

    private OidcRestClient oidcRestClient;
    private String url;

    AaregRestKlient() {
        //for CDI proxy
    }

    @Inject
    public AaregRestKlient(OidcRestClient oidcRestClient,
                           @KonfigVerdi(value = "aareg.rs.url", defaultVerdi = "http://aareg-services-nais.arbeidsforhold/api/v1/arbeidstaker") String url) {
        this.oidcRestClient = oidcRestClient;
        this.url = url;
    }

    public List<ArbeidsforholdRS> finnArbeidsforholdForArbeidstaker(String ident, LocalDate qfom, LocalDate qtom) {
        try {
            var target = UriBuilder.fromUri(url)
                .path("arbeidsforhold")
                .queryParam("ansettelsesperiodeFom", String.valueOf(qfom))
                .queryParam("ansettelsesperiodeTom", String.valueOf(qtom))
                .queryParam("regelverk", "A_ORDNINGEN")
                .queryParam("historikk", "true")
                .queryParam("sporingsinformasjon", "false")
                .build();
            Set<Header> headers = Set.of(new BasicHeader("Nav-Personident", ident));
            ArbeidsforholdRS[] result = oidcRestClient.get(target, headers, ArbeidsforholdRS[].class);
            return Arrays.asList(result);
        } catch (UriBuilderException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Utviklerfeil syntax-exception for finnArbeidsforholdForArbeidstaker");
        }
    }

    public List<ArbeidsforholdRS> finnArbeidsforholdForFrilanser(String ident, LocalDate qfom, LocalDate qtom) {
        try {
            var target = UriBuilder.fromUri(url)
                .path("arbeidsforhold")
                .queryParam("ansettelsesperiodeFom", String.valueOf(qfom))
                .queryParam("ansettelsesperiodeTom", String.valueOf(qtom))
                .queryParam("arbeidsforholdtype", "frilanserOppdragstakerHonorarPersonerMm")
                .queryParam("regelverk", "A_ORDNINGEN")
                .queryParam("historikk", "true")
                .queryParam("sporingsinformasjon", "false")
                .build();
            Set<Header> headers = Set.of(new BasicHeader("Nav-Personident", ident));
            ArbeidsforholdRS[] result = oidcRestClient.get(target, headers, ArbeidsforholdRS[].class);
            return Arrays.asList(result);
        } catch (UriBuilderException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Utviklerfeil syntax-exception for finnArbeidsforholdForArbeidstaker");
        }
    }
}
