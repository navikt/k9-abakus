package no.nav.foreldrepenger.abakus.registerdata.tjeneste;

import static no.nav.foreldrepenger.abakus.registerdata.callback.CallbackTask.EKSISTERENDE_GRUNNLAG_REF;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
import no.nav.foreldrepenger.abakus.kobling.kontroll.YtelseTypeRef;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.registerdata.IAYRegisterInnhentingTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.RegisterdataInnhentingTask;
import no.nav.foreldrepenger.abakus.registerdata.callback.CallbackTask;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Aktør;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Periode;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.request.InnhentRegisterdataRequest;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.api.TaskStatus;

@ApplicationScoped
public class InnhentRegisterdataTjeneste {

    private Map<YtelseType, IAYRegisterInnhentingTjeneste> registerInnhentingTjeneste;
    private InntektArbeidYtelseTjeneste iayTjeneste;
    private KoblingTjeneste koblingTjeneste;
    private ProsessTaskRepository prosessTaskRepository;
    private KodeverkRepository kodeverkRepository;

    InnhentRegisterdataTjeneste() {
        // CDI
    }

    @Inject
    public InnhentRegisterdataTjeneste(@Any Instance<IAYRegisterInnhentingTjeneste> innhentingTjeneste,
                                       InntektArbeidYtelseTjeneste iayTjeneste,
                                       KoblingTjeneste koblingTjeneste,
                                       ProsessTaskRepository prosessTaskRepository,
                                       KodeverkRepository kodeverkRepository) {
        this.registerInnhentingTjeneste = new HashMap<>();
        innhentingTjeneste.forEach(innhenter -> populerMap(registerInnhentingTjeneste, innhenter));
        this.iayTjeneste = iayTjeneste;
        this.koblingTjeneste = koblingTjeneste;
        this.prosessTaskRepository = prosessTaskRepository;
        this.kodeverkRepository = kodeverkRepository;
    }

    private void populerMap(Map<YtelseType, IAYRegisterInnhentingTjeneste> map, IAYRegisterInnhentingTjeneste innhenter) {
        YtelseType type = YtelseType.UDEFINERT;
        if (innhenter.getClass().isAnnotationPresent(YtelseTypeRef.class)) {
            type = kodeverkRepository.finn(YtelseType.class, innhenter.getClass().getAnnotation(YtelseTypeRef.class).value());
        }
        map.put(type, innhenter);
    }

    public Optional<UUID> innhent(InnhentRegisterdataRequest dto) {
        Kobling kobling = oppdaterKobling(dto);

        // Trigg innhenting
        InntektArbeidYtelseAggregatBuilder builder = finnInnhenter(mapTilYtelseType(dto)).innhentRegisterdata(kobling);
        iayTjeneste.lagre(kobling.getId(), builder);

        Optional<InntektArbeidYtelseGrunnlag> grunnlag = iayTjeneste.hentGrunnlagFor(kobling.getId());
        return grunnlag.map(InntektArbeidYtelseGrunnlag::getReferanse);
    }

    private IAYRegisterInnhentingTjeneste finnInnhenter(YtelseType ytelseType) {
        IAYRegisterInnhentingTjeneste innhenter = registerInnhentingTjeneste.get(ytelseType);
        if (innhenter == null) {
            throw new IllegalArgumentException("Finner ikke IAYRegisterInnhenter. Støtter ikke ytelsetype " + ytelseType);
        }
        return innhenter;
    }

    private Kobling oppdaterKobling(InnhentRegisterdataRequest dto) {
        UUID referanse = UUID.fromString(dto.getReferanse());
        Optional<Kobling> koblingOpt = koblingTjeneste.hentFor(referanse);
        Kobling kobling;
        if (koblingOpt.isEmpty()) {
            // Lagre kobling
            Periode opplysningsperiode = dto.getOpplysningsperiode();
            YtelseType ytelseType = mapTilYtelseType(dto);
            DatoIntervallEntitet opplysningsperiode1 = DatoIntervallEntitet.fraOgMedTilOgMed(opplysningsperiode.getFom(), opplysningsperiode.getTom());
            AktørId aktørId = new AktørId(dto.getAktør().getIdent());
            kobling = new Kobling(referanse, aktørId, opplysningsperiode1, ytelseType);
        } else {
            kobling = koblingOpt.get();
        }
        // Oppdater kobling
        Aktør annenPartAktør = dto.getAnnenPartAktør();
        if (annenPartAktør != null) {
            kobling.setAnnenPartAktørId(new AktørId(annenPartAktør.getIdent()));
        }
        Periode opplysningsperiode = dto.getOpplysningsperiode();
        if (opplysningsperiode != null) {
            kobling.setOpplysningsperiode(DatoIntervallEntitet.fraOgMedTilOgMed(opplysningsperiode.getFom(), opplysningsperiode.getTom()));
        }
        Periode opptjeningsperiode = dto.getOpptjeningsperiode();
        if (opptjeningsperiode != null) {
            kobling.setOpptjeningsperiode(DatoIntervallEntitet.fraOgMedTilOgMed(opptjeningsperiode.getFom(), opptjeningsperiode.getTom()));
        }
        // Diff & log endringer
        koblingTjeneste.lagre(kobling);
        return kobling;
    }

    private YtelseType mapTilYtelseType(InnhentRegisterdataRequest dto) {
        return kodeverkRepository.finn(YtelseType.class, dto.getYtelseType().getKode());
    }

    public String triggAsyncInnhent(InnhentRegisterdataRequest dto) {
        Kobling kobling = oppdaterKobling(dto);

        ProsessTaskGruppe taskGruppe = new ProsessTaskGruppe();
        ProsessTaskData innhentingTask = new ProsessTaskData(RegisterdataInnhentingTask.TASKTYPE);
        ProsessTaskData callbackTask = new ProsessTaskData(CallbackTask.TASKTYPE);
        innhentingTask.setKobling(kobling.getId(), kobling.getAktørId().getId());
        callbackTask.setKobling(kobling.getId(), kobling.getAktørId().getId());

        Optional<UUID> eksisterendeGrunnlagRef = hentSisteReferanseFor(kobling.getReferanse());
        eksisterendeGrunnlagRef.ifPresent(ref -> callbackTask.setProperty(EKSISTERENDE_GRUNNLAG_REF, ref.toString()));

        if (dto.getCallbackUrl() != null) {
            innhentingTask.setCallbackUrl(dto.getCallbackUrl());
            callbackTask.setCallbackUrl(dto.getCallbackUrl());
        }
        taskGruppe.addNesteSekvensiell(innhentingTask);
        taskGruppe.addNesteSekvensiell(callbackTask);

        return prosessTaskRepository.lagre(innhentingTask);
    }

    public boolean innhentingFerdig(String taskReferanse) {
        List<TaskStatus> taskStatuses = prosessTaskRepository.finnStatusForGruppe(taskReferanse);
        return taskStatuses.stream().anyMatch(it -> !ProsessTaskStatus.KLAR.equals(it.getStatus()));
    }

    public Optional<UUID> hentSisteReferanseFor(UUID koblingRef) {
        Optional<Kobling> kobling = koblingTjeneste.hentFor(koblingRef);
        if (kobling.isEmpty()) {
            return Optional.empty();
        }
        Optional<InntektArbeidYtelseGrunnlag> grunnlag = iayTjeneste.hentGrunnlagFor(kobling.get().getId());
        return grunnlag.map(InntektArbeidYtelseGrunnlag::getReferanse);
    }

}
