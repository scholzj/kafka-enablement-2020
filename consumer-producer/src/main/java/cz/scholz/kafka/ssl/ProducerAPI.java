package cz.scholz.kafka.ssl;

import cz.scholz.kafka.weatherreport.WeatherReport;
import cz.scholz.kafka.weatherreport.WeatherStations;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.SslConfigs;
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
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:19092");

        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        props.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        props.setProperty(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "../kafka-architecture/ssl/keys/truststore");
        props.setProperty(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "123456");
        props.setProperty(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "../kafka-architecture/ssl/keys/user1.keystore");
        props.setProperty(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "123456");
        props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "HTTPS");

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
