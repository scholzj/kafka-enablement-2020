package cz.scholz.kafka.weatherreport;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class WeatherReportSerializer implements Serializer {
    @Override
    public void configure(Map map, boolean b) {
        // Nothing to configure
    }

    @Override
    public byte[] serialize(String s, Object o) {
        byte[] retVal = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            retVal = objectMapper.writeValueAsBytes(o);
        } catch (Exception e) {
            System.out.println("Ups: " + e);
        }
        return retVal;
    }

    @Override
    public void close() {
        // Nothing to do
    }
}
