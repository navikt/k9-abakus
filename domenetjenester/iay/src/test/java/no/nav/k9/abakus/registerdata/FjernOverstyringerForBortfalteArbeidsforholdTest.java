package no.nav.k9.abakus.registerdata;


import static no.nav.k9.abakus.registerdata.FjernOverstyringerForBortfalteArbeidsforhold.fjern;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.abakus.iaygrunnlag.kodeverk.ArbeidType;
import no.nav.abakus.iaygrunnlag.kodeverk.ArbeidsforholdHandlingType;
import no.nav.k9.abakus.domene.iay.Arbeidsgiver;
import no.nav.k9.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.k9.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.k9.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.k9.abakus.domene.iay.arbeidsforhold.ArbeidsforholdOverstyring;
import no.nav.k9.abakus.domene.iay.arbeidsforhold.ArbeidsforholdOverstyringBuilder;
import no.nav.k9.abakus.domene.iay.arbeidsforhold.ArbeidsforholdReferanse;
import no.nav.k9.abakus.registerdata.arbeidsforhold.ArbeidsforholdIdentifikator;
import no.nav.k9.abakus.registerdata.arbeidsforhold.Organisasjon;
import no.nav.k9.abakus.typer.EksternArbeidsforholdRef;
import no.nav.k9.abakus.typer.InternArbeidsforholdRef;
import no.nav.k9.abakus.typer.OrgNummer;

class FjernOverstyringerForBortfalteArbeidsforholdTest {

    @Test
    void skal_fjerne_overstyringer_for_arbeidsforhold() {
        // Arrange
        EksternArbeidsforholdRef eksternRef = EksternArbeidsforholdRef.ref("eksternRef");
        EksternArbeidsforholdRef eksternRef2 = EksternArbeidsforholdRef.ref("eksternRef2");
        OrgNummer orgnr = new OrgNummer("910909088");
        Arbeidsgiver virksomhet = Arbeidsgiver.virksomhet(orgnr);
        Set<ArbeidsforholdIdentifikator> innhentetArbeidsforhold = Set.of(
            new ArbeidsforholdIdentifikator(new Organisasjon(orgnr.getId()), eksternRef, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD.getKode()));
        InntektArbeidYtelseGrunnlagBuilder grunnlagBuilder = InntektArbeidYtelseGrunnlagBuilder.nytt();
        ArbeidsforholdInformasjonBuilder builder = ArbeidsforholdInformasjonBuilder.builder(Optional.empty());
        InternArbeidsforholdRef ref = InternArbeidsforholdRef.nyRef();
        ArbeidsforholdOverstyringBuilder overstyringBuilder = ArbeidsforholdOverstyringBuilder.oppdatere(Optional.empty())
            .medArbeidsgiver(virksomhet)
            .medArbeidsforholdRef(ref);
        InternArbeidsforholdRef ref2 = InternArbeidsforholdRef.nyRef();
        ArbeidsforholdOverstyringBuilder overstyringBuilder2 = ArbeidsforholdOverstyringBuilder.oppdatere(Optional.empty())
            .medArbeidsgiver(virksomhet)
            .medArbeidsforholdRef(ref2);
        builder.leggTil(overstyringBuilder).leggTil(overstyringBuilder2).leggTilNyReferanse(new ArbeidsforholdReferanse(virksomhet, ref, eksternRef));
        builder.leggTilNyReferanse(new ArbeidsforholdReferanse(virksomhet, ref2, eksternRef2));
        grunnlagBuilder.medInformasjon(builder.build());

        // Act
        ArbeidsforholdInformasjonBuilder informasjonBuilder = fjern(grunnlagBuilder, innhentetArbeidsforhold);

        // Assert
        ArbeidsforholdInformasjon nyInformasjon = informasjonBuilder.build();
        assertThat(nyInformasjon.getOverstyringer()).hasSize(1);
        ArbeidsforholdOverstyring gjenværendeOverstyring = nyInformasjon.getOverstyringer().iterator().next();
        assertThat(gjenværendeOverstyring.getArbeidsgiver().getOrgnr()).isEqualByComparingTo(orgnr);
        assertThat(gjenværendeOverstyring.getArbeidsforholdRef()).isEqualTo(ref);
    }


    @Test
    void skal_ikke_fjerne_overstyringer_for_fiktive_arbeidsforhold() {
        // Arrange
        EksternArbeidsforholdRef eksternRef = EksternArbeidsforholdRef.ref("eksternRef");
        OrgNummer orgnr = new OrgNummer("910909088");
        Arbeidsgiver virksomhet = Arbeidsgiver.virksomhet(orgnr);
        Set<ArbeidsforholdIdentifikator> innhentetArbeidsforhold = Set.of(
            new ArbeidsforholdIdentifikator(new Organisasjon(orgnr.getId()), eksternRef, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD.getKode()));
        InntektArbeidYtelseGrunnlagBuilder grunnlagBuilder = InntektArbeidYtelseGrunnlagBuilder.nytt();
        ArbeidsforholdInformasjonBuilder builder = ArbeidsforholdInformasjonBuilder.builder(Optional.empty());
        InternArbeidsforholdRef ref = InternArbeidsforholdRef.nyRef();
        ArbeidsforholdOverstyringBuilder overstyringBuilder = ArbeidsforholdOverstyringBuilder.oppdatere(Optional.empty())
            .medArbeidsgiver(virksomhet)
            .medArbeidsforholdRef(ref);
        ArbeidsforholdOverstyringBuilder overstyringBuilder2 = ArbeidsforholdOverstyringBuilder.oppdatere(Optional.empty())
            .medHandling(ArbeidsforholdHandlingType.LAGT_TIL_AV_SAKSBEHANDLER)
            .medArbeidsgiver(Arbeidsgiver.virksomhet(new OrgNummer(OrgNummer.KUNSTIG_ORG)));
        builder.leggTil(overstyringBuilder).leggTil(overstyringBuilder2).leggTilNyReferanse(new ArbeidsforholdReferanse(virksomhet, ref, eksternRef));
        grunnlagBuilder.medInformasjon(builder.build());

        // Act
        ArbeidsforholdInformasjonBuilder informasjonBuilder = fjern(grunnlagBuilder, innhentetArbeidsforhold);

        // Assert
        ArbeidsforholdInformasjon nyInformasjon = informasjonBuilder.build();
        assertThat(nyInformasjon.getOverstyringer()).hasSize(2);
    }
}
