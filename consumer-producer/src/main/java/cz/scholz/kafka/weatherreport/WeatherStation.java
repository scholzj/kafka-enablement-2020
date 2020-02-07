package cz.scholz.kafka.weatherreport;

public class WeatherStation {
    String city;
    String country;
    int minTemp;
    int maxTemp;

    public WeatherStation(String city, String country, int minTemp, int maxTemp) {
        this.city = city;
        this.country = country;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
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

    public int getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(int minTemp) {
        this.minTemp = minTemp;
    }

    public int getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(int maxTemp) {
        this.maxTemp = maxTemp;
    }

    @Override
    public String toString() {
        return "WeatherStation{" +
                "city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", minTemp=" + minTemp +
                ", maxTemp=" + maxTemp +
                '}';
    }
}
