package no.nav.k9.abakus.domene.iay.søknad;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import no.nav.k9.abakus.felles.diff.ChangeTracked;
import no.nav.k9.abakus.felles.jpa.BaseEntitet;

@Entity(name = "OppgittOpptjeningAggregat")
@Table(name = "IAY_OPPGITTE_OPPTJENINGER")
public class OppgittOpptjeningAggregat extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_IAY_OPPGITTE_OPPTJENINGER")
    private Long id;

    @OneToMany(mappedBy = "oppgitteOpptjeninger")
    @ChangeTracked
    private Set<OppgittOpptjening> oppgitteOpptjeninger = new HashSet<>();

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public OppgittOpptjeningAggregat() {
    }

    private OppgittOpptjeningAggregat(Collection<OppgittOpptjening> oppgitteOpptjeninger) {
        this.oppgitteOpptjeninger.addAll(oppgitteOpptjeninger.stream().map(oppgittOpptjening -> {
            var kopi = new OppgittOpptjening(oppgittOpptjening);
            kopi.setOppgitteOpptjeninger(this);
            return kopi;
        }).collect(Collectors.toList()));
    }

    public static OppgittOpptjeningAggregat ny(OppgittOpptjening oppgittOpptjening) {
        return new OppgittOpptjeningAggregat(List.of(oppgittOpptjening));
    }

    public static OppgittOpptjeningAggregat oppdater(OppgittOpptjeningAggregat gammel, OppgittOpptjening oppgittOpptjening) {
        if (gammel.getOppgitteOpptjeninger()
            .stream()
            .map(OppgittOpptjening::getJournalpostId)
            .anyMatch(o -> o.equals(oppgittOpptjening.getJournalpostId()))) {
            throw new IllegalArgumentException(
                "Har allerede journalpostId " + oppgittOpptjening.getJournalpostId() + " registrert, kan ikke legge til");
        }
        List<OppgittOpptjening> opptjeninger = new ArrayList<>();
        opptjeninger.addAll(gammel.oppgitteOpptjeninger);
        opptjeninger.add(oppgittOpptjening);
        return new OppgittOpptjeningAggregat(opptjeninger);
    }

    /**
     * Get alle oppgitte opptjeninger
     *
     * @return
     */
    public Collection<OppgittOpptjening> getOppgitteOpptjeninger() {
        return Collections.unmodifiableCollection(oppgitteOpptjeninger);
    }

    public Long getId() {
        return id;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof OppgittOpptjeningAggregat)) {
            return false;
        }
        var that = (OppgittOpptjeningAggregat) o;
        return Objects.equals(oppgitteOpptjeninger, that.oppgitteOpptjeninger);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oppgitteOpptjeninger);
    }

    @Override
    public String toString() {
        return "OppgittOpptjeningAggregat{" + "oppgitteOpptjeninger=" + oppgitteOpptjeninger + '}';
    }
}
