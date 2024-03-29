K9-ABAKUS
================
[![Bygg og deploy](https://github.com/navikt/k9-abakus/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/navikt/k9-abakus/actions/workflows/build.yml)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=navikt_k9-abakus&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=navikt_k9-abakus)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=navikt_k9-abakus&metric=coverage)](https://sonarcloud.io/summary/new_code?id=navikt_k9-abakus)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=navikt_k9-abakus&metric=alert_status)](https://sonarcloud.io/dashboard?id=navikt_k9-abakus)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=navikt_k9-abakus&metric=bugs)](https://sonarcloud.io/dashboard?id=navikt_k9-abakus)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=navikt_k9-abakus&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=navikt_k9-abakus)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=navikt_k9-abakus&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=navikt_k9-abakus)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=navikt_k9-abakus&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=navikt_k9-abakus)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=navikt_k9-abakus&metric=sqale_index)](https://sonarcloud.io/dashboard?id=navikt_k9-abakus)

### Abakus kontrakt
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/navikt/k9-abakus)](https://github.com/navikt/k9-abakus/releases)
![GitHub](https://img.shields.io/github/license/navikt/k9-abakus)

Dette er repository for kildkode som dekker innhenting og etablering av grunnlag for IAY(inntekt, arbeid & ytelse). Grunnlaget består av registrerte
inntekter, arbeidsgivere og arbeidsforhold, etablerte ytelser, oppgitt opptjening, og saksbehandlers merknader og evt. bekreftede/skjønnsmessig
fastsatte vurderinger.

### Struktur

Dette er dekker IAY(inntekt, arbeid & ytelse) Pleiepenger og Omsorgspenger (Folketrygdloven kapittel 9). Dette benyttes som underlag for opptjening, hvilke arbeidsaktiviteter bruker har hatt, og beregningsgrunnlag i saksflyt (
ikke del av denne tjenesten)

Hvert grunnlag er immutable, men består av ett eller flere 'aggregater' (DDD terminologi) med hver sin livssyklus (eks. inntektsmeldigner kommer fra
arbeidsgivere, registeropplysninger fra ulike systemer i Nav, Skatt, A-ordningen). Hver endring lagres separat og deduplisert ifht. aggregatene (dvs.
dersom et aggregat ikke endrer seg blir det ikke duplisert, men lages en peker fra grunnlaget til den versjonen som inkluderes).

### Utviklingshåndbok

[Utviklingoppsett](https://confluence.adeo.no/display/LVF/60+Utviklingsoppsett)
[Utviklerhåndbok, Kodestandard, osv](https://confluence.adeo.no/pages/viewpage.action?pageId=190254327)

### Miljøoversikt

[Miljøer](https://confluence.adeo.no/pages/viewpage.action?pageId=193202159)

### Linker

[Foreldrepengeprosjektet på Confluence](http://confluence.adeo.no/display/MODNAV/Foreldrepengeprosjektet)

### Sikkerhet

Det er mulig å kalle tjenesten med bruk av følgende tokens

- Azure CC
- Azure OBO med følgende rettigheter:
    - k9-saksbehandler
    - k9-veileder
    - k9-drift
- STS (fases ut)
