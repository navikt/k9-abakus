create table iay_oppgitt_ytelse (
    id                      bigint                                          not null constraint pk_oppgitt_ytelse primary key,
    oppgitt_opptjening_id   bigint                                          not null constraint fk_oppgitt_ytelse references iay_oppgitt_opptjening,
    ytelse                  numeric(10, 2)                                  not null,
    fom                     date                                            not null,
    tom                     date                                            not null,
    opprettet_av            varchar(20)  default 'VL'::character varying    not null,
    opprettet_tid           timestamp(3) default LOCALTIMESTAMP             not null,
    endret_av               varchar(20),
    endret_tid              timestamp(3),
    versjon                 bigint       default 0                          not null
);


comment on table iay_oppgitt_ytelse is 'Oppgitt informasjon om ytelse';
comment on column iay_oppgitt_ytelse.id is 'Primærnøkkel';
comment on column iay_oppgitt_ytelse.oppgitt_opptjening_id is 'FK:';
comment on column iay_oppgitt_ytelse.ytelse is 'Mottatt ytelse i perioden';
comment on column iay_oppgitt_ytelse.fom is 'Fom-dato for oppgitt ytelse';
comment on column iay_oppgitt_ytelse.tom is 'Tom-dato for oppgitt ytelse';

create index idx_oppgitt_ytelse_oppgitt_opptjening
    on iay_oppgitt_ytelse (oppgitt_opptjening_id);

create sequence seq_oppgitt_ytelse
    start with 1000000
    increment by 50;
