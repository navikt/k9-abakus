package no.nav.k9.abakus.iay.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import no.nav.abakus.iaygrunnlag.AktørIdPersonident;
import no.nav.abakus.iaygrunnlag.ArbeidsforholdRefDto;
import no.nav.abakus.iaygrunnlag.JournalpostId;
import no.nav.abakus.iaygrunnlag.Organisasjon;
import no.nav.abakus.iaygrunnlag.UuidDto;
import no.nav.abakus.iaygrunnlag.inntektsmelding.v1.InntektsmeldingDto;
import no.nav.abakus.iaygrunnlag.inntektsmelding.v1.InntektsmeldingerDto;
import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektsmeldingInnsendingsårsakType;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.iaygrunnlag.request.InntektsmeldingerMottattRequest;
import no.nav.k9.abakus.dbstoette.JpaExtension;
import no.nav.k9.abakus.domene.iay.GrunnlagReferanse;
import no.nav.k9.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.k9.abakus.domene.iay.InntektArbeidYtelseRepository;
import no.nav.k9.abakus.domene.iay.InntektsmeldingAggregat;
import no.nav.k9.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.k9.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.k9.abakus.iay.InntektsmeldingerTjeneste;
import no.nav.k9.abakus.kobling.KoblingTjeneste;
import no.nav.k9.abakus.kobling.repository.KoblingRepository;
import no.nav.k9.abakus.kobling.repository.LåsRepository;


class InntektsmeldingerRestTjenesteTest {

    public static final String SAKSNUMMER = "1234123412345";

    @RegisterExtension
    public static JpaExtension repositoryRule = new JpaExtension();

    private final KoblingRepository repository = new KoblingRepository(repositoryRule.getEntityManager());
    private final KoblingTjeneste koblingTjeneste = new KoblingTjeneste(repository, new LåsRepository(repositoryRule.getEntityManager()));
    private final InntektArbeidYtelseRepository iayRepository = new InntektArbeidYtelseRepository(repositoryRule.getEntityManager());

    private InntektsmeldingerTjeneste imTjenesten;
    private InntektsmeldingerRestTjeneste tjeneste;

    @BeforeEach
    public void setUp() throws Exception {
        imTjenesten = new InntektsmeldingerTjeneste(iayRepository);
        tjeneste = new InntektsmeldingerRestTjeneste(imTjenesten, koblingTjeneste, new InntektArbeidYtelseTjeneste(iayRepository));
    }

    @Test
    void skal_lagre_kobling_og_inntektsmelding() {
        InntektsmeldingerDto im = new InntektsmeldingerDto().medInntektsmeldinger(List.of(
            new InntektsmeldingDto(new Organisasjon("999999999"), new JournalpostId(UUID.randomUUID().toString()), LocalDateTime.now(),
                LocalDate.now()).medStartDatoPermisjon(LocalDate.now())
                .medInntektBeløp(1)
                .medInnsendingsårsak(InntektsmeldingInnsendingsårsakType.NY)
                .medArbeidsforholdRef(new ArbeidsforholdRefDto(UUID.randomUUID().toString(), "ALTINN-01", Fagsystem.AAREGISTERET))
                .medKanalreferanse("KANAL")
                .medKildesystem("Altinn")));
        InntektsmeldingerMottattRequest request = new InntektsmeldingerMottattRequest(SAKSNUMMER, UUID.randomUUID(),
            new AktørIdPersonident("1234123412341"), YtelseType.FORELDREPENGER, im);

        UuidDto uuidDto = tjeneste.lagreInntektsmeldinger(request);

        assertThat(uuidDto).isNotNull();

        Optional<InntektArbeidYtelseGrunnlag> grunnlagOpt = iayRepository.hentInntektArbeidYtelseForReferanse(
            new GrunnlagReferanse(uuidDto.getReferanse()));

        assertThat(grunnlagOpt).isPresent();
        assertThat(grunnlagOpt.flatMap(InntektArbeidYtelseGrunnlag::getInntektsmeldinger)
            .map(InntektsmeldingAggregat::getInntektsmeldinger)).hasValueSatisfying(imer -> assertThat(imer).hasSize(1));
        assertThat(grunnlagOpt.flatMap(InntektArbeidYtelseGrunnlag::getArbeidsforholdInformasjon)
            .map(ArbeidsforholdInformasjon::getArbeidsforholdReferanser)).hasValueSatisfying(imer -> assertThat(imer).hasSize(1));
    }
}
