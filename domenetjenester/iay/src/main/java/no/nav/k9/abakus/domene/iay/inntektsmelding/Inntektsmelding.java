package no.nav.k9.abakus.domene.iay.inntektsmelding;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektsmeldingInnsendingsårsakType;
import no.nav.k9.abakus.domene.iay.Arbeidsgiver;
import no.nav.k9.abakus.domene.iay.InntektsmeldingAggregat;
import no.nav.k9.abakus.felles.diff.ChangeTracked;
import no.nav.k9.abakus.felles.diff.IndexKeyComposer;
import no.nav.k9.abakus.felles.jpa.BaseEntitet;
import no.nav.k9.abakus.iay.jpa.InntektsmeldingInnsendingsårsakKodeverdiConverter;
import no.nav.k9.abakus.typer.Beløp;
import no.nav.k9.abakus.typer.InternArbeidsforholdRef;
import no.nav.k9.abakus.typer.JournalpostId;
import no.nav.k9.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "Inntektsmelding")
@Table(name = "IAY_INNTEKTSMELDING")
public class Inntektsmelding extends BaseEntitet implements IndexKey {

    public static final Comparator<? super Inntektsmelding> COMP_REKKEFØLGE = (Inntektsmelding a, Inntektsmelding b) -> {
        if (a == b) {
            return 0;
        }
        if (a == null) {
            return -1;
        }
        if (b == null) {
            return 1;
        }
        if (a.erFraNavNo() || b.erFraNavNo() || a.getKanalreferanse() == null || b.getKanalreferanse() == null) {
            // For inntektsmeldinger fra nav.no bruker vi innsendingstidspunkt
            return a.getInnsendingstidspunkt().compareTo(b.getInnsendingstidspunkt());
        }
        // For inntektsmeldinger fra Altinn bruker vi kanalreferanse
        return a.getKanalreferanse().compareTo(b.getKanalreferanse());
    };

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_INNTEKTSMELDING")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inntektsmeldinger_id", nullable = false, updatable = false)
    private InntektsmeldingAggregat inntektsmeldinger;

    @OneToMany(mappedBy = "inntektsmelding")
    @ChangeTracked
    private List<Gradering> graderinger = new ArrayList<>();

    @OneToMany(mappedBy = "inntektsmelding")
    @ChangeTracked
    private List<NaturalYtelse> naturalYtelser = new ArrayList<>();

    @OneToMany(mappedBy = "inntektsmelding")
    @ChangeTracked
    private List<Fravær> oppgittFravær = new ArrayList<>();

    @OneToMany(mappedBy = "inntektsmelding")
    @ChangeTracked
    private List<UtsettelsePeriode> utsettelsePerioder = new ArrayList<>();

    @Embedded
    @ChangeTracked
    private Arbeidsgiver arbeidsgiver;

    @Embedded
    @ChangeTracked
    private InternArbeidsforholdRef arbeidsforholdRef;

    @Column(name = "start_dato_permisjon", updatable = false, nullable = false)
    @ChangeTracked
    private LocalDate startDatoPermisjon;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "naer_relasjon", updatable = false, nullable = false)
    private boolean nærRelasjon;

    @Embedded
    private JournalpostId journalpostId;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "inntekt_beloep", nullable = false)))
    @ChangeTracked
    private Beløp inntektBeløp;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "refusjon_beloep", updatable = false)))
    @ChangeTracked
    private Beløp refusjonBeløpPerMnd;

    @Column(name = "refusjon_opphoerer", updatable = false)
    @ChangeTracked
    private LocalDate refusjonOpphører;

    @Column(name = "innsendingstidspunkt", updatable = false, nullable = false)
    private LocalDateTime innsendingstidspunkt;

    @Column(name = "kanalreferanse")
    private String kanalreferanse;

    @Column(name = "kildesystem")
    private String kildesystem;

    @Column(name = "mottatt_dato", nullable = false, updatable = false)
    private LocalDate mottattDato;

    @OneToMany(mappedBy = "inntektsmelding")
    @ChangeTracked
    private List<Refusjon> endringerRefusjon = new ArrayList<>();

    @Convert(converter = InntektsmeldingInnsendingsårsakKodeverdiConverter.class)
    @Column(name = "innsendingsaarsak", nullable = false, updatable = false)
    @ChangeTracked
    private InntektsmeldingInnsendingsårsakType innsendingsårsak = InntektsmeldingInnsendingsårsakType.UDEFINERT;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    Inntektsmelding() {
    }

    /**
     * copy ctor.
     */
    public Inntektsmelding(Inntektsmelding inntektsmelding) {
        this.arbeidsgiver = inntektsmelding.getArbeidsgiver();
        this.arbeidsforholdRef = inntektsmelding.getArbeidsforholdRef();
        this.startDatoPermisjon = inntektsmelding.getStartDatoPermisjon();
        this.nærRelasjon = inntektsmelding.getErNærRelasjon();
        this.journalpostId = inntektsmelding.getJournalpostId();
        this.inntektBeløp = inntektsmelding.getInntektBeløp();
        this.refusjonBeløpPerMnd = inntektsmelding.getRefusjonBeløpPerMnd();
        this.refusjonOpphører = inntektsmelding.getRefusjonOpphører();
        this.innsendingsårsak = inntektsmelding.getInntektsmeldingInnsendingsårsak();
        this.innsendingstidspunkt = inntektsmelding.getInnsendingstidspunkt();
        this.kanalreferanse = inntektsmelding.getKanalreferanse();
        this.kildesystem = inntektsmelding.getKildesystem();
        this.mottattDato = inntektsmelding.getMottattDato();

        this.graderinger = inntektsmelding.getGraderinger().stream().map(g -> {
            var data = new Gradering(g);
            data.setInntektsmelding(this);
            return data;
        }).collect(Collectors.toList());
        this.naturalYtelser = inntektsmelding.getNaturalYtelser().stream().map(n -> {
            var data = new NaturalYtelse(n);
            data.setInntektsmelding(this);
            return data;
        }).collect(Collectors.toList());
        this.utsettelsePerioder = inntektsmelding.getUtsettelsePerioder().stream().map(u -> {
            var data = new UtsettelsePeriode(u);
            data.setInntektsmelding(this);
            return data;
        }).collect(Collectors.toList());
        this.endringerRefusjon = inntektsmelding.getEndringerRefusjon().stream().map(r -> {
            var data = new Refusjon(r);
            data.setInntektsmelding(this);
            return data;
        }).collect(Collectors.toList());
        this.oppgittFravær = inntektsmelding.getOppgittFravær().stream().map(f -> {
            var data = new Fravær(f);
            data.setInntektsmelding(this);
            return data;
        }).collect(Collectors.toList());
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = {arbeidsgiver, arbeidsforholdRef};
        return IndexKeyComposer.createKey(keyParts);
    }

    public void setInntektsmeldinger(InntektsmeldingAggregat inntektsmeldinger) {
        this.inntektsmeldinger = inntektsmeldinger;
    }

    /**
     * Arbeidsgiveren som har sendt inn inntektsmeldingen
     *
     * @return {@link ArbeidsgiverEntitet}
     */
    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    void setArbeidsgiver(Arbeidsgiver virksomhet) {
        this.arbeidsgiver = virksomhet;
    }

    public InntektsmeldingInnsendingsårsakType getInntektsmeldingInnsendingsårsak() {
        return innsendingsårsak;
    }

    void setInntektsmeldingInnsendingsårsak(InntektsmeldingInnsendingsårsakType innsendingsårsak) {
        this.innsendingsårsak = innsendingsårsak;
    }

    public LocalDateTime getInnsendingstidspunkt() {
        return innsendingstidspunkt;
    }

    void setInnsendingstidspunkt(LocalDateTime innsendingstidspunkt) {
        this.innsendingstidspunkt = innsendingstidspunkt;
    }

    public String getKanalreferanse() {
        return kanalreferanse;
    }

    void setKanalreferanse(String kanalreferanse) {
        this.kanalreferanse = kanalreferanse;
    }

    /**
     * Dato inntektsmelding mottatt i NAV (tilsvarer dato lagret i Joark).
     */
    public LocalDate getMottattDato() {
        return mottattDato;
    }

    void setMottattDato(LocalDate mottattDato) {
        this.mottattDato = mottattDato;
    }

    public String getKildesystem() {
        return kildesystem;
    }

    public boolean erFraNavNo() {
        return Objects.equals(getKildesystem(), "NAV_NO");
    }

    void setKildesystem(String kildesystem) {
        this.kildesystem = kildesystem;
    }

    public List<Fravær> getOppgittFravær() {
        return Collections.unmodifiableList(oppgittFravær);
    }

    /**
     * Liste over perioder med graderinger
     *
     * @return {@link Gradering}
     */
    public List<Gradering> getGraderinger() {
        return Collections.unmodifiableList(graderinger);
    }

    /**
     * Liste over naturalytelser
     *
     * @return {@link NaturalYtelse}
     */
    public List<NaturalYtelse> getNaturalYtelser() {
        return Collections.unmodifiableList(naturalYtelser);
    }

    /**
     * Liste over utsettelse perioder
     *
     * @return {@link UtsettelsePeriode}
     */
    public List<UtsettelsePeriode> getUtsettelsePerioder() {
        return Collections.unmodifiableList(utsettelsePerioder);
    }

    /**
     * Arbeidsgivers arbeidsforhold referanse
     *
     * @return {@link ArbeidsforholdRef}
     */
    public InternArbeidsforholdRef getArbeidsforholdRef() {
        return arbeidsforholdRef != null ? arbeidsforholdRef : InternArbeidsforholdRef.nullRef();
    }

    /**
     * Gjelder for et spesifikt arbeidsforhold
     *
     * @return {@link Boolean}
     */
    public boolean gjelderForEtSpesifiktArbeidsforhold() {
        return getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold();
    }

    public boolean gjelderSammeArbeidsforhold(Inntektsmelding annen) {
        return getArbeidsgiver().equals(annen.getArbeidsgiver()) && getArbeidsforholdRef().gjelderFor(annen.getArbeidsforholdRef());
    }

    /**
     * Setter intern arbeidsdforhold Id for inntektsmelding
     *
     * @param arbeidsforholdRef Intern arbeidsforhold id
     */
    void setArbeidsforholdId(InternArbeidsforholdRef arbeidsforholdRef) {
        this.arbeidsforholdRef = arbeidsforholdRef != null ? arbeidsforholdRef : InternArbeidsforholdRef.nullRef();
    }

    /**
     * Startdato for permisjonen
     *
     * @return {@link LocalDate}
     */
    public LocalDate getStartDatoPermisjon() {
        return startDatoPermisjon;
    }

    void setStartDatoPermisjon(LocalDate startDatoPermisjon) {
        this.startDatoPermisjon = startDatoPermisjon;
    }

    /**
     * Referanse til {@link no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument} som benyttes for å markere
     * hvilke dokument som er gjeldende i behandlingen
     *
     * @return {@link Long}
     */
    public JournalpostId getJournalpostId() {
        return journalpostId;
    }

    void setJournalpostId(JournalpostId journalpostId) {
        this.journalpostId = journalpostId;
    }

    /**
     * Er det nær relasjon mellom søker og arbeidsgiver
     *
     * @return {@link Boolean}
     */
    public boolean getErNærRelasjon() {
        return nærRelasjon;
    }

    void setNærRelasjon(boolean nærRelasjon) {
        this.nærRelasjon = nærRelasjon;
    }

    /**
     * Oppgitt årsinntekt fra arbeidsgiver
     *
     * @return {@link BigDecimal}
     */
    public Beløp getInntektBeløp() {
        return inntektBeløp;
    }

    void setInntektBeløp(Beløp inntektBeløp) {
        this.inntektBeløp = inntektBeløp;
    }

    /**
     * Beløpet arbeidsgiver ønsker refundert
     *
     * @return {@link BigDecimal}
     */
    public Beløp getRefusjonBeløpPerMnd() {
        return refusjonBeløpPerMnd;
    }

    void setRefusjonBeløpPerMnd(Beløp refusjonBeløpPerMnd) {
        this.refusjonBeløpPerMnd = refusjonBeløpPerMnd;
    }

    /**
     * Dersom refusjonen opphører i stønadsperioden angis siste dag det søkes om refusjon for.
     *
     * @return {@link LocalDate}
     */
    public LocalDate getRefusjonOpphører() {
        return refusjonOpphører;
    }

    void setRefusjonOpphører(LocalDate refusjonOpphører) {
        this.refusjonOpphører = refusjonOpphører;
    }

    /**
     * Liste over endringer i refusjonsbeløp
     *
     * @return {@Link Refusjon}
     */

    public List<Refusjon> getEndringerRefusjon() {
        return Collections.unmodifiableList(endringerRefusjon);
    }

    void leggTil(Gradering gradering) {
        this.graderinger.add(gradering);
        gradering.setInntektsmelding(this);
    }

    void leggTil(NaturalYtelse naturalYtelse) {
        this.naturalYtelser.add(naturalYtelse);
        naturalYtelse.setInntektsmelding(this);
    }

    void leggTil(UtsettelsePeriode utsettelsePeriode) {
        this.utsettelsePerioder.add(utsettelsePeriode);
        utsettelsePeriode.setInntektsmelding(this);
    }

    void leggTil(Refusjon refusjon) {
        this.endringerRefusjon.add(refusjon);
        refusjon.setInntektsmelding(this);
    }

    void leggTil(Fravær fravær) {
        this.oppgittFravær.add(fravær);
        fravær.setInntektsmelding(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof Inntektsmelding)) {
            return false;
        }
        var entitet = (Inntektsmelding) o;
        return Objects.equals(getArbeidsgiver(), entitet.getArbeidsgiver()) && Objects.equals(getJournalpostId(), entitet.getJournalpostId())
            && Objects.equals(getArbeidsforholdRef(), entitet.getArbeidsforholdRef());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getArbeidsgiver(), getArbeidsforholdRef(), getJournalpostId());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + "id=" + id + ", virksomhet=" + arbeidsgiver + ", arbeidsforholdId='" + arbeidsforholdRef + '\''
            + ", startDatoPermisjon=" + startDatoPermisjon + ", nærRelasjon=" + nærRelasjon + ", journalpostId=" + journalpostId + ", inntektBeløp="
            + inntektBeløp + ", refusjonBeløpPerMnd=" + refusjonBeløpPerMnd + ", refusjonOpphører=" + refusjonOpphører + ", innsendingsårsak= "
            + innsendingsårsak + ", innsendingstidspunkt= " + innsendingstidspunkt + ", kanalreferanse=" + kanalreferanse + ", kildesystem="
            + kildesystem + ", mottattDato=" + mottattDato + '>';
    }

}
