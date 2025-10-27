CREATE  TABLE  VEDTAK_YTELSE_FEIL_DUMP (
                                           id                   bigint                                       not null
                                               constraint pk_vedtak_ytelse_feil_dump
                                                   primary key,
                                           aktoer_id            varchar(50)                                  not null,
                                           ytelse_type          varchar(100)                                 not null,
                                           vedtatt_tidspunkt    timestamp(3)                                 not null,
                                           vedtak_referanse     uuid                                         not null,
                                           fom                  date                                         not null,
                                           tom                  date                                         not null,
                                             har_sykepenger          boolean                                 not null,
                                           har_foreldrepenger          boolean                                 not null,
                                           saksnummer           varchar(19)
);

create sequence seq_vedtak_ytelse_feil_dump
    start with 1000000
    increment by 50;
