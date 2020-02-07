package cz.scholz.kafka.weatherreport;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class WeatherStations {
    private static Random rand = new Random();

    static final List<WeatherStation> stations = new ArrayList<WeatherStation>() {{
        add(new WeatherStation("Prague", "CZ", -10, 10));
        add(new WeatherStation("Olomouc", "CZ", -10, 10));
        add(new WeatherStation("Birmingham", "UK", 0, 15));
        add(new WeatherStation("Sao Paulo", "BR", 15, 30));
    }};

    public static WeatherReport nextWeatherReport()    {
        int index = rand.nextInt(stations.size());
        WeatherStation station = stations.get(index);
        int temprature = station.getMinTemp() + rand.nextInt(station.getMaxTemp() - station.getMinTemp());

        return new WeatherReport(station.getCity(), station.getCountry(), temprature, Calendar.getInstance().getTime());
    }
}
