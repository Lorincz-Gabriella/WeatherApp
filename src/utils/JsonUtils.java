package utils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import model.WeatherData;

import java.io.FileReader;
import java.io.IOException;

public class JsonUtils {

    public static void importWeatherData(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            Gson gson = new Gson();

            WeatherData[] weatherDataArray = gson.fromJson(reader, WeatherData[].class);

            if (weatherDataArray == null) {
                System.err.println("[JsonUtils] Error: JSON file is empty or invalid format.");
                return;
            }

            System.out.println("[JsonUtils] Imported weather data from file:");
            for (WeatherData data : weatherDataArray) {
                if (data != null) {

                    if (data.getLocatie() == null || data.getData() == null || data.getStareaVremii() == null || data.getX() == 0 || data.getY() == 0) {
                        System.err.println("[JsonUtils] Invalid weather data found, skipping entry: " + data);
                        continue;
                    }

                    if (data.getTemperaturaPentruUrmatoarele3Zile() == null ||
                            data.getTemperaturaPentruUrmatoarele3Zile().length != 3) {
                        System.err.println("[JsonUtils] Invalid temperature data for location: " + data.getLocatie());
                        continue;
                    }

                    String normalizedLocation = WeatherData.normalizeLocation(data.getLocatie());
                    if (normalizedLocation == null) {
                        System.err.println("[JsonUtils] Skipping invalid location.");
                        continue;
                    }
                    boolean success = WeatherData.updateWeatherDataInDatabase(
                            normalizedLocation,
                            data.getData(),
                            data.getStareaVremii(),
                            data.getTemperaturaDeAzi(),
                            data.getTemperaturaPentruUrmatoarele3Zile(),
                            data.getX(),
                            data.getY()
                    ) || WeatherData.addWeatherDataToDatabase(
                            normalizedLocation,
                            data.getData(),
                            data.getStareaVremii(),
                            data.getTemperaturaDeAzi(),
                            data.getTemperaturaPentruUrmatoarele3Zile(),
                            data.getX(),
                            data.getY()
                    );

                    if (success) {
                        System.out.println("[JsonUtils] Data added/updated for location: " + normalizedLocation);
                    } else {
                        System.err.println("[JsonUtils] Failed to add/update data for location: " + normalizedLocation);
                    }
                } else {
                    System.err.println("[JsonUtils] Null data found in JSON.");
                }
            }
            System.out.println("[JsonUtils] Weather data import complete.");
        } catch (JsonSyntaxException e) {
            System.err.println("[JsonUtils] Error: Invalid JSON format. " + e.getMessage());
        } catch (IOException e) {
            System.err.println("[JsonUtils] Error reading JSON file: " + e.getMessage());
        }
    }
}
