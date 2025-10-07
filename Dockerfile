# syntax=docker/dockerfile:1.7.0-labs
FROM ghcr.io/navikt/k9-felles/felles-java-21:7.1.3 AS duplikatfjerner

COPY --link --exclude=no.nav.k9.abakus* web/target/lib/ /build/lib/
USER root
RUN ["java", "scripts/RyddBiblioteker", "DUPLIKAT", "/app/lib", "/build/lib"]



FROM ghcr.io/navikt/k9-felles/felles-java-21:7.1.3
LABEL org.opencontainers.image.source=https://github.com/navikt/k9-abakus

ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom -XX:-OmitStackTraceInFastThrow -Dlogback.configurationFile=conf/logback.xml -Duser.timezone=Europe/Oslo "

COPY --link --from=duplikatfjerner /build/lib/ /app/lib/
USER root
RUN ["java", "scripts/RyddBiblioteker", "UBRUKT", "/app/lib"]
USER apprunner

COPY --link build/init-app.sh /init-scripts/init-app.sh
COPY --link web/target/classes/logback*.xml /app/conf/
##kopier prosjektets moduler
COPY --link web/target/lib/no.nav.k9.abakus* /app/lib/
COPY --link web/target/app.jar /app/

EXPOSE 8015
