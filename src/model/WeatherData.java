package model;

import com.google.gson.annotations.SerializedName;
import java.sql.*;
import java.util.Objects;

public class WeatherData {
    private static final String DB_URL = "jdbc:sqlite:weather.db";

    private String locatie;
    private String data;

    @SerializedName("stareaVremii")
    private String stareaVremii;

    @SerializedName("temperaturaDeAzi")
    private int temperaturaDeAzi;

    @SerializedName("temperaturaPentruUrmatoarele3Zile")
    private int[] temperaturaPentruUrmatoarele3Zile;

    private int x;
    private int y;

    public WeatherData(String locatie, String data, String stareaVremii, int temperaturaDeAzi, int[] temperaturaPentruUrmatoarele3Zile, int x, int y) {
        this.locatie = locatie;
        this.data = data;
        this.stareaVremii = stareaVremii;
        this.temperaturaDeAzi = temperaturaDeAzi;
        this.temperaturaPentruUrmatoarele3Zile = temperaturaPentruUrmatoarele3Zile;
        this.x = x;
        this.y = y;
    }

    public String getLocatie() {
        return locatie;
    }

    public String getData() {
        return data;
    }

    public String getStareaVremii() {
        return stareaVremii;
    }

    public int getTemperaturaDeAzi() {
        return temperaturaDeAzi;
    }

    public int[] getTemperaturaPentruUrmatoarele3Zile() {
        return temperaturaPentruUrmatoarele3Zile;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeatherData that = (WeatherData) o;
        return Objects.equals(locatie, that.locatie);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locatie);
    }

    static {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             Statement statement = connection.createStatement()) {

            String dropTableSQL = "DROP TABLE IF EXISTS weather_data;";
            statement.executeUpdate(dropTableSQL);

            String createTableSQL = """
            CREATE TABLE IF NOT EXISTS weather_data (
                locatie TEXT PRIMARY KEY,
                data TEXT NOT NULL,
                starea_vremii TEXT NOT NULL,
                temperatura_de_azi INTEGER NOT NULL,
                ziua1 INTEGER NOT NULL,
                ziua2 INTEGER NOT NULL,
                ziua3 INTEGER NOT NULL,
                x INTEGER NOT NULL,
                y INTEGER NOT NULL
            );
        """;
            statement.executeUpdate(createTableSQL);

            System.out.println("[WeatherData] Table created successfully.");
        } catch (SQLException e) {
            System.err.println("[WeatherData] Error initializing database: " + e.getMessage());
        }
    }

    public static boolean addWeatherDataToDatabase(String locatie, String data, String stareaVremii, int temperaturaDeAzi, int[] temperaturi, int x, int y) {
        String insertSQL = """
            INSERT INTO weather_data (locatie, data, starea_vremii, temperatura_de_azi, ziua1, ziua2, ziua3, x, y)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {

            preparedStatement.setString(1, locatie);
            preparedStatement.setString(2, data);
            preparedStatement.setString(3, stareaVremii);
            preparedStatement.setInt(4, temperaturaDeAzi);
            preparedStatement.setInt(5, temperaturi[0]);
            preparedStatement.setInt(6, temperaturi[1]);
            preparedStatement.setInt(7, temperaturi[2]);
            preparedStatement.setInt(8, x);
            preparedStatement.setInt(9, y);

            preparedStatement.executeUpdate();
            System.out.println("[WeatherData] Data added successfully for location: " + locatie);
            return true;
        } catch (SQLException e) {
            System.err.println("[WeatherData] Error adding data: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateWeatherDataInDatabase(String locatie, String data, String stareaVremii, int temperaturaDeAzi, int[] temperaturi, int x, int y) {
        if (getWeatherDataFromDatabase(locatie) == null) {
            System.err.println("[WeatherData] No existing data for location: " + locatie);
            return false;
        }
        String updateSQL = """
        UPDATE weather_data
        SET data = ?, starea_vremii = ?, temperatura_de_azi = ?, ziua1 = ?, ziua2 = ?, ziua3 = ?, x = ?, y = ?
        WHERE locatie = ?
    """;
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = connection.prepareStatement(updateSQL)) {

            preparedStatement.setString(1, data);
            preparedStatement.setString(2, stareaVremii);
            preparedStatement.setInt(3, temperaturaDeAzi);
            preparedStatement.setInt(4, temperaturi[0]);
            preparedStatement.setInt(5, temperaturi[1]);
            preparedStatement.setInt(6, temperaturi[2]);
            preparedStatement.setInt(7, x);
            preparedStatement.setInt(8, y);
            preparedStatement.setString(9, locatie);

            int rowsUpdated = preparedStatement.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.err.println("[WeatherData] Error updating data: " + e.getMessage());
            return false;
        }
    }

    public static WeatherData getWeatherDataFromDatabase(String locatie) {
        String querySQL = "SELECT * FROM weather_data WHERE locatie = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = connection.prepareStatement(querySQL)) {

            preparedStatement.setString(1, locatie);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String data = resultSet.getString("data");
                String stareaVremii = resultSet.getString("starea_vremii");
                int temperaturaDeAzi = resultSet.getInt("temperatura_de_azi");
                int[] temperaturi = {
                        resultSet.getInt("ziua1"),
                        resultSet.getInt("ziua2"),
                        resultSet.getInt("ziua3")
                };
                int x = resultSet.getInt("x");
                int y = resultSet.getInt("y");

                return new WeatherData(locatie, data, stareaVremii, temperaturaDeAzi, temperaturi, x, y);
            }
        } catch (SQLException e) {
            System.err.println("[WeatherData] Error retrieving data: " + e.getMessage());
        }
        return null;
    }

    public static void listAllWeatherData() {
        String querySQL = "SELECT * FROM weather_data";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = connection.prepareStatement(querySQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String locatie = resultSet.getString("locatie");
                String data = resultSet.getString("data");
                String stareaVremii = resultSet.getString("starea_vremii");
                int temperaturaDeAzi = resultSet.getInt("temperatura_de_azi");
                int[] temperaturi = {
                        resultSet.getInt("ziua1"),
                        resultSet.getInt("ziua2"),
                        resultSet.getInt("ziua3")
                };
                int x = resultSet.getInt("x");
                int y = resultSet.getInt("y");

                WeatherData weatherData = new WeatherData(locatie, data, stareaVremii, temperaturaDeAzi, temperaturi, x, y);
                System.out.println(weatherData);
            }
        } catch (SQLException e) {
            System.err.println("[WeatherData] Error listing data: " + e.getMessage());
        }
    }

    public static String normalizeLocation(String location) {
        if (location == null || location.isEmpty()) {
            return null;
        }
        return location.trim().toLowerCase()
                .replace("ă", "a")
                .replace("â", "a")
                .replace("î", "i")
                .replace("ș", "s")
                .replace("ț", "t");
    }

    public static WeatherData getNearestWeatherDataByEuclideanDistance(int x, int y) {
        String querySQL = """
        SELECT *, 
               ((x - ?) * (x - ?) + (y - ?) * (y - ?)) AS distance
        FROM weather_data
        ORDER BY distance
        LIMIT 1
    """;
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = connection.prepareStatement(querySQL)) {

            preparedStatement.setInt(1, x);
            preparedStatement.setInt(2, x);
            preparedStatement.setInt(3, y);
            preparedStatement.setInt(4, y);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String locatie = resultSet.getString("locatie");
                String data = resultSet.getString("data");
                String stareaVremii = resultSet.getString("starea_vremii");
                int temperaturaDeAzi = resultSet.getInt("temperatura_de_azi");
                int[] temperaturi = {
                        resultSet.getInt("ziua1"),
                        resultSet.getInt("ziua2"),
                        resultSet.getInt("ziua3")
                };

                return new WeatherData(locatie, data, stareaVremii, temperaturaDeAzi, temperaturi,
                        resultSet.getInt("x"), resultSet.getInt("y"));
            }
        } catch (SQLException e) {
            System.err.println("[WeatherData] Error retrieving nearest data by Euclidean distance: " + e.getMessage());
        }
        return null;
    }

    public static WeatherData getWeatherDataByCoordinates(int x, int y) {
        String querySQL = "SELECT * FROM weather_data WHERE x = ? AND y = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = connection.prepareStatement(querySQL)) {

            preparedStatement.setInt(1, x);
            preparedStatement.setInt(2, y);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String locatie = resultSet.getString("locatie");
                String data = resultSet.getString("data");
                String stareaVremii = resultSet.getString("starea_vremii");
                int temperaturaDeAzi = resultSet.getInt("temperatura_de_azi");
                int[] temperaturi = {
                        resultSet.getInt("ziua1"),
                        resultSet.getInt("ziua2"),
                        resultSet.getInt("ziua3")
                };

                return new WeatherData(locatie, data, stareaVremii, temperaturaDeAzi, temperaturi,
                        resultSet.getInt("x"), resultSet.getInt("y"));
            } else {
                // Dacă nu găsește coordonatele exacte, caută locația cea mai apropiată
                return getNearestWeatherDataByEuclideanDistance(x, y);
            }
        } catch (SQLException e) {
            System.err.println("[WeatherData] Error retrieving data by coordinates: " + e.getMessage());
        }
        return null;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("WeatherData{");
        sb.append("locatie='").append(locatie).append('\'');
        sb.append(", data='").append(data).append('\'');
        sb.append(", stareaVremii='").append(stareaVremii).append('\'');
        sb.append(", temperaturaDeAzi=").append(temperaturaDeAzi);
        sb.append(", temperaturaPentruUrmatoarele3Zile=[");
        if (temperaturaPentruUrmatoarele3Zile != null) {
            for (int i = 0; i < temperaturaPentruUrmatoarele3Zile.length; i++) {
                sb.append(temperaturaPentruUrmatoarele3Zile[i]);
                if (i < temperaturaPentruUrmatoarele3Zile.length - 1) {
                    sb.append(", ");
                }
            }
        }
        sb.append("], x=").append(x);
        sb.append(", y=").append(y);
        sb.append("}");
        return sb.toString();
    }
}