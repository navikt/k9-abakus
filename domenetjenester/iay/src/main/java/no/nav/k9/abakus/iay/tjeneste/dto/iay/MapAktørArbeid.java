package no.nav.k9.abakus.iay.tjeneste.dto.iay;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.abakus.iaygrunnlag.Aktør;
import no.nav.abakus.iaygrunnlag.AktørIdPersonident;
import no.nav.abakus.iaygrunnlag.ArbeidsforholdRefDto;
import no.nav.abakus.iaygrunnlag.Organisasjon;
import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.PersonIdent;
import no.nav.abakus.iaygrunnlag.arbeid.v1.AktivitetsAvtaleDto;
import no.nav.abakus.iaygrunnlag.arbeid.v1.ArbeidDto;
import no.nav.abakus.iaygrunnlag.arbeid.v1.PermisjonDto;
import no.nav.abakus.iaygrunnlag.arbeid.v1.YrkesaktivitetDto;
import no.nav.k9.abakus.domene.iay.AktivitetsAvtale;
import no.nav.k9.abakus.domene.iay.AktivitetsAvtaleBuilder;
import no.nav.k9.abakus.domene.iay.AktørArbeid;
import no.nav.k9.abakus.domene.iay.Arbeidsgiver;
import no.nav.k9.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.k9.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder;
import no.nav.k9.abakus.domene.iay.Opptjeningsnøkkel;
import no.nav.k9.abakus.domene.iay.Permisjon;
import no.nav.k9.abakus.domene.iay.PermisjonBuilder;
import no.nav.k9.abakus.domene.iay.Yrkesaktivitet;
import no.nav.k9.abakus.domene.iay.YrkesaktivitetBuilder;
import no.nav.k9.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.k9.abakus.felles.jpa.IntervallEntitet;
import no.nav.k9.abakus.typer.AktørId;
import no.nav.k9.abakus.typer.InternArbeidsforholdRef;
import no.nav.k9.abakus.typer.OrgNummer;

public class MapAktørArbeid {

    private static final Comparator<YrkesaktivitetDto> COMP_YRKESAKTIVITET = Comparator.comparing(
            (YrkesaktivitetDto dto) -> dto.getArbeidsgiver().map(Aktør::getIdent).orElse(null), Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(dto -> dto.getArbeidsforholdId() == null ? null : dto.getArbeidsforholdId().getAbakusReferanse(),
            Comparator.nullsFirst(Comparator.naturalOrder()));

    private static final Comparator<AktivitetsAvtaleDto> COMP_AKTIVITETSAVTALE = Comparator.comparing(
            (AktivitetsAvtaleDto dto) -> dto.getPeriode().getFom(), Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(dto -> dto.getPeriode().getTom(), Comparator.nullsLast(Comparator.naturalOrder()));

    private static final Comparator<PermisjonDto> COMP_PERMISJON = Comparator.comparing((PermisjonDto dto) -> dto.getPeriode().getFom(),
            Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(dto -> dto.getPeriode().getTom(), Comparator.nullsLast(Comparator.naturalOrder()));

    static class MapFraDto {

        @SuppressWarnings("unused")
        private AktørId søkerAktørId;

        private InntektArbeidYtelseAggregatBuilder registerData;

        MapFraDto(AktørId søkerAktørId, InntektArbeidYtelseAggregatBuilder registerData) {
            this.registerData = registerData;
            this.søkerAktørId = søkerAktørId;
        }

        List<AktørArbeidBuilder> map(Collection<ArbeidDto> dtos) {
            if (dtos == null || dtos.isEmpty()) {
                return Collections.emptyList();
            }
            return dtos.stream().map(this::mapAktørArbeid).collect(Collectors.toUnmodifiableList());
        }

        private AktørArbeidBuilder mapAktørArbeid(ArbeidDto dto) {
            var builder = registerData.getAktørArbeidBuilder(tilAktørId(dto.getPerson()));
            dto.getYrkesaktiviteter().forEach(yrkesaktivitetDto -> builder.leggTilYrkesaktivitet(mapYrkesaktivitet(yrkesaktivitetDto, builder)));
            return builder;
        }

        /**
         * Returnerer person sin aktørId. Denne trenger ikke være samme som søkers aktørid men kan f.eks. være annen part i en sak.
         */
        private AktørId tilAktørId(PersonIdent person) {
            if (!(person instanceof AktørIdPersonident)) {
                throw new IllegalArgumentException("Støtter kun " + AktørIdPersonident.class.getSimpleName() + " her");
            }
            return new AktørId(person.getIdent());
        }

        private YrkesaktivitetBuilder mapYrkesaktivitet(YrkesaktivitetDto dto, AktørArbeidBuilder builder) {
            var arbeidsgiver = dto.getArbeidsgiver().map(this::mapArbeidsgiver).orElse(null);
            var internArbeidsforholdRef = arbeidsgiver == null ? null : mapArbeidsforholdRef(arbeidsgiver, dto.getArbeidsforholdId());
            YrkesaktivitetBuilder yrkesaktivitetBuilder;
            if (arbeidsgiver == null) {
                yrkesaktivitetBuilder = builder.getYrkesaktivitetBuilderForType(dto.getType());
            } else {
                Opptjeningsnøkkel nøkkel = Opptjeningsnøkkel.forArbeidsforholdIdMedArbeidgiver(internArbeidsforholdRef, arbeidsgiver);
                yrkesaktivitetBuilder = builder.getYrkesaktivitetBuilderForNøkkelAvType(nøkkel, dto.getType());
            }
            yrkesaktivitetBuilder.medArbeidsforholdId(internArbeidsforholdRef)
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsgiverNavn(dto.getNavnArbeidsgiverUtland())
                .medArbeidType(dto.getType());

            yrkesaktivitetBuilder.tilbakestillAvtaler();
            dto.getAktivitetsAvtaler()
                .forEach(aktivitetsAvtaleDto -> yrkesaktivitetBuilder.leggTilAktivitetsAvtale(mapAktivitetsAvtale(aktivitetsAvtaleDto)));

            yrkesaktivitetBuilder.tilbakestillPermisjon();
            dto.getPermisjoner()
                .forEach(
                    permisjonDto -> yrkesaktivitetBuilder.leggTilPermisjon(mapPermisjon(permisjonDto, yrkesaktivitetBuilder.getPermisjonBuilder())));

            return yrkesaktivitetBuilder;
        }

        private Permisjon mapPermisjon(PermisjonDto dto, PermisjonBuilder permisjonBuilder) {
            return permisjonBuilder.medPeriode(dto.getPeriode().getFom(), dto.getPeriode().getTom())
                .medPermisjonsbeskrivelseType(dto.getType())
                .medProsentsats(dto.getProsentsats())
                .build();
        }

        private AktivitetsAvtaleBuilder mapAktivitetsAvtale(AktivitetsAvtaleDto dto) {
            return AktivitetsAvtaleBuilder.ny()
                .medBeskrivelse(dto.getBeskrivelse())
                .medPeriode(mapPeriode(dto.getPeriode()))
                .medProsentsats(dto.getStillingsprosent())
                .medSisteLønnsendringsdato(dto.getSistLønnsendring());
        }

        private IntervallEntitet mapPeriode(Periode periode) {
            return IntervallEntitet.fraOgMedTilOgMed(periode.getFom(), periode.getTom());
        }

        private InternArbeidsforholdRef mapArbeidsforholdRef(@SuppressWarnings("unused") Arbeidsgiver arbeidsgiver,
                                                             ArbeidsforholdRefDto arbeidsforholdId) {
            if (arbeidsforholdId == null) {
                return InternArbeidsforholdRef.nullRef();
            }
            // intern referanse == abakus referanse.
            return InternArbeidsforholdRef.ref(arbeidsforholdId.getAbakusReferanse());
        }

        private Arbeidsgiver mapArbeidsgiver(Aktør arbeidsgiver) {
            if (arbeidsgiver.getErOrganisasjon()) {
                return Arbeidsgiver.virksomhet(new OrgNummer(arbeidsgiver.getIdent()));
            }
            return Arbeidsgiver.person(new AktørId(arbeidsgiver.getIdent()));
        }
    }

    static class MapTilDto {

        private ArbeidsforholdInformasjon arbeidsforholdInformasjon;

        public MapTilDto(ArbeidsforholdInformasjon arbeidsforholdInformasjon) {
            this.arbeidsforholdInformasjon = arbeidsforholdInformasjon;
        }

        List<ArbeidDto> map(Collection<AktørArbeid> aktørArbeid) {
            if (aktørArbeid == null || aktørArbeid.isEmpty()) {
                return Collections.emptyList();
            }
            return aktørArbeid.stream().map(this::map).collect(Collectors.toList());
        }

        private ArbeidDto map(AktørArbeid arb) {
            List<YrkesaktivitetDto> yrkesaktiviteter = new ArrayList<>(getYrkesaktiviteter(arb.hentAlleYrkesaktiviteter()));

            var aktiviteter = yrkesaktiviteter.stream().filter(this::erGyldigYrkesaktivitet).sorted(COMP_YRKESAKTIVITET).collect(Collectors.toList());

            var dto = new ArbeidDto(new AktørIdPersonident(arb.getAktørId().getId())).medYrkesaktiviteter(aktiviteter);
            return dto;
        }

        private boolean erGyldigYrkesaktivitet(YrkesaktivitetDto yrkesaktivitet) {
            return !yrkesaktivitet.getAktivitetsAvtaler().isEmpty() || !yrkesaktivitet.getPermisjoner().isEmpty();
        }

        private List<YrkesaktivitetDto> getYrkesaktiviteter(Collection<Yrkesaktivitet> aktiviteter) {
            return aktiviteter.stream().map(this::mapYrkesaktivitet).collect(Collectors.toList());
        }

        private AktivitetsAvtaleDto map(AktivitetsAvtale aa) {
            LocalDate fomDato = aa.getPeriodeUtenOverstyring().getFomDato();
            LocalDate tomDato = aa.getPeriodeUtenOverstyring().getTomDato();
            var avtale = new AktivitetsAvtaleDto(fomDato, tomDato).medBeskrivelse(aa.getBeskrivelse())
                .medSistLønnsendring(aa.getSisteLønnsendringsdato())
                .medStillingsprosent(aa.getProsentsats() == null ? null : aa.getProsentsats().getVerdi());
            return avtale;
        }

        private PermisjonDto map(Permisjon p) {
            var permisjonsbeskrivelseType = p.getPermisjonsbeskrivelseType();
            var permisjon = new PermisjonDto(new Periode(p.getFraOgMed(), p.getTilOgMed()), permisjonsbeskrivelseType).medProsentsats(
                p.getProsentsats() == null ? null : p.getProsentsats().getVerdi());
            return permisjon;
        }

        private YrkesaktivitetDto mapYrkesaktivitet(Yrkesaktivitet a) {
            var aktivitetsAvtaler = a.getAlleAktivitetsAvtaler().stream().map(this::map).sorted(COMP_AKTIVITETSAVTALE).collect(Collectors.toList());
            var permisjoner = a.getPermisjon().stream().map(this::map).sorted(COMP_PERMISJON).collect(Collectors.toList());
            var arbeidsforholdId = mapArbeidsforholdsId(a.getArbeidsgiver(), a);

            var arbeidType = a.getArbeidType();
            var dto = new YrkesaktivitetDto(arbeidType).medArbeidsgiver(mapAktør(a.getArbeidsgiver()))
                .medAktivitetsAvtaler(aktivitetsAvtaler)
                .medPermisjoner(permisjoner)
                .medArbeidsforholdId(arbeidsforholdId)
                .medNavnArbeidsgiverUtland(a.getNavnArbeidsgiverUtland());

            return dto;
        }

        private ArbeidsforholdRefDto mapArbeidsforholdsId(Arbeidsgiver arbeidsgiver, Yrkesaktivitet yrkesaktivitet) {
            var internRef = yrkesaktivitet.getArbeidsforholdRef();
            if (internRef == null || internRef.getReferanse() == null) {
                return null;
            }
            var eksternRef = arbeidsforholdInformasjon == null ? null : arbeidsforholdInformasjon.finnEksternRaw(arbeidsgiver, internRef);

            if (eksternRef == null || eksternRef.getReferanse() == null) {
                throw new java.lang.IllegalStateException("Mapping til Abakus: Savner eksternRef for internRef: " + internRef);
            }

            return new ArbeidsforholdRefDto(internRef.getReferanse(), eksternRef.getReferanse(),
                no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem.AAREGISTERET);
        }

        private Aktør mapAktør(Arbeidsgiver arbeidsgiver) {
            if (arbeidsgiver == null) {
                return null; // arbeidType='NÆRING' har null arbeidsgiver
            }
            return arbeidsgiver.erAktørId() ? new AktørIdPersonident(arbeidsgiver.getAktørId().getId()) : new Organisasjon(
                arbeidsgiver.getOrgnr().getId());
        }

    }

}
