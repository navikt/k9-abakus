package no.nav.k9.abakus.iay.tjeneste.dto.iay;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import no.nav.abakus.iaygrunnlag.inntektsmelding.v1.InntektsmeldingerDto;
import no.nav.abakus.iaygrunnlag.kodeverk.ArbeidsforholdHandlingType;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.k9.abakus.dbstoette.JpaExtension;
import no.nav.k9.abakus.domene.iay.Arbeidsgiver;
import no.nav.k9.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.k9.abakus.domene.iay.InntektArbeidYtelseRepository;
import no.nav.k9.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.k9.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.k9.abakus.domene.iay.arbeidsforhold.ArbeidsforholdOverstyringBuilder;
import no.nav.k9.abakus.domene.iay.arbeidsforhold.ArbeidsforholdReferanse;
import no.nav.k9.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.k9.abakus.domene.iay.inntektsmelding.InntektsmeldingBuilder;
import no.nav.k9.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.k9.abakus.kobling.Kobling;
import no.nav.k9.abakus.kobling.KoblingReferanse;
import no.nav.k9.abakus.kobling.KoblingTjeneste;
import no.nav.k9.abakus.kobling.repository.KoblingRepository;
import no.nav.k9.abakus.kobling.repository.LåsRepository;
import no.nav.k9.abakus.typer.AktørId;
import no.nav.k9.abakus.typer.EksternArbeidsforholdRef;
import no.nav.k9.abakus.typer.InternArbeidsforholdRef;
import no.nav.k9.abakus.typer.OrgNummer;
import no.nav.k9.abakus.typer.Saksnummer;

class MapInntektsmeldingerTest {

    public static final String SAKSNUMMER = "1234123412345";
    private static final YtelseType ytelseType = YtelseType.FORELDREPENGER;

    @RegisterExtension
    public static JpaExtension jpaExtension = new JpaExtension();

    private final KoblingRepository repository = new KoblingRepository(jpaExtension.getEntityManager());
    private final KoblingTjeneste koblingTjeneste = new KoblingTjeneste(repository, new LåsRepository(jpaExtension.getEntityManager()));
    private final InntektArbeidYtelseRepository iayRepository = new InntektArbeidYtelseRepository(jpaExtension.getEntityManager());
    private final InntektArbeidYtelseTjeneste iayTjeneste = new InntektArbeidYtelseTjeneste(iayRepository);

    @Test
    void skal_hente_alle_inntektsmeldinger_for_fagsak_uten_duplikater() {
        // Arrange
        Saksnummer saksnummer = new Saksnummer(SAKSNUMMER);
        KoblingReferanse koblingReferanse = new KoblingReferanse(UUID.randomUUID());
        KoblingReferanse koblingReferanse2 = new KoblingReferanse(UUID.randomUUID());
        AktørId aktørId = new AktørId("1234123412341");
        YtelseType foreldrepenger = YtelseType.FORELDREPENGER;
        Kobling kobling1 = new Kobling(foreldrepenger, saksnummer, koblingReferanse, aktørId);
        Kobling kobling2 = new Kobling(foreldrepenger, saksnummer, koblingReferanse2, aktørId);
        koblingTjeneste.lagre(kobling1);
        koblingTjeneste.lagre(kobling2);
        Inntektsmelding im = InntektsmeldingBuilder.builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet(new OrgNummer("910909088")))
            .medBeløp(BigDecimal.TEN)
            .medInnsendingstidspunkt(LocalDateTime.now())
            .medMottattDato(LocalDate.now())
            .medJournalpostId("journalpost_id")
            .build();
        ArbeidsforholdInformasjonBuilder arbeidsforholdInformasjon = ArbeidsforholdInformasjonBuilder.builder(Optional.empty());
        iayRepository.lagre(koblingReferanse, arbeidsforholdInformasjon, List.of(im));
        iayRepository.lagre(koblingReferanse2, arbeidsforholdInformasjon, List.of(im));

        // Act
        Map<Inntektsmelding, ArbeidsforholdInformasjon> alleIm = iayTjeneste.hentArbeidsforholdinfoInntektsmeldingerMapFor(aktørId, saksnummer,
            foreldrepenger);
        InntektsmeldingerDto inntektsmeldingerDto = MapInntektsmeldinger.mapUnikeInntektsmeldingerFraGrunnlag(alleIm);

        // Assert
        assertThat(inntektsmeldingerDto.getInntektsmeldinger().size()).isEqualTo(1);
    }

    @Test
    void skal_hente_alle_inntektsmeldinger_for_fagsak_uten_duplikater_med_flere_versjoner_av_arbeidsforholdInformasjon() {
        // Arrange
        Saksnummer saksnummer = new Saksnummer(SAKSNUMMER);
        KoblingReferanse koblingReferanse = new KoblingReferanse(UUID.randomUUID());
        AktørId aktørId = new AktørId("1234123412341");
        YtelseType foreldrepenger = YtelseType.FORELDREPENGER;
        Kobling kobling1 = new Kobling(foreldrepenger, saksnummer, koblingReferanse, aktørId);
        koblingTjeneste.lagre(kobling1);
        UUID internArbeidsforholdRef = UUID.randomUUID();
        Arbeidsgiver virksomhet = Arbeidsgiver.virksomhet(new OrgNummer("910909088"));
        InternArbeidsforholdRef ref = InternArbeidsforholdRef.ref(internArbeidsforholdRef);
        Inntektsmelding im = InntektsmeldingBuilder.builder()
            .medArbeidsgiver(virksomhet)
            .medArbeidsforholdId(ref)
            .medBeløp(BigDecimal.TEN)
            .medInnsendingstidspunkt(LocalDateTime.now())
            .medMottattDato(LocalDate.now())
            .medJournalpostId("journalpost_id")
            .build();
        EksternArbeidsforholdRef eksternRef = EksternArbeidsforholdRef.ref("EksternRef");
        ArbeidsforholdInformasjonBuilder arbeidsforholdInfo1 = ArbeidsforholdInformasjonBuilder.builder(Optional.empty());
        arbeidsforholdInfo1.leggTilNyReferanse(new ArbeidsforholdReferanse(virksomhet, ref, eksternRef));
        iayRepository.lagre(koblingReferanse, arbeidsforholdInfo1, List.of(im));
        ArbeidsforholdInformasjonBuilder arbeidsforholdInfo2 = ArbeidsforholdInformasjonBuilder.builder(Optional.of(arbeidsforholdInfo1.build()))
            .leggTil(ArbeidsforholdOverstyringBuilder.oppdatere(Optional.empty())
                .medArbeidsforholdRef(ref)
                .medArbeidsgiver(virksomhet)
                .medHandling(ArbeidsforholdHandlingType.BASERT_PÅ_INNTEKTSMELDING));
        InntektArbeidYtelseGrunnlagBuilder grBuilder = InntektArbeidYtelseGrunnlagBuilder.oppdatere(
            iayRepository.hentInntektArbeidYtelseForBehandling(koblingReferanse)).medInformasjon(arbeidsforholdInfo2.build());
        iayRepository.lagre(koblingReferanse, grBuilder);

        // Act
        Map<Inntektsmelding, ArbeidsforholdInformasjon> alleIm = iayTjeneste.hentArbeidsforholdinfoInntektsmeldingerMapFor(aktørId, saksnummer,
            foreldrepenger);
        InntektsmeldingerDto inntektsmeldingerDto = MapInntektsmeldinger.mapUnikeInntektsmeldingerFraGrunnlag(alleIm);

        // Assert
        assertThat(inntektsmeldingerDto.getInntektsmeldinger().size()).isEqualTo(1);
    }

}
