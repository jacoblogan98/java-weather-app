package org.example;

import com.google.gson.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class WeatherApp {
    private static final Gson parser = new Gson();

    public static JsonObject getWeatherData(String locationName) {
        JsonArray locationData = getLocationData(locationName);
        System.out.println(locationData);
        assert locationData != null;
        JsonObject location = (JsonObject) locationData.get(0);
        double latitude = parser.fromJson(location.get("latitude"), Double.class);
        double longitude = parser.fromJson(location.get("longitude"), Double.class);

        String urlString = "https://api.open-meteo.com/v1/forecast?" +
            "latitude=" + latitude + "&longitude=" + longitude +
            "&hourly=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&temperature_unit=fahrenheit&wind_speed_unit=mph&timezone=America%2FNew_York";

        try {
            HttpURLConnection conn = fetchApiResponse(urlString);

            if (conn.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to the API");
                return null;
            }

            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while (scanner.hasNext()) {
                resultJson.append(scanner.nextLine());
            }

            scanner.close();
            conn.disconnect();

            JsonObject resultJsonObj = parser.fromJson(String.valueOf(resultJson), JsonObject.class);

            JsonObject hourly = (JsonObject) resultJsonObj.get("hourly");
            JsonArray time = (JsonArray) hourly.get("time");
            int index = findIndexOfCurrentTime(time);

            JsonArray temperatureData = (JsonArray) hourly.get("temperature_2m");
            JsonElement temperature = temperatureData.get(index);

            JsonArray weatherCode = (JsonArray) hourly.get("weather_code");
            JsonElement weatherCondition = convertWeatherCode(parser.fromJson(weatherCode.get(index), Long.class));

            JsonArray relativeHumidity = (JsonArray) hourly.get("relative_humidity_2m");
            JsonElement humidity = relativeHumidity.get(index);

            JsonArray windSpeedData = (JsonArray) hourly.get("wind_speed_10m");
            JsonElement windSpeed = windSpeedData.get(index);

            JsonObject weatherData = new JsonObject();
            weatherData.add("temperature", temperature);
            weatherData.add("weather_condition", weatherCondition);
            weatherData.add("humidity", humidity);
            weatherData.add("wind_speed", windSpeed);

            return weatherData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static JsonArray getLocationData(String locationName) {
        locationName = locationName.replaceAll(" ", "+");

        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                locationName + "&count=10&language=en&format=json";

        try {
            HttpURLConnection conn = fetchApiResponse(urlString);
            assert conn != null;
            if (conn.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to API");
                return null;
            } else {
                StringBuilder resultJson = new StringBuilder();
                Scanner scanner = new Scanner(conn.getInputStream());

                while (scanner.hasNext()) {
                    resultJson.append(scanner.nextLine());
                }

                scanner.close();
                conn.disconnect();

                JsonObject resultsJsonObj = parser.fromJson(String.valueOf(resultJson), JsonObject.class);
                return (JsonArray) resultsJsonObj.get("results");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static HttpURLConnection fetchApiResponse(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            return conn;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static int findIndexOfCurrentTime(JsonArray timeList) {
        String currentTime = getCurrentTime();

        for (int i = 0; i < timeList.size(); i++) {
            String time = parser.fromJson(timeList.get(i), String.class);

            if (time.equalsIgnoreCase(currentTime)) {
                return i;
            }
        }

        return 0;
    }

    private static String getCurrentTime() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:00");

        return currentDateTime.format(formatter);
    }

    private static JsonElement convertWeatherCode(long weatherCode) {
        String weatherCondition = "";

        if (weatherCode == 0L) {
            weatherCondition = "Clear";
        } else if ((weatherCode >= 51L && weatherCode <= 67L) ||
                (weatherCode >= 80L && weatherCode <= 99L)) {
            weatherCondition = "Cloudy";
        } else if (weatherCode >- 71L && weatherCode <= 77L) {
            weatherCondition = "Snow";
        }

        return parser.toJsonTree(weatherCondition);
    }

}
