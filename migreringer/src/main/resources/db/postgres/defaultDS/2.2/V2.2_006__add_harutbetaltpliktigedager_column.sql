-- Add harutbetaltpliktigedager column to iay_inntektsmelding table
ALTER TABLE iay_inntektsmelding ADD COLUMN har_utbetalt_pliktige_dager VARCHAR(1) default 'N'::character varying not null;

-- Add comment for the new column
COMMENT ON COLUMN iay_inntektsmelding.har_utbetalt_pliktige_dager IS 'Arbeidsgiver oppgir om de har utbetalt pliktige dager, for omsorgspenger.';
