package cz.scholz.kafka.weatherreport;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

public class WeatherReport {
    String city;
    String country;
    int temperature;
    Date time;

    public WeatherReport() {
    }

    public WeatherReport(String city, String country, int temperature, Date time) {
        this.city = city;
        this.country = country;
        this.temperature = temperature;
        this.time = time;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @JsonIgnore
    public String getCityCountry() {
        return city + "/" + country;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "WeatherReport{" +
                "city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", temperature=" + temperature +
                ", time=" + time +
                '}';
    }
}
