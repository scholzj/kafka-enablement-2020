package cz.scholz.kafka.basic;

import cz.scholz.kafka.weatherreport.WeatherReport;
import cz.scholz.kafka.weatherreport.WeatherStations;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerAPI {
    private static Logger LOG = LoggerFactory.getLogger(ProducerAPI.class);

    public static void main(String[] args) throws InterruptedException {
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
            producer.send(msg);
            i++;
            Thread.sleep(1000);
        }

        /*
         * Close producer
         */
        producer.close();
    }
}
