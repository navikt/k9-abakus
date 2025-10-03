-- Add inntektsmeldingType column to iay_inntektsmelding table
ALTER TABLE iay_inntektsmelding ADD COLUMN inntektsmeldingtype VARCHAR(50);

-- Add comment for the new column
COMMENT ON COLUMN iay_inntektsmelding.inntektsmeldingtype IS 'Type inntektsmelding';
