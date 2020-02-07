/*
 * Copyright 2018, Jakub Scholz
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package cz.scholz.rhdevelopermeetupbrno.pricefeedconnector;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PriceFeedConnector extends SourceConnector {
    private static final Logger log = LoggerFactory.getLogger(PriceFeedConnector.class);

    public static final String NAME_CONFIG = "name";
    public static final String TOPIC_CONFIG = "topic";
    public static final String FREQUENCY_CONFIG = "frequency";

    private static final ConfigDef CONFIG_DEF = new ConfigDef()
        .define(TOPIC_CONFIG, Type.STRING, Importance.HIGH, "The topic to publish data to")
        .define(FREQUENCY_CONFIG, Type.INT, 1, Importance.HIGH, "The frequency with which the prices should change");

    private String connectorName;
    private String topic;
    private int frequency;

    public static List<ShareMasterData> masterData = new ArrayList<>(10);

    static {
        masterData.add(new ShareMasterData("RHT", 183.25f, 2));
        masterData.add(new ShareMasterData("AMZN", 1837.28f, 2));
        masterData.add(new ShareMasterData("VMW", 185.79f, 2));
        masterData.add(new ShareMasterData("TWTR", 34.72f, 2));
        masterData.add(new ShareMasterData("GOOG", 1207.15f, 2));
        masterData.add(new ShareMasterData("IBM", 143.28f, 2));
        masterData.add(new ShareMasterData("TSLA", 274.96f, 2));
        masterData.add(new ShareMasterData("MSFT", 119.89f, 2));
        masterData.add(new ShareMasterData("ORCL", 53.93f, 2));
        masterData.add(new ShareMasterData("FB", 175.72f, 2));
    }

    @Override
    public String version() {
        return "1.0.0-SNAPSHOT";
    }

    @Override
    public void start(Map<String, String> props) {
        log.info("Connector config keys: {}", String.join(", ", props.keySet()));

        connectorName = props.get(NAME_CONFIG);
        log.info("Starting connector {}", connectorName);

        AbstractConfig parsedConfig = new AbstractConfig(CONFIG_DEF, props);
        topic = parsedConfig.getString(TOPIC_CONFIG);
        frequency = parsedConfig.getInt(FREQUENCY_CONFIG);
    }

    @Override
    public Class<? extends Task> taskClass() {
        return PriceFeedTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        ArrayList<Map<String, String>> configs = new ArrayList<>();

        Map<String, String> config = new HashMap<>(1);
        config.put(TOPIC_CONFIG, topic);
        config.put(FREQUENCY_CONFIG, String.valueOf(frequency));

        configs.add(config);

        return configs;
    }

    @Override
    public void stop() {
        // Nothing to do
    }

    @Override
    public ConfigDef config() {
        return CONFIG_DEF;
    }
}
