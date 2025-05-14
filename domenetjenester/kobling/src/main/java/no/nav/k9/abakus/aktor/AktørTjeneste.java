package no.nav.k9.abakus.aktor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.k9.abakus.typer.AktørId;
import no.nav.k9.abakus.typer.PersonIdent;
import no.nav.k9.felles.exception.VLException;
import no.nav.k9.felles.integrasjon.pdl.HentIdenterQueryRequest;
import no.nav.k9.felles.integrasjon.pdl.IdentGruppe;
import no.nav.k9.felles.integrasjon.pdl.IdentInformasjon;
import no.nav.k9.felles.integrasjon.pdl.IdentInformasjonResponseProjection;
import no.nav.k9.felles.integrasjon.pdl.Identliste;
import no.nav.k9.felles.integrasjon.pdl.IdentlisteResponseProjection;
import no.nav.k9.felles.integrasjon.pdl.PdlKlient;
import no.nav.k9.felles.util.LRUCache;


@ApplicationScoped
public class AktørTjeneste {

    private static final int DEFAULT_CACHE_SIZE = 1000;
    private static final long DEFAULT_CACHE_TIMEOUT = TimeUnit.MILLISECONDS.convert(8, TimeUnit.HOURS);

    private LRUCache<AktørId, PersonIdent> cacheAktørIdTilIdent;
    private LRUCache<PersonIdent, AktørId> cacheIdentTilAktørId;

    private PdlKlient pdlKlient;

    AktørTjeneste() {
        //for CDI proxy
    }

    @Inject
    public AktørTjeneste(PdlKlient pdlKlient) {
        this.pdlKlient = pdlKlient;
        this.cacheAktørIdTilIdent = new LRUCache<>(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_TIMEOUT);
        this.cacheIdentTilAktørId = new LRUCache<>(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_TIMEOUT);
    }

    public Optional<AktørId> hentAktørForIdent(PersonIdent fnr) {
        var request = new HentIdenterQueryRequest();
        request.setIdent(fnr.getIdent());
        request.setGrupper(List.of(IdentGruppe.AKTORID));
        request.setHistorikk(Boolean.FALSE);
        var projection = new IdentlisteResponseProjection().identer(new IdentInformasjonResponseProjection().ident());

        try {
            var identliste = hentIdenterForYtelse(request, projection);
            var aktørId = identliste.getIdenter().stream().findFirst().map(IdentInformasjon::getIdent).map(AktørId::new);
            aktørId.ifPresent(a -> cacheIdentTilAktørId.put(fnr, a));
            return aktørId;
        } catch (VLException v) {
            if (PdlKlient.PDL_KLIENT_NOT_FOUND_KODE.equals(v.getKode())) {
                return Optional.empty();
            }
            throw v;
        }
    }

    public Set<AktørId> hentAktørIderForIdent(PersonIdent fnr) {
        var request = new HentIdenterQueryRequest();
        request.setIdent(fnr.getIdent());
        request.setGrupper(List.of(IdentGruppe.AKTORID));
        request.setHistorikk(Boolean.TRUE);
        var projection = new IdentlisteResponseProjection().identer(new IdentInformasjonResponseProjection().ident());

        try {
            var identliste = hentIdenterForYtelse(request, projection);
            return identliste.getIdenter().stream().map(IdentInformasjon::getIdent).map(AktørId::new).collect(Collectors.toSet());
        } catch (VLException v) {
            if (PdlKlient.PDL_KLIENT_NOT_FOUND_KODE.equals(v.getKode())) {
                return Set.of();
            }
            throw v;
        }
    }

    public Optional<PersonIdent> hentIdentForAktør(AktørId aktørId) {
        var fraCache = cacheAktørIdTilIdent.get(aktørId);
        if (fraCache != null) {
            return Optional.of(fraCache);
        }
        var request = new HentIdenterQueryRequest();
        request.setIdent(aktørId.getId());
        request.setGrupper(List.of(IdentGruppe.FOLKEREGISTERIDENT));
        request.setHistorikk(Boolean.FALSE);
        var projection = new IdentlisteResponseProjection().identer(new IdentInformasjonResponseProjection().ident());

        final Identliste identliste;

        try {
            identliste = hentIdenterForYtelse(request, projection);
            var ident = identliste.getIdenter().stream().findFirst().map(IdentInformasjon::getIdent).map(PersonIdent::new);
            ident.ifPresent(i -> cacheAktørIdTilIdent.put(aktørId, i));
            return ident;
        } catch (VLException v) {
            if (PdlKlient.PDL_KLIENT_NOT_FOUND_KODE.equals(v.getKode())) {
                return Optional.empty();
            }
            throw v;
        }
    }

    public Set<PersonIdent> hentPersonIdenterForAktør(AktørId aktørId) {
        var request = new HentIdenterQueryRequest();
        request.setIdent(aktørId.getId());
        request.setGrupper(List.of(IdentGruppe.FOLKEREGISTERIDENT));
        request.setHistorikk(Boolean.TRUE);
        var projection = new IdentlisteResponseProjection().identer(new IdentInformasjonResponseProjection().ident());

        try {
            var identliste = hentIdenterForYtelse(request, projection);
            return identliste.getIdenter().stream().map(IdentInformasjon::getIdent).map(PersonIdent::new).collect(Collectors.toSet());
        } catch (VLException v) {
            if (PdlKlient.PDL_KLIENT_NOT_FOUND_KODE.equals(v.getKode())) {
                return Set.of();
            }
            throw v;
        }
    }

    private Identliste hentIdenterForYtelse(HentIdenterQueryRequest request, IdentlisteResponseProjection projection) {
        return pdlKlient.hentIdenter(request, projection);
    }
}
