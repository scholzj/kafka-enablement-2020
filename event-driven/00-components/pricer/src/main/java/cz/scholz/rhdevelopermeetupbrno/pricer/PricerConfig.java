package cz.scholz.rhdevelopermeetupbrno.pricer;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class PricerConfig {
    private static final Logger log = LoggerFactory.getLogger(PricerConfig.class.getName());

    private final String bootstrapServers;
    private final String leftSourceTopic;
    private final String rightSourceTopic;
    private final String targetTopic;
    private final String trustStorePassword;
    private final String trustStorePath;
    private final String keyStorePassword;
    private final String keyStorePath;
    private final String username;
    private final String password;

    public PricerConfig(String bootstrapServers, String leftSourceTopic, String rightSourceTopic, String targetTopic, String trustStorePassword, String trustStorePath, String keyStorePassword, String keyStorePath, String username, String password) {
        this.bootstrapServers = bootstrapServers;
        this.leftSourceTopic = leftSourceTopic;
        this.rightSourceTopic = rightSourceTopic;
        this.targetTopic = targetTopic;
        this.trustStorePassword = trustStorePassword;
        this.trustStorePath = trustStorePath;
        this.keyStorePassword = keyStorePassword;
        this.keyStorePath = keyStorePath;
        this.username = username;
        this.password = password;
    }

    public static PricerConfig fromEnv() {
        String bootstrapServers = System.getenv("BOOTSTRAP_SERVERS");
        String leftSourceTopic = System.getenv("LEFT_SOURCE_TOPIC");
        String rightSourceTopic = System.getenv("RIGHT_SOURCE_TOPIC");
        String targetTopic = System.getenv("TARGET_TOPIC");
        String trustStorePassword = System.getenv("TRUSTSTORE_PASSWORD") == null ? null : System.getenv("TRUSTSTORE_PASSWORD");
        String trustStorePath = System.getenv("TRUSTSTORE_PATH") == null ? null : System.getenv("TRUSTSTORE_PATH");
        String keyStorePassword = System.getenv("KEYSTORE_PASSWORD") == null ? null : System.getenv("KEYSTORE_PASSWORD");
        String keyStorePath = System.getenv("KEYSTORE_PATH") == null ? null : System.getenv("KEYSTORE_PATH");
        String username = System.getenv("USERNAME") == null ? null : System.getenv("USERNAME");
        String password = System.getenv("PASSWORD") == null ? null : System.getenv("PASSWORD");

        return new PricerConfig(bootstrapServers, leftSourceTopic, rightSourceTopic, targetTopic, trustStorePassword, trustStorePath, keyStorePassword, keyStorePath, username, password);
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public String getTradesTopic() {
        return leftSourceTopic;
    }

    public String getPricesTopic() {
        return rightSourceTopic;
    }

    public String getPortfolioTopic() {
        return targetTopic;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public String getTrustStorePath() {
        return trustStorePath;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public String getKeyStorePath() {
        return keyStorePath;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public static Properties getKafkaStreamsProperties(PricerConfig config) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "pricer");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 5000);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Float().getClass());

        if (config.getTrustStorePassword() != null && config.getTrustStorePath() != null)   {
            log.info("Configuring truststore");
            props.put("security.protocol", "SSL");
            props.put("ssl.truststore.type", "PKCS12");
            props.put("ssl.truststore.password", config.getTrustStorePassword());
            props.put("ssl.truststore.location", config.getTrustStorePath());
        }

        if (config.getKeyStorePassword() != null && config.getKeyStorePath() != null)   {
            log.info("Configuring keystore");
            props.put("security.protocol", "SSL");
            props.put("ssl.keystore.type", "PKCS12");
            props.put("ssl.keystore.password", config.getKeyStorePassword());
            props.put("ssl.keystore.location", config.getKeyStorePath());
        }

        if (config.getUsername() != null && config.getPassword() != null)   {
            props.put("sasl.mechanism","SCRAM-SHA-512");
            props.put("sasl.jaas.config", "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"" + config.getUsername() + "\" password=\"" + config.getPassword() + "\";");

            if (props.get("security.protocol") != null && props.get("security.protocol").equals("SSL"))  {
                props.put("security.protocol","SASL_SSL");
            } else {
                props.put("security.protocol","SASL_PLAINTEXT");
            }
        }

        return props;
    }
}
