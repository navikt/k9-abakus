# syntax=docker/dockerfile:1.7.0-labs
FROM ghcr.io/navikt/k9-felles/felles-java-chainguard-25:12.3.1 AS duplikatfjerner

COPY --link --exclude=no.nav.k9.abakus* web/target/lib/ /build/lib/
USER root
RUN ["java", "scripts/RyddBiblioteker", "DUPLIKAT", "/app/lib", "/build/lib"]



FROM ghcr.io/navikt/k9-felles/felles-java-chainguard-25:12.3.1
LABEL org.opencontainers.image.source=https://github.com/navikt/k9-abakus

COPY --link --from=duplikatfjerner /build/lib/ /app/lib/
USER root
RUN ["java", "scripts/RyddBiblioteker", "UBRUKT", "/app/lib"]
USER java

COPY --link web/target/classes/logback*.xml /app/conf/
##kopier prosjektets moduler
COPY --link web/target/lib/no.nav.k9.abakus* /app/lib/
COPY --link web/target/app.jar /app/

EXPOSE 8015

# JDK_JAVA_OPTIONS kommer fra baseimaget og skal ikke overskrives. App-spesifikke JVM-flagg legges i CMD.
CMD ["--enable-preview", "-Dlogback.configurationFile=conf/logback.xml", "-jar", "app.jar"]
