package cz.scholz.kafka;

import io.strimzi.kafka.oauth.client.ClientConfig;
import io.strimzi.kafka.oauth.common.Config;
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
         * Configure the OAuth callback
         */
        System.setProperty(ClientConfig.OAUTH_TOKEN_ENDPOINT_URI, "https://keycloak-myproject.apps.jscholz.rhmw-integrations.net/auth/realms/External/protocol/openid-connect/token");
        System.setProperty(Config.OAUTH_CLIENT_ID, "ext-kafka-consumer");
        System.setProperty(Config.OAUTH_CLIENT_SECRET, "ext-kafka-consumer-secret");
        System.setProperty(Config.OAUTH_USERNAME_CLAIM, "preferred_username");
        System.setProperty(Config.OAUTH_SSL_TRUSTSTORE_LOCATION, "../oauth.truststore");
        System.setProperty(Config.OAUTH_SSL_TRUSTSTORE_PASSWORD, "123456");

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
        props.setProperty(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "../08-custom-certificates/external.truststore");
        props.setProperty(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "123456");
        props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "HTTPS");

        props.put("sasl.jaas.config", "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;");
        props.put("security.protocol", "SASL_SSL");
        props.put("sasl.mechanism", "OAUTHBEARER");
        props.put("sasl.login.callback.handler.class", "io.strimzi.kafka.oauth.client.JaasClientOauthLoginCallbackHandler");

        /*
         * Create the consumer and subscribe to topics
         */
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
        consumer.subscribe(Pattern.compile("my-topic"));

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
