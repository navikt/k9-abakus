package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.util.stream.Collectors;

import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.InntektsmeldingAggregat;
import no.nav.foreldrepenger.abakus.domene.iay.InntektsmeldingAggregatEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdReferanseEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Gradering;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.GraderingEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.NaturalYtelse;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.NaturalYtelseEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Refusjon;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.RefusjonEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.UtsettelsePeriode;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.UtsettelsePeriodeEntitet;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.EksternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Aktør;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.AktørIdPersonident;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.ArbeidsforholdRefDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.JournalpostId;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Organisasjon;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Periode;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.inntektsmelding.v1.GraderingDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.inntektsmelding.v1.InntektsmeldingDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.inntektsmelding.v1.InntektsmeldingerDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.inntektsmelding.v1.NaturalytelseDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.inntektsmelding.v1.RefusjonDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.inntektsmelding.v1.UtsettelsePeriodeDto;

public class MapInntektsmeldinger {

    public static class MapTilDto {

        private ArbeidsforholdInformasjon arbeidsforholdInformasjon;

        public MapTilDto(ArbeidsforholdInformasjon arbeidsforholdInformasjon) {
            this.arbeidsforholdInformasjon = arbeidsforholdInformasjon;
        }

        public InntektsmeldingerDto map(InntektsmeldingAggregat inntektsmeldingAggregat) {
            if (arbeidsforholdInformasjon == null && inntektsmeldingAggregat == null) {
                return null;
            } else if (arbeidsforholdInformasjon != null && inntektsmeldingAggregat != null) {
                var dto = new InntektsmeldingerDto();

                var inntektsmeldinger = inntektsmeldingAggregat.getAlleInntektsmeldinger().stream()
                    .map(im -> this.mapInntektsmelding(im)).collect(Collectors.toList());
                dto.medInntektsmeldinger(inntektsmeldinger);

                return dto;
            } else {
                throw new IllegalStateException(
                    "Utvikler-feil: Både arbeidsforholdInformasjon og inntektsmeldingAggregat må samtidig eksistere, men har arbeidsforholdInformasjon:"
                        + arbeidsforholdInformasjon + ", inntektsmeldingAggregat=" + inntektsmeldingAggregat);
            }
        }

        private InntektsmeldingDto mapInntektsmelding(Inntektsmelding im) {
            var arbeidsgiver = mapAktør(im.getArbeidsgiver());
            var journalpostId = new JournalpostId(im.getJournalpostId().getVerdi());
            var innsendingstidspunkt = im.getInnsendingstidspunkt();
            var eksternRef = arbeidsforholdInformasjon.finnEkstern(im.getArbeidsgiver(), im.getArbeidsforholdRef());
            var arbeidsforholdId = mapArbeidsforholdsId(im.getArbeidsgiver(), im.getArbeidsforholdRef(), eksternRef);
            var innsendingsårsak = KodeverkMapper.mapInntektsmeldingInnsendingsårsak(im.getInntektsmeldingInnsendingsårsak());
            var mottattDato = im.getMottattDato();

            var inntektsmeldingDto = new InntektsmeldingDto(arbeidsgiver, journalpostId, innsendingstidspunkt, mottattDato)
                .medArbeidsforholdRef(arbeidsforholdId)
                .medInnsendingsårsak(innsendingsårsak)
                .medInntektBeløp(im.getInntektBeløp().getVerdi())
                .medKanalreferanse(im.getKanalreferanse())
                .medKildesystem(im.getKildesystem())
                .medRefusjonOpphører(im.getRefusjonOpphører())
                .medRefusjonsBeløpPerMnd(im.getRefusjonBeløpPerMnd() == null ? null : im.getRefusjonBeløpPerMnd().getVerdi())
                .medStartDatoPermisjon(im.getStartDatoPermisjon())
                .medNærRelasjon(im.getErNærRelasjon());

            inntektsmeldingDto.medEndringerRefusjon(im.getEndringerRefusjon().stream().map(this::mapEndringRefusjon).collect(Collectors.toList()));

            inntektsmeldingDto.medGraderinger(im.getGraderinger().stream().map(this::mapGradering).collect(Collectors.toList()));

            inntektsmeldingDto.medNaturalytelser(im.getNaturalYtelser().stream().map(this::mapNaturalytelse).collect(Collectors.toList()));

            inntektsmeldingDto.medUtsettelsePerioder(im.getUtsettelsePerioder().stream().map(this::mapUtsettelsePeriode).collect(Collectors.toList()));

            return inntektsmeldingDto;
        }

        private RefusjonDto mapEndringRefusjon(Refusjon refusjon) {
            return new RefusjonDto(refusjon.getFom(), refusjon.getRefusjonsbeløp().getVerdi());
        }

        private GraderingDto mapGradering(Gradering gradering) {
            var periode = gradering.getPeriode();
            var arbeidstidProsent = gradering.getArbeidstidProsent();
            return new GraderingDto(new Periode(periode.getFomDato(), periode.getTomDato()), arbeidstidProsent.getVerdi());
        }

        private NaturalytelseDto mapNaturalytelse(NaturalYtelse naturalYtelse) {
            var periode = naturalYtelse.getPeriode();
            var type = KodeverkMapper.mapNaturalYtelseTilDto(naturalYtelse.getType());
            var beløpPerMnd = naturalYtelse.getBeloepPerMnd().getVerdi();
            return new NaturalytelseDto(new Periode(periode.getFomDato(), periode.getTomDato()), type, beløpPerMnd);
        }

        private UtsettelsePeriodeDto mapUtsettelsePeriode(UtsettelsePeriode utsettelsePeriode) {
            var periode = utsettelsePeriode.getPeriode();
            var utsettelseÅrsak = KodeverkMapper.mapUtsettelseÅrsakTilDto(utsettelsePeriode.getÅrsak());
            return new UtsettelsePeriodeDto(new Periode(periode.getFomDato(), periode.getTomDato()), utsettelseÅrsak);
        }

        private Aktør mapAktør(Arbeidsgiver arbeidsgiverEntitet) {
            return arbeidsgiverEntitet.erAktørId()
                ? new AktørIdPersonident(arbeidsgiverEntitet.getAktørId().getId())
                : new Organisasjon(arbeidsgiverEntitet.getOrgnr().getId());
        }

        private ArbeidsforholdRefDto mapArbeidsforholdsId(@SuppressWarnings("unused") Arbeidsgiver arbeidsgiver, InternArbeidsforholdRef internRef, EksternArbeidsforholdRef eksternRef) {
            if ((internRef == null || internRef.getReferanse() == null) && (eksternRef == null || eksternRef.getReferanse() == null)) {
                return null;
            } else if (internRef != null && eksternRef != null && internRef.getReferanse() != null && eksternRef.getReferanse() != null) {
                return new ArbeidsforholdRefDto(internRef.getReferanse(), eksternRef.getReferanse(),
                    no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.Fagsystem.AAREGISTERET);
            } else {
                throw new IllegalStateException(
                    "Både internArbeidsforholdRef og eksternArbeidsforholdRef må være satt (eller begge ikke satt), har nå internRef=" + internRef
                        + ", eksternRef=" + eksternRef);
            }
        }
    }

    public static class MapFraDto {

        public InntektsmeldingAggregatEntitet map(ArbeidsforholdInformasjonBuilder arbeidsforholdInformasjon, InntektsmeldingerDto dto) {
            if (dto == null)
                return null;
            var inntektsmeldingAggregat = new InntektsmeldingAggregatEntitet();
            dto.getInntektsmeldinger().stream().map(im -> mapInntektsmelding(arbeidsforholdInformasjon, im)).forEach(inntektsmeldingAggregat::leggTil);
            return inntektsmeldingAggregat;
        }

        private Inntektsmelding mapInntektsmelding(ArbeidsforholdInformasjonBuilder arbeidsforholdInformasjon, InntektsmeldingDto dto) {
            var arbeidsforholdRef = dto.getArbeidsforholdRef();
            var internRef = InternArbeidsforholdRef.ref(arbeidsforholdRef == null ? null : arbeidsforholdRef.getAbakusReferanse());
            var eksternRef = EksternArbeidsforholdRef.ref(arbeidsforholdRef == null ? null : arbeidsforholdRef.getEksternReferanse());
            var arbeidsgiver = mapArbeidsgiver(dto.getArbeidsgiver());
            if (arbeidsforholdRef != null && eksternRef.gjelderForSpesifiktArbeidsforhold()) {
                arbeidsforholdInformasjon.leggTilNyReferanse(new ArbeidsforholdReferanseEntitet(arbeidsgiver, internRef, eksternRef));
            }
            var journalpostId = dto.getJournalpostId().getId();
            var innsendingstidspunkt = dto.getInnsendingstidspunkt().toLocalDateTime();
            var innsendingsårsak = KodeverkMapper.mapInntektsmeldingInnsendingsårsakFraDto(dto.getInnsendingsårsak());

            var builder = InntektsmeldingBuilder.builder()
                .medJournalpostId(journalpostId)
                .medArbeidsgiver(mapArbeidsgiver(dto.getArbeidsgiver()))
                .medInnsendingstidspunkt(innsendingstidspunkt)
                .medBeløp(dto.getInntektBeløp())
                .medArbeidsforholdId(eksternRef)
                .medArbeidsforholdId(internRef)
                .medStartDatoPermisjon(dto.getStartDatoPermisjon())
                .medRefusjon(dto.getRefusjonsBeløpPerMnd(), dto.getRefusjonOpphører())
                .medKanalreferanse(dto.getKanalreferanse())
                .medInntektsmeldingaarsak(innsendingsårsak)
                .medNærRelasjon(dto.isNærRelasjon() == null ? false : dto.isNærRelasjon())
                .medKildesystem(dto.getKildesystem())
                .medMottattDato(dto.getMottattDato());

            dto.getEndringerRefusjon().stream()
                .map(eir -> new RefusjonEntitet(eir.getRefusjonsbeløpMnd(), eir.getFom()))
                .forEach(builder::leggTil);

            dto.getGraderinger().stream()
                .map(gr -> {
                    var periode = gr.getPeriode();
                    return new GraderingEntitet(periode.getFom(), periode.getTom(), gr.getArbeidstidProsent());
                })
                .forEach(builder::leggTil);

            dto.getNaturalytelser().stream()
                .map(ny -> {
                    var periode = ny.getPeriode();
                    var naturalYtelseType = KodeverkMapper.mapNaturalYtelseFraDto(ny.getType());
                    return new NaturalYtelseEntitet(periode.getFom(), periode.getTom(), ny.getBeløpPerMnd(), naturalYtelseType);
                })
                .forEach(builder::leggTil);

            dto.getUtsettelsePerioder().stream()
                .map(up -> {
                    var periode = up.getPeriode();
                    var utsettelseÅrsak = KodeverkMapper.mapUtsettelseÅrsakFraDto(up.getUtsettelseÅrsakDto());
                    return UtsettelsePeriodeEntitet.utsettelse(periode.getFom(), periode.getTom(), utsettelseÅrsak);
                })
                .forEach(builder::leggTil);

            return builder.build();
        }

        private Arbeidsgiver mapArbeidsgiver(Aktør arbeidsgiverDto) {
            if (arbeidsgiverDto == null) {
                return null;
            }
            String identifikator = arbeidsgiverDto.getIdent();
            if (arbeidsgiverDto.getErOrganisasjon()) {
                return Arbeidsgiver.virksomhet(new OrgNummer(identifikator));
            }
            if (arbeidsgiverDto.getErPerson()) {
                return Arbeidsgiver.person(new AktørId(identifikator));
            }
            throw new IllegalArgumentException();
        }

    }

}
