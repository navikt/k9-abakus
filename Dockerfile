FROM ghcr.io/navikt/sif-baseimages/java-21:2025.06.02.0726Z

LABEL org.opencontainers.image.source=https://github.com/navikt/k9-abakus
ENV TZ=Europe/Oslo

ENV JAVA_OPTS="-Djava.security.egd=file:/dev/urandom \
    -XX:-OmitStackTraceInFastThrow \
    -Dlogback.configurationFile=conf/logback.xml"

# Application Start Konfigurasjon
COPY build/init-app.sh /init-scripts/init-app.sh

# Config
COPY web/target/classes/logback*.xml ./conf/

# Application Container (Jetty)
COPY web/target/lib/*.jar ./lib/
COPY web/target/app.jar ./
