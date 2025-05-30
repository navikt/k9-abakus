package no.nav.k9.abakus.registerdata.tjeneste;

import static no.nav.k9.abakus.registerdata.callback.K9sakCallbackTask.EKSISTERENDE_GRUNNLAG_REF;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.abakus.iaygrunnlag.JsonObjectMapper;
import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.iaygrunnlag.request.InnhentRegisterdataRequest;
import no.nav.abakus.iaygrunnlag.request.RegisterdataType;
import no.nav.k9.abakus.domene.iay.GrunnlagReferanse;
import no.nav.k9.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.k9.abakus.felles.jpa.IntervallEntitet;
import no.nav.k9.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.k9.abakus.kobling.Kobling;
import no.nav.k9.abakus.kobling.KoblingReferanse;
import no.nav.k9.abakus.kobling.KoblingTjeneste;
import no.nav.k9.abakus.kobling.TaskConstants;
import no.nav.k9.abakus.registerdata.RegisterdataInnhentingTask;
import no.nav.k9.abakus.registerdata.callback.K9sakCallbackTask;
import no.nav.k9.abakus.registerdata.callback.UngsakCallbackTask;
import no.nav.k9.abakus.typer.AktørId;
import no.nav.k9.abakus.typer.Saksnummer;
import no.nav.k9.prosesstask.api.ProsessTaskData;
import no.nav.k9.prosesstask.api.ProsessTaskGruppe;
import no.nav.k9.prosesstask.api.ProsessTaskTjeneste;

@ApplicationScoped
public class InnhentRegisterdataTjeneste {

    private static final Map<RegisterdataType, RegisterdataElement> registerdataMapping = initMapping();
    private InntektArbeidYtelseTjeneste iayTjeneste;
    private KoblingTjeneste koblingTjeneste;
    private ProsessTaskTjeneste taskTjeneste;

    InnhentRegisterdataTjeneste() {
        // CDI
    }

    @Inject
    public InnhentRegisterdataTjeneste(InntektArbeidYtelseTjeneste iayTjeneste, KoblingTjeneste koblingTjeneste, ProsessTaskTjeneste taskTjeneste) {
        this.iayTjeneste = iayTjeneste;
        this.koblingTjeneste = koblingTjeneste;
        this.taskTjeneste = taskTjeneste;
    }

    private static Map<RegisterdataType, RegisterdataElement> initMapping() {
        return Map.of(RegisterdataType.ARBEIDSFORHOLD, RegisterdataElement.ARBEIDSFORHOLD, RegisterdataType.YTELSE, RegisterdataElement.YTELSE,
            RegisterdataType.LIGNET_NÆRING, RegisterdataElement.LIGNET_NÆRING, RegisterdataType.INNTEKT_PENSJONSGIVENDE,
            RegisterdataElement.INNTEKT_PENSJONSGIVENDE, RegisterdataType.INNTEKT_BEREGNINGSGRUNNLAG, RegisterdataElement.INNTEKT_BEREGNINGSGRUNNLAG,
            RegisterdataType.INNTEKT_SAMMENLIGNINGSGRUNNLAG, RegisterdataElement.INNTEKT_SAMMENLIGNINGSGRUNNLAG,
            RegisterdataType.INNTEKT_UNGDOMSYTELSEGRUNNLAG, RegisterdataElement.INNTEKT_UNGDOMSYTELSEGRUNNLAG);
    }

    public static Set<RegisterdataElement> hentUtInformasjonsElementer(InnhentRegisterdataRequest dto) {
        final var elementer = dto.getElementer();

        if (elementer == null || elementer.isEmpty()) {
            return Set.of();
        }

        return elementer.stream().map(registerdataMapping::get).collect(Collectors.toSet());
    }

    private Kobling oppdaterKobling(InnhentRegisterdataRequest dto) {
        KoblingReferanse referanse = new KoblingReferanse(dto.getReferanse());
        var koblingLås = Optional.ofNullable(koblingTjeneste.taSkrivesLås(referanse)); // kan bli null hvis gjelder ny
        Optional<Kobling> koblingOpt = koblingTjeneste.hentFor(referanse);
        Kobling kobling;
        if (koblingOpt.isEmpty()) {
            // Lagre kobling
            AktørId aktørId = new AktørId(dto.getAktør().getIdent());
            kobling = new Kobling(dto.getYtelseType(), new Saksnummer(dto.getSaksnummer()), referanse, aktørId);
        } else {
            kobling = koblingOpt.get();
            if (YtelseType.UDEFINERT.equals(kobling.getYtelseType())) {
                var ytelseType = dto.getYtelseType();
                if (ytelseType != null) {
                    kobling.setYtelseType(ytelseType);
                }
            }
        }

        // Oppdater kobling med perioder
        mapPeriodeTilIntervall(dto.getOpplysningsperiode()).ifPresent(kobling::setOpplysningsperiode);
        mapPeriodeTilIntervall(dto.getOpplysningsperiodeSkattegrunnlag()).ifPresent(kobling::setOpplysningsperiodeSkattegrunnlag);
        mapPeriodeTilIntervall(dto.getOpptjeningsperiode()).ifPresent(kobling::setOpptjeningsperiode);

        // Diff & log endringer
        koblingTjeneste.lagre(kobling);
        koblingLås.ifPresent(lås -> koblingTjeneste.oppdaterLåsVersjon(lås));

        return kobling;
    }

    private Optional<IntervallEntitet> mapPeriodeTilIntervall(Periode periode) {
        return Optional.ofNullable(periode == null ? null : IntervallEntitet.fraOgMedTilOgMed(periode.getFom(), periode.getTom()));
    }

    public String triggAsyncInnhent(InnhentRegisterdataRequest dto) {
        Kobling kobling = oppdaterKobling(dto);

        ProsessTaskGruppe taskGruppe = new ProsessTaskGruppe();
        var innhentingTask = ProsessTaskData.forProsessTask(RegisterdataInnhentingTask.class);
        var callbackTask = kobling.getYtelseType() == YtelseType.UNGDOMSYTELSE ? ProsessTaskData.forProsessTask(UngsakCallbackTask.class) : ProsessTaskData.forProsessTask(K9sakCallbackTask.class);
        innhentingTask.setAktørId(kobling.getAktørId().getId());
        innhentingTask.setProperty(TaskConstants.GAMMEL_KOBLING_ID, kobling.getId().toString());
        innhentingTask.setProperty(TaskConstants.NY_KOBLING_ID, kobling.getId().toString());
        try {
            innhentingTask.setPayload(JsonObjectMapper.getMapper().writeValueAsString(dto));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Feil i serialisering av innhentingrequest", e);
        }
        callbackTask.setAktørId(kobling.getAktørId().getId());
        callbackTask.setProperty(TaskConstants.GAMMEL_KOBLING_ID, kobling.getId().toString());
        callbackTask.setProperty(TaskConstants.NY_KOBLING_ID, kobling.getId().toString());

        Optional<GrunnlagReferanse> eksisterendeGrunnlagRef = hentSisteReferanseFor(kobling.getKoblingReferanse());
        eksisterendeGrunnlagRef.map(GrunnlagReferanse::getReferanse)
            .ifPresent(ref -> callbackTask.setProperty(EKSISTERENDE_GRUNNLAG_REF, ref.toString()));

        callbackTask.setProperty(TaskConstants.CALLBACK_URL, dto.getCallbackUrl());

        taskGruppe.addNesteSekvensiell(innhentingTask);
        taskGruppe.addNesteSekvensiell(callbackTask);
        taskGruppe.setCallIdFraEksisterende();

        return taskTjeneste.lagre(taskGruppe);
    }

    public boolean innhentingFerdig(String taskReferanse) {
        return taskTjeneste.finnUferdigForGruppe(taskReferanse).isEmpty();
    }

    public Optional<GrunnlagReferanse> hentSisteReferanseFor(KoblingReferanse koblingRef) {
        Optional<Kobling> kobling = koblingTjeneste.hentFor(koblingRef);
        if (kobling.isEmpty()) {
            return Optional.empty();
        }
        Optional<InntektArbeidYtelseGrunnlag> grunnlag = iayTjeneste.hentGrunnlagFor(kobling.get().getKoblingReferanse());
        return grunnlag.map(InntektArbeidYtelseGrunnlag::getGrunnlagReferanse);
    }

}
