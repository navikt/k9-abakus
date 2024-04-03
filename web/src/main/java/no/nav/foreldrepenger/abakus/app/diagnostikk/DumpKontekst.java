package no.nav.k9.abakus.app.diagnostikk;

import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.k9.abakus.kobling.Kobling;
import no.nav.k9.abakus.typer.PersonIdent;
import no.nav.k9.abakus.typer.Saksnummer;

public class DumpKontekst {

    private Kobling kobling;
    private PersonIdent ident;

    public DumpKontekst(Kobling kobling, PersonIdent ident) {
        this.kobling = kobling;
        this.ident = ident;
    }

    public Kobling getKobling() {
        return this.kobling;
    }

    public PersonIdent getIdent() {
        return this.ident;
    }

    public YtelseType getYtelseType() {
        return kobling.getYtelseType();
    }

    public Saksnummer getSaksnummer() {
        return kobling.getSaksnummer();
    }
}
