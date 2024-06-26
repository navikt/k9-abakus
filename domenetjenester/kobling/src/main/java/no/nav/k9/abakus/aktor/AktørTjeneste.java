package no.nav.k9.abakus.aktor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.k9.abakus.typer.AktørId;
import no.nav.k9.abakus.typer.PersonIdent;
import no.nav.pdl.HentIdenterQueryRequest;
import no.nav.pdl.IdentGruppe;
import no.nav.pdl.IdentInformasjon;
import no.nav.pdl.IdentInformasjonResponseProjection;
import no.nav.pdl.Identliste;
import no.nav.pdl.IdentlisteResponseProjection;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.integrasjon.person.Persondata;
import no.nav.vedtak.felles.integrasjon.person.Tema;
import no.nav.vedtak.util.LRUCache;


@ApplicationScoped
public class AktørTjeneste {

    private static final int DEFAULT_CACHE_SIZE = 1000;
    private static final long DEFAULT_CACHE_TIMEOUT = TimeUnit.MILLISECONDS.convert(8, TimeUnit.HOURS);

    private final LRUCache<AktørId, PersonIdent> cacheAktørIdTilIdent;
    private final LRUCache<PersonIdent, AktørId> cacheIdentTilAktørId;

    private final Persondata pdlKlientOMS;

    public AktørTjeneste() {
        this.pdlKlientOMS = new PdlKlient(Tema.OMS); // Inntil omlegging til k9-felles
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
            if (Persondata.PDL_KLIENT_NOT_FOUND_KODE.equals(v.getKode())) {
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
            if (Persondata.PDL_KLIENT_NOT_FOUND_KODE.equals(v.getKode())) {
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
            if (Persondata.PDL_KLIENT_NOT_FOUND_KODE.equals(v.getKode())) {
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
            if (Persondata.PDL_KLIENT_NOT_FOUND_KODE.equals(v.getKode())) {
                return Set.of();
            }
            throw v;
        }
    }

    private Identliste hentIdenterForYtelse(HentIdenterQueryRequest request, IdentlisteResponseProjection projection) {
        return pdlKlientOMS.hentIdenter(request, projection);
    }
}
