package cz.scholz.kafka.serialization;

import cz.scholz.kafka.weatherreport.WeatherReport;
import cz.scholz.kafka.weatherreport.WeatherReportDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Properties;
import java.util.regex.Pattern;

public class ConsumerAPI {
    private static Logger LOG = LoggerFactory.getLogger(cz.scholz.kafka.manualcommits.ConsumerAPI.class);

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
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "my-group");
        props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        /*
         * Create the consumer and subscribe to topics
         *
         * NOTICE: The Deserializer and the types used for the consumer
         */
        KafkaConsumer<String, WeatherReport> consumer = new KafkaConsumer<String, WeatherReport>(props, new StringDeserializer(), new WeatherReportDeserializer());
        consumer.subscribe(Pattern.compile("weather-report"));

        /*
         * Consume 100 messages or stop when poll returns null
         */
        int i = 0;
        while (i < 100)    {
            ConsumerRecords<String, WeatherReport> records = consumer.poll(Duration.ofSeconds(5));

            if (records.isEmpty()) {
                LOG.warn("No messages received");
                break;
            }

            records.forEach(record -> {
                LOG.info("Received weather report for {} with temperature {} taken at {}", record.key(), record.value().getTemperature(), record.value().getTime());
            });
        }

        /*
         * Close the consumer
         */
        consumer.close();
    }
}
