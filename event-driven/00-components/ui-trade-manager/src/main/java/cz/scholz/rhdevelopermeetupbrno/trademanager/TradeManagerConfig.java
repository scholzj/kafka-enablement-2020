package cz.scholz.rhdevelopermeetupbrno.trademanager;

public class TradeManagerConfig {
    private final String bootstrapServers;
    private final String topic;
    private final String groupId;
    private String acks = "1";
    private final String trustStorePassword;
    private final String trustStorePath;
    private final String keyStorePassword;
    private final String keyStorePath;
    private final String username;
    private final String password;

    public TradeManagerConfig(String bootstrapServers, String topic, String groupId, String trustStorePassword, String trustStorePath, String keyStorePassword, String keyStorePath, String username, String password) {
        this.bootstrapServers = bootstrapServers;
        this.topic = topic;
        this.groupId = groupId;
        this.trustStorePassword = trustStorePassword;
        this.trustStorePath = trustStorePath;
        this.keyStorePassword = keyStorePassword;
        this.keyStorePath = keyStorePath;
        this.username = username;
        this.password = password;
    }

    public static TradeManagerConfig fromEnv() {
        String bootstrapServers = System.getenv("BOOTSTRAP_SERVERS");
        String topic = System.getenv("TOPIC");
        String groupId = System.getenv("GROUP_ID");
        String trustStorePassword = System.getenv("TRUSTSTORE_PASSWORD") == null ? null : System.getenv("TRUSTSTORE_PASSWORD");
        String trustStorePath = System.getenv("TRUSTSTORE_PATH") == null ? null : System.getenv("TRUSTSTORE_PATH");
        String keyStorePassword = System.getenv("KEYSTORE_PASSWORD") == null ? null : System.getenv("KEYSTORE_PASSWORD");
        String keyStorePath = System.getenv("KEYSTORE_PATH") == null ? null : System.getenv("KEYSTORE_PATH");
        String username = System.getenv("USERNAME") == null ? null : System.getenv("USERNAME");
        String password = System.getenv("PASSWORD") == null ? null : System.getenv("PASSWORD");

        return new TradeManagerConfig(bootstrapServers, topic, groupId, trustStorePassword, trustStorePath, keyStorePassword, keyStorePath, username, password);
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public String getTopic() {
        return topic;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getAcks() {
        return acks;
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
}
