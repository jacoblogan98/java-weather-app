package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WeatherAppGui extends JFrame {
    private JsonObject weatherData;
    private final Gson parser = new Gson();

    public WeatherAppGui() {
        super("Weather App");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450,650);
        setLocationRelativeTo(null);
        setLayout(null);
        setResizable(false);

        addGuiComponents();
    }

    private void addGuiComponents() {
        JTextField searchTextField = new JTextField();
        searchTextField.setBounds(15,15,351,45);
        searchTextField.setFont(new Font("Dialog", Font.PLAIN, 24));
        add(searchTextField);

        JLabel weatherConditionImage = new JLabel(loadImage("src/assets/cloudy.png"));
        weatherConditionImage.setBounds(0,125,450,217);
        add(weatherConditionImage);

        JLabel temperatureText = new JLabel("10 F");
        temperatureText.setBounds(0,350,450,54);
        temperatureText.setFont(new Font("Dialog", Font.BOLD, 48));
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperatureText);

        JLabel weatherConditionDesc = new JLabel("Cloudy");
        weatherConditionDesc.setBounds(0,405,450,36);
        weatherConditionDesc.setFont(new Font("Dialog", Font.PLAIN, 32));
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherConditionDesc);

        JLabel humidityImage = new JLabel(loadImage("src/assets/humidity.png"));
        humidityImage.setBounds(15,500,74,66);
        add(humidityImage);

        JLabel humidityText = new JLabel("<html><b>Humidity</b></html>");
        humidityText.setBounds(90,500,85,55);
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(humidityText);

        JLabel windSpeedImage= new JLabel(loadImage("src/assets/windspeed.png"));
        windSpeedImage.setBounds(220,500,74,66);
        add(windSpeedImage);

        JLabel windSpeedText = new JLabel("<html><b>Windspeed</b> 15mph</html>");
        windSpeedText.setBounds(310,500,85,55);
        windSpeedText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(windSpeedText);

        JButton searchButton = new JButton(loadImage("src/assets/search.png"));
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(375,13,47,45);
        searchButton.addActionListener(e -> {
            String userInput = searchTextField.getText();

            if (userInput.replaceAll("\\s", "").isEmpty()) {
                return;
            }

            this.weatherData = WeatherApp.getWeatherData(userInput);
            String weatherCondition = parser.fromJson(this.weatherData.get("weather_condition"), String.class);

            switch (weatherCondition) {
                case "Clear":
                    weatherConditionImage.setIcon(loadImage("src/assets/clear.png"));
                    break;
                case "Cloudy":
                    weatherConditionImage.setIcon(loadImage("src/assets/cloudy.png"));
                    break;
                case "Rain":
                    weatherConditionImage.setIcon(loadImage("src/assets/rain.png"));
                    break;
                case "Snow":
                    weatherConditionImage.setIcon(loadImage("src/assets/snow.png"));
                    break;
            }

            double temperature = parser.fromJson(this.weatherData.get("temperature"), Double.class);
            temperatureText.setText(temperature + " F");

            weatherConditionDesc.setText(weatherCondition);

            long humidity = parser.fromJson(this.weatherData.get("humidity"), Long.class);
            humidityText.setText("<html><b>Humidity</b>" + humidity + "%</html>");

            double windSpeed = parser.fromJson(this.weatherData.get("wind_speed"), Double.class);
            System.out.println(windSpeed);
            windSpeedText.setText("<html><b>Wind Speed</b>" + windSpeed + "mph</html>");

        });
        add(searchButton);

    }

    private ImageIcon loadImage(String resourcePath) {
        try {
            BufferedImage image = ImageIO.read(new File(resourcePath));
            return new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Could not find resource");
        return null;
    }
}
