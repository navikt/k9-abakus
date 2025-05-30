package no.nav.k9.abakus.web.app.rest.ekstern;

import static no.nav.k9.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt.APPLIKASJON;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import no.nav.k9.abakus.registerdata.ytelse.infotrygd.rest.sp.dto.grunnlag.request.GrunnlagRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import no.nav.abakus.vedtak.ytelse.Aktør;
import no.nav.abakus.vedtak.ytelse.Desimaltall;
import no.nav.abakus.vedtak.ytelse.Periode;
import no.nav.abakus.vedtak.ytelse.Ytelse;
import no.nav.abakus.vedtak.ytelse.Ytelser;
import no.nav.abakus.vedtak.ytelse.request.VedtakForPeriodeRequest;
import no.nav.abakus.vedtak.ytelse.v1.YtelseV1;
import no.nav.abakus.vedtak.ytelse.v1.anvisning.Anvisning;
import no.nav.abakus.vedtak.ytelse.v1.anvisning.AnvistAndel;
import no.nav.abakus.vedtak.ytelse.v1.anvisning.ArbeidsgiverIdent;
import no.nav.k9.abakus.aktor.AktørTjeneste;
import no.nav.k9.abakus.felles.jpa.IntervallEntitet;
import no.nav.k9.abakus.registerdata.infotrygd.InfotrygdgrunnlagYtelseMapper;
import no.nav.k9.abakus.registerdata.ytelse.infotrygd.InnhentingInfotrygdTjeneste;
import no.nav.k9.abakus.registerdata.ytelse.infotrygd.rest.ps.InfotrygdPSGrunnlagRestKlient;
import no.nav.k9.abakus.registerdata.ytelse.infotrygd.rest.ps.PS;
import no.nav.k9.abakus.typer.AktørId;
import no.nav.k9.abakus.typer.Beløp;
import no.nav.k9.abakus.typer.PersonIdent;
import no.nav.k9.abakus.typer.Saksnummer;
import no.nav.k9.abakus.typer.Stillingsprosent;
import no.nav.k9.abakus.vedtak.domene.VedtakYtelseRepository;
import no.nav.k9.abakus.vedtak.extract.v1.ConvertToYtelseV1;
import no.nav.k9.felles.konfigurasjon.konfig.Tid;
import no.nav.k9.felles.sikkerhet.abac.AbacDataAttributter;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessurs;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.k9.felles.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.k9.felles.sikkerhet.abac.TilpassetAbacAttributt;

@OpenAPIDefinition(tags = @Tag(name = "ekstern"), servers = @Server())
@Path("/ytelse/v1")
@ApplicationScoped
@Transactional
public class EksternDelingAvYtelserRestTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(EksternDelingAvYtelserRestTjeneste.class);

    private static final Set<Ytelser> K9_INFOTRYGD_YTELSER = Set.of(Ytelser.PLEIEPENGER_NÆRSTÅENDE, Ytelser.OPPLÆRINGSPENGER, Ytelser.PLEIEPENGER_SYKT_BARN);

    private VedtakYtelseRepository ytelseRepository;
    private AktørTjeneste aktørTjeneste;
    private InfotrygdPSGrunnlagRestKlient infotrygdPSGrunnlag;

    public EksternDelingAvYtelserRestTjeneste() {
    } // CDI Ctor

    @Inject
    public EksternDelingAvYtelserRestTjeneste(VedtakYtelseRepository ytelseRepository,
                                              @PS InfotrygdPSGrunnlagRestKlient infotrygdPSGrunnlag,
                                              AktørTjeneste aktørTjeneste) {
        this.ytelseRepository = ytelseRepository;
        this.aktørTjeneste = aktørTjeneste;
        this.infotrygdPSGrunnlag = infotrygdPSGrunnlag;
    }

    private static ArbeidsgiverIdent mapArbeidsgiverIdent(no.nav.k9.abakus.domene.iay.Arbeidsgiver arbeidsgiver) {
        if (arbeidsgiver == null) {
            return null;
        }
        return new ArbeidsgiverIdent(arbeidsgiver.getIdentifikator());
    }

    @POST
    @Path("/hent-ytelse-vedtak")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Henter alle vedtak for en gitt person, evt med periode etter en fom", tags = "ytelse")
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.READ, resource = APPLIKASJON) //, availabilityType = AvailabilityType.ALL)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<Ytelse> hentVedtakYtelse(@NotNull @TilpassetAbacAttributt(supplierClass = EksternDelingAvYtelserRestTjeneste.VedtakForPeriodeRequestAbacDataSupplier.class) @Valid VedtakForPeriodeRequest request) {
        LOG.info("ABAKUS VEDTAK ekstern /hent-ytelse-vedtak for ytelser {}", request.getYtelser());

        if (request.getYtelser().isEmpty()) {
            return List.of();
        }

        Set<AktørId> aktørIder = utledAktørIdFraRequest(request.getIdent());
        var periode = IntervallEntitet.fraOgMedTilOgMed(request.getPeriode().getFom(), request.getPeriode().getTom());
        var ytelser = new ArrayList<Ytelse>();
        for (AktørId aktørId : aktørIder) {
            ytelser.addAll(ytelseRepository.hentYtelserForIPeriode(aktørId, periode)
                .stream()
                .filter(it -> request.getYtelser().contains(ConvertToYtelseV1.mapYtelser(it.getYtelseType())))
                .map(ConvertToYtelseV1::convert)
                .toList());
        }
        ytelser.addAll(hentVedtakYtelseInfotrygdK9Intern(request));

        return ytelser;
    }


    private List<Ytelse> hentVedtakYtelseInfotrygdK9Intern(VedtakForPeriodeRequest request) {
        if (request.getYtelser().isEmpty() || K9_INFOTRYGD_YTELSER.stream().noneMatch(y -> request.getYtelser().contains(y))) {
            return List.of();
        }

        var aktørIdOpt = utledEnkeltAktørIdFraRequest(request.getIdent());
        if (aktørIdOpt.isEmpty()) {
            return List.of();
        }

        var aktørId = aktørIdOpt.orElseThrow();
        var identer = utledPersonIdentFraRequest(request.getIdent());
        var periode = IntervallEntitet.fraOgMedTilOgMed(request.getPeriode().getFom(), request.getPeriode().getTom());
        var fnr = identer.stream().map(PersonIdent::getIdent).toList();
        var inforequest = new GrunnlagRequest(fnr, fomEllerMin(periode.getFomDato()), tomEllerMax(periode.getTomDato()));
        var infotrygdYtelser = infotrygdPSGrunnlag.hentGrunnlagFailSoft(inforequest);
        var mappedYtelser =  InnhentingInfotrygdTjeneste.mapTilInfotrygdYtelseGrunnlag(infotrygdYtelser, periode.getFomDato()).stream()
            .map(InfotrygdgrunnlagYtelseMapper::oversettInfotrygdYtelseGrunnlagTilYtelse)
            .map(it -> ytelseTilYtelse(aktørId, it))
            .filter(it -> request.getYtelser().contains(it.getYtelse()))
            .toList();
        var ytelser = new ArrayList<Ytelse>(mappedYtelser);

        return ytelser;
    }

    private Set<AktørId> utledAktørIdFraRequest(Aktør aktør) {
        if (aktør.erAktørId()) {
            return Set.of(new AktørId(aktør.getVerdi()));
        }
        return aktørTjeneste.hentAktørIderForIdent(new PersonIdent(aktør.getVerdi()));
    }

    private Optional<AktørId> utledEnkeltAktørIdFraRequest(Aktør aktør) {
        if (aktør.erAktørId()) {
            return Optional.of(new AktørId(aktør.getVerdi()));
        }
        return aktørTjeneste.hentAktørForIdent(new PersonIdent(aktør.getVerdi()));
    }

    private Set<PersonIdent> utledPersonIdentFraRequest(Aktør aktør) {
        if (aktør.erAktørId()) {
            return aktørTjeneste.hentPersonIdenterForAktør(new AktørId(aktør.getVerdi()));

        }
        return Set.of(new PersonIdent(aktør.getVerdi()));
    }

    private YtelseV1 ytelseTilYtelse(AktørId aktørId, no.nav.k9.abakus.domene.iay.Ytelse vedtak) {
        var ytelse = new YtelseV1();
        var aktør = new Aktør();
        aktør.setVerdi(aktørId.getId());
        ytelse.setAktør(aktør);
        ytelse.setVedtattTidspunkt(Optional.ofNullable(vedtak.getVedtattTidspunkt()).orElseGet(LocalDateTime::now));
        ytelse.setYtelse(ConvertToYtelseV1.mapYtelser(vedtak.getRelatertYtelseType()));
        Optional.ofNullable(vedtak.getSaksreferanse()).map(Saksnummer::getVerdi).ifPresent(ytelse::setSaksnummer);
        ytelse.setYtelseStatus(ConvertToYtelseV1.mapStatus(vedtak.getStatus()));
        ytelse.setKildesystem(ConvertToYtelseV1.mapKildesystem(vedtak.getKilde()));
        ytelse.setVedtakReferanse(UUID.randomUUID().toString()); // NotNull i kontrakt
        var periode = new Periode();
        periode.setFom(Optional.ofNullable(vedtak.getPeriode()).map(IntervallEntitet::getFomDato).orElseGet(LocalDate::now));
        periode.setTom(Optional.ofNullable(vedtak.getPeriode()).map(IntervallEntitet::getTomDato).orElseGet(LocalDate::now));
        ytelse.setPeriode(periode);
        var anvist = vedtak.getYtelseAnvist().stream().map(this::mapLagretInfotrygdAnvist).toList();
        ytelse.setAnvist(anvist);
        return ytelse;
    }

    private Anvisning mapLagretInfotrygdAnvist(no.nav.k9.abakus.domene.iay.YtelseAnvist anvist) {
        var anvisning = new Anvisning();
        var periode = new Periode();
        periode.setFom(anvist.getAnvistFOM());
        periode.setTom(anvist.getAnvistTOM());
        anvisning.setPeriode(periode);
        anvist.getBeløp().map(Beløp::getVerdi).map(Desimaltall::new).ifPresent(anvisning::setBeløp);
        anvist.getDagsats().map(Beløp::getVerdi).map(Desimaltall::new).ifPresent(anvisning::setDagsats);
        anvist.getUtbetalingsgradProsent().map(Stillingsprosent::getVerdi).map(Desimaltall::new).ifPresent(anvisning::setUtbetalingsgrad);
        anvisning.setAndeler(mapInfotrygdAndeler(anvist));

        return anvisning;
    }

    private List<AnvistAndel> mapInfotrygdAndeler(no.nav.k9.abakus.domene.iay.YtelseAnvist anvist) {
        return anvist.getYtelseAnvistAndeler()
            .stream()
            .map(a -> new AnvistAndel(a.getArbeidsgiver().map(EksternDelingAvYtelserRestTjeneste::mapArbeidsgiverIdent).orElse(null),
                a.getArbeidsforholdRef().getReferanse(), new Desimaltall(a.getDagsats().getVerdi()),
                a.getUtbetalingsgradProsent() == null ? null : new Desimaltall(a.getUtbetalingsgradProsent().getVerdi()),
                a.getRefusjonsgradProsent() == null ? null : new Desimaltall(a.getRefusjonsgradProsent().getVerdi()),
                ConvertToYtelseV1.fraInntektskategori(a.getInntektskategori())))
            .toList();
    }

    public static class VedtakForPeriodeRequestAbacDataSupplier implements Function<Object, AbacDataAttributter> {

        public VedtakForPeriodeRequestAbacDataSupplier() {
            // Jackson
        }

        @Override
        public AbacDataAttributter apply(Object obj) {
            var req = (VedtakForPeriodeRequest) obj;
            var attributeType = req.getIdent().erAktørId() ? StandardAbacAttributtType.AKTØR_ID : StandardAbacAttributtType.FNR;
            return AbacDataAttributter.opprett().leggTil(attributeType, req.getIdent().getVerdi());
        }
    }

    private static LocalDate fomEllerMin(LocalDate fom) {
        return fom != null ? fom : Tid.TIDENES_BEGYNNELSE;
    }

    private static LocalDate tomEllerMax(LocalDate tom) {
        return tom != null ? tom : Tid.TIDENES_ENDE;
    }


}
