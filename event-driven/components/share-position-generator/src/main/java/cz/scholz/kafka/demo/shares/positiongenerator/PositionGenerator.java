package cz.scholz.kafka.demo.shares.positiongenerator;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PositionGenerator extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(PositionGenerator.class.getName());

    private final Random random = new Random();

    private final PositionGeneratorConfig verticleConfig;
    private final List<ShareMasterData> masterData;
    private KafkaProducer<String, Integer> producer;

    public PositionGenerator(PositionGeneratorConfig verticleConfig, List<ShareMasterData> masterData) {
        log.info("Creating PositionGenerator");
        this.verticleConfig = verticleConfig;
        this.masterData = masterData;
    }

    /*
    Start the verticle
     */
    @Override
    public void start(Future<Void> start) {
        log.info("Starting PositionGenerator");

        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", verticleConfig.getBootstrapServers());
        config.put("acks", verticleConfig.getAcks());
        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("value.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");

        if (verticleConfig.getTrustStorePassword() != null && verticleConfig.getTrustStorePath() != null)   {
            log.info("Configuring truststore");
            config.put("security.protocol", "SSL");
            config.put("ssl.truststore.type", "PKCS12");
            config.put("ssl.truststore.password", verticleConfig.getTrustStorePassword());
            config.put("ssl.truststore.location", verticleConfig.getTrustStorePath());
        }

        if (verticleConfig.getKeyStorePassword() != null && verticleConfig.getKeyStorePath() != null)   {
            log.info("Configuring keystore");
            config.put("security.protocol", "SSL");
            config.put("ssl.keystore.type", "PKCS12");
            config.put("ssl.keystore.password", verticleConfig.getKeyStorePassword());
            config.put("ssl.keystore.location", verticleConfig.getKeyStorePath());
        }

        if (verticleConfig.getUsername() != null && verticleConfig.getPassword() != null)   {
            config.put("sasl.mechanism","SCRAM-SHA-512");
            config.put("sasl.jaas.config", "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"" + verticleConfig.getUsername() + "\" password=\"" + verticleConfig.getPassword() + "\";");

            if (config.get("security.protocol") != null && config.get("security.protocol").equals("SSL"))  {
                config.put("security.protocol","SASL_SSL");
            } else {
                config.put("security.protocol","SASL_PLAINTEXT");
            }
        }

        producer = KafkaProducer.create(vertx, config, String.class, Integer.class);
        producer.exceptionHandler(res -> {
            log.error("Received exception", res);
        });

        vertx.setPeriodic(verticleConfig.getTimer(), res -> {
            sendMessage();
        });

        start.complete();
        sendMessage();
    }

    private void sendMessage() {
        ShareMasterData share = masterData.get(random.nextInt(masterData.size()));

        int position = ((10 - random.nextInt(20)) * 10);

        KafkaProducerRecord<String, Integer> record = KafkaProducerRecord.create(verticleConfig.getTopic(), share.getCode(), position);
        producer.write(record, res2 -> {
            log.info("New position generted for {}: {}", record.key(), record.value());
        });
    }

    /*
    Stop the verticle
     */
    @Override
    public void stop(Future<Void> stopFuture) {
        log.info("Stopping the PositionGenerator.");
        producer.close(res -> {
            stopFuture.complete();
        });
    }
}
