package cz.scholz.kafka.demo.shares.pricegenerator;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PriceGenerator extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(PriceGenerator.class.getName());

    private final Random random = new Random();

    private final PriceGeneratorConfig verticleConfig;
    private final List<ShareMasterData> masterData;
    private KafkaProducer<String, Float> producer;

    public PriceGenerator(PriceGeneratorConfig verticleConfig, List<ShareMasterData> masterData) {
        log.info("Creating PriceGenerator");
        this.verticleConfig = verticleConfig;
        this.masterData = masterData;
    }

    /*
    Start the verticle
     */
    @Override
    public void start(Future<Void> start) {
        log.info("Starting PriceGenerator");

        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", verticleConfig.getBootstrapServers());
        config.put("acks", verticleConfig.getAcks());
        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("value.serializer", "org.apache.kafka.common.serialization.FloatSerializer");

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

        producer = KafkaProducer.create(vertx, config, String.class, Float.class);
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
        masterData.stream().forEach(share -> {
            float oldPrice = share.getPrice();
            float change = ((100f - random.nextInt(200)) / 100f) * (share.getMaxChange() / 100f);
            float newPrice = Math.round(share.getPrice() * (1f + change) * 100f) / 100f;
            share.setPrice(newPrice);

            KafkaProducerRecord<String, Float> record = KafkaProducerRecord.create(verticleConfig.getTopic(), share.getCode(), share.getPrice());
            producer.write(record, res2 -> {
                log.info("New price generated for {}: {} (change from {})", record.key(), record.value(), oldPrice);
            });
        });
    }

    /*
    Stop the verticle
     */
    @Override
    public void stop(Future<Void> stopFuture) {
        log.info("Stopping the PriceGenerator.");
        producer.close(res -> {
            stopFuture.complete();
        });
    }
}
