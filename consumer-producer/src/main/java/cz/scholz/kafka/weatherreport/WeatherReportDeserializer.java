package cz.scholz.kafka.weatherreport;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class WeatherReportDeserializer implements Deserializer {
    @Override
    public void configure(Map map, boolean b) {
        // Nothing to do
    }

    @Override
    public Object deserialize(String s, byte[] bytes) {
        ObjectMapper mapper = new ObjectMapper();
        WeatherReport obj = null;

        try {
            obj = mapper.readValue(bytes, WeatherReport.class);
        } catch (Exception e) {
            System.out.println("Ups: " + e);
        }

        return obj;
    }

    @Override
    public void close() {
        // Nothing to close
    }
}
