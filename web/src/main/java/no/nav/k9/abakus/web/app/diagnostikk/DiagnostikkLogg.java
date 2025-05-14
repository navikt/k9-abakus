package no.nav.k9.abakus.web.app.diagnostikk;

import no.nav.k9.abakus.felles.jpa.BaseEntitet;
import no.nav.k9.abakus.typer.Saksnummer;

import jakarta.persistence.*;

@Entity(name = "DiagnostikkLogg")
@Table(name = "DIAGNOSTIKK_LOGG")
public class DiagnostikkLogg extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_DIAGNOSTIKK_LOGG")
    @Column(name = "id")
    private Long id;

    @Column(name = "saksnummer", nullable = false, updatable = false, insertable = true)
    private Saksnummer saksnummer;

    DiagnostikkLogg() {
        // Hibernate
    }

    public DiagnostikkLogg(Saksnummer saksnummer) {
        this.saksnummer = saksnummer;
    }

    public Long getId() {
        return id;
    }

    public Saksnummer getSaksnummer() {
        return this.saksnummer;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<saksnummer=" + saksnummer + ">";
    }

    @PreRemove
    protected void onDelete() {
        throw new IllegalStateException("Skal aldri kunne slette. [id=" + id + "]");
    }
}
