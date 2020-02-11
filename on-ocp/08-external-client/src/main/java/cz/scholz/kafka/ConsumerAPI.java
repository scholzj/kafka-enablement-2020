package cz.scholz.kafka;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.config.SslConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Properties;
import java.util.regex.Pattern;

public class ConsumerAPI {
    private static Logger LOG = LoggerFactory.getLogger(cz.scholz.kafka.ConsumerAPI.class);

    public static void main(String[] args) {
        /*
         * Configure the logger
         */
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");

        /*
         * Configure the consumer
         */
        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "my-cluster-kafka-bootstrap-myproject.apps.jscholz.rhmw-integrations.net:443");

        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "external-group");
        props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        props.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        props.setProperty(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "../cluster.p12");
        props.setProperty(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "kjjJ0RE7ICH0");
        props.setProperty(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "PKCS12");
        props.setProperty(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "../user.p12");
        props.setProperty(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "gIY4dACpwpbz");
        props.setProperty(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PKCS12");
        props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "HTTPS");

        /*
         * Create the consumer and subscribe to topics
         */
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
        consumer.subscribe(Pattern.compile("kafka-test-apps"));

        /*
         * Consume 100 messages or stop when poll returns null
         */
        int i = 0;
        while (i < 100)    {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(60));

            if (records.isEmpty()) {
                LOG.warn("No messages received");
                break;
            }

            records.forEach(record -> {
                LOG.info("Received message with key {} and value {}", record.key(), record.value());
            });
        }

        /*
         * Close the consumer
         */
        consumer.close();
    }
}
