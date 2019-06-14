package no.nav.foreldrepenger.abakus.vedtak.kafka;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.LogAndFailExceptionHandler;
import org.apache.kafka.streams.kstream.Consumed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.apptjeneste.AppServiceHandler;

@ApplicationScoped
public class VedtakConsumer implements AppServiceHandler {

    private static final Logger log = LoggerFactory.getLogger(VedtakConsumer.class);
    private KafkaStreams stream;
    private String topic;

    VedtakConsumer() {
    }

    @Inject
    public VedtakConsumer(VedtaksHendelseHåndterer vedtaksHendelseHåndterer, VedtakStreamKafkaProperties streamKafkaProperties) {
        this.topic = streamKafkaProperties.getTopic();

        Properties props = setupProperties(streamKafkaProperties);

        final StreamsBuilder builder = new StreamsBuilder();

        Consumed<String, String> stringStringConsumed = Consumed.with(Topology.AutoOffsetReset.EARLIEST);
        builder.stream(this.topic, stringStringConsumed)
            .foreach(vedtaksHendelseHåndterer::handleMessage);

        final Topology topology = builder.build();
        stream = new KafkaStreams(topology, props);
    }

    private void addShutdownHooks() {
        stream.setStateListener((newState, oldState) -> {
            log.info("From state={} to state={}", oldState, newState);

            if (newState == KafkaStreams.State.ERROR) {
                // if the stream has died there is no reason to keep spinning
                log.warn("No reason to keep living, closing stream");
                stop();
            }
        });
        stream.setUncaughtExceptionHandler((t, e) -> {
            log.error("Caught exception in stream, exiting", e);
            stop();
        });
    }

    private Properties setupProperties(VedtakStreamKafkaProperties streamProperties) {
        Properties props = new Properties();

        log.info("Consuming topic='{}' with applicationId='{}'", streamProperties.getTopic(), streamProperties.getApplicationId());
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, streamProperties.getApplicationId());
        props.put(StreamsConfig.CLIENT_ID_CONFIG, streamProperties.getClientId());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, streamProperties.getBootstrapServers());

        // Sikkerhet
        if (streamProperties.harSattBrukernavn()) {
            log.info("Using user name {} to authenticate against Kafka brokers ", streamProperties.getUsername());
            props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
            String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
            props.put(SaslConfigs.SASL_JAAS_CONFIG, String.format(jaasTemplate, streamProperties.getUsername(), streamProperties.getPassword()));
        }

        // Setup truststore? Skal det settes opp?
        if (streamProperties.harSattTrustStore()) {
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
            props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, streamProperties.getTrustStorePath());
            props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, streamProperties.getTrustStorePassword());
        }

        // Setup schema-registry
        if (streamProperties.getSchemaRegistryUrl() != null) {
            props.put("schema.registry.url", streamProperties.getSchemaRegistryUrl());
        }

        // Serde
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, streamProperties.getKeyClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, streamProperties.getValueClass());
        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndFailExceptionHandler.class);

        // Polling
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "200");
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "100000");

        return props;
    }

    @Override
    public void start() {
        addShutdownHooks();

        stream.start();
        log.info("Starter konsumering av topic={}, tilstand={}", topic, stream.state());
    }

    public KafkaStreams.State getTilstand() {
        return stream.state();
    }

    public String getTopic() {
        return topic;
    }

    @Override
    public void stop() {
        log.info("Starter shutdown av topic={}, tilstand={} med 10 sekunder timeout", topic, stream.state());
        stream.close(60, TimeUnit.SECONDS);
        log.info("Shutdown av topic={}, tilstand={} med 10 sekunder timeout", topic, stream.state());
    }
}
