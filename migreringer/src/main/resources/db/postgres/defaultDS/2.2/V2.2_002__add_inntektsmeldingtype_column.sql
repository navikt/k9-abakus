-- Add inntektsmeldingType column to iay_inntektsmelding table
ALTER TABLE iay_inntektsmelding ADD COLUMN inntektsmelding_type VARCHAR(50);

-- Add comment for the new column
COMMENT ON COLUMN iay_inntektsmelding.inntektsmelding_type IS 'Type inntektsmelding';
