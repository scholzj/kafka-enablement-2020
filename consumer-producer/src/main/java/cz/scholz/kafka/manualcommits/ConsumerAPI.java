package cz.scholz.kafka.manualcommits;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Properties;
import java.util.regex.Pattern;

public class ConsumerAPI {
    private static Logger LOG = LoggerFactory.getLogger(ConsumerAPI.class);

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
        props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        /*
         * Create the consumer and subscribe to topics
         */
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
        consumer.subscribe(Pattern.compile("weather-report"));

        /*
         * Consume 100 messages or stop when poll returns null
         */
        int i = 0;
        while (i < 100)    {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));

            if (records.isEmpty()) {
                LOG.warn("No messages received");
                break;
            }

            records.forEach(record -> {
                LOG.info("Received weather report for {}:   {}", record.key(), record.value());
            });

            // Sync
            //consumer.commitSync();

            // Async
            consumer.commitAsync((map, e) -> LOG.info("Committed latest offsets: {}", map));
        }

        /*
         * Close the consumer
         */
        consumer.close();
    }
}
