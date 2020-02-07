package cz.scholz.kafka.acks;

import cz.scholz.kafka.weatherreport.WeatherReport;
import cz.scholz.kafka.weatherreport.WeatherStations;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class ProducerAPI {
    private static Logger LOG = LoggerFactory.getLogger(ProducerAPI.class);

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        /*
         * Configure the logger
         */
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");

        /*
         * Configure the producer
         */
        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        // Options: 0 => no wait, 1 => only wait for leader, all => all in-sync replicas
        props.setProperty(ProducerConfig.ACKS_CONFIG, "all");

        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        /*
         * Create the producer
         */
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);

        /*
         * Send 100 messages
         */
        int i = 0;
        while (i < 100)    {
            WeatherReport report = WeatherStations.nextWeatherReport();
            LOG.info("Sending new report for {}/{} with temperature {}", report.getCity(), report.getCountry(), report.getTemperature());

            ProducerRecord<String, String> msg = new ProducerRecord<String, String>("weather-report", report.getCityCountry(), report.toString());

            // Async
            producer.send(msg, (recordMetadata, e) -> LOG.info("Message has been stored to partition {} and offset {}", recordMetadata.partition(), recordMetadata.offset()));

            // Sync
            //RecordMetadata meta = producer.send(msg).get();
            //LOG.info("Message has been stored to partition {} and offset {}", meta.partition(), meta.offset());

            i++;
            Thread.sleep(1000);
        }

        /*
         * Close producer
         */
        producer.close();
    }
}
