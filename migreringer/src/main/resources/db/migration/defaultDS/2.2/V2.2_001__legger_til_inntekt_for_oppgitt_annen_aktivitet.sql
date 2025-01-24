alter table iay_annen_aktivitet add column inntekt numeric(10, 2);
comment on column iay_annen_aktivitet.inntekt is 'oppgitt inntekt for aktivitet i perioden';
