package model;

import java.sql.*;
import java.util.Objects;
import java.util.Optional;


public class User {
    private static final String DB_URL = "jdbc:sqlite:users.db";

    private String username;
    private String password;
    private boolean isAdmin;
    private int x;
    private int y;

    public User(String username, String password, boolean isAdmin, int x, int y) {
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin;
        this.x = x;
        this.y = y;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", isAdmin=" + isAdmin +
                ", x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return username.equals(user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    static {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             Statement statement = connection.createStatement()) {
            String createTableSQL = """
                CREATE TABLE IF NOT EXISTS users (
                    username TEXT PRIMARY KEY,
                    password TEXT NOT NULL,
                    is_admin INTEGER NOT NULL,
                    x INTEGER NOT NULL,
                    y INTEGER NOT NULL
                );
            """;
            statement.executeUpdate(createTableSQL);
        } catch (SQLException e) {
            System.err.println("[User] Error initializing database: " + e.getMessage());
        }
    }

    public static boolean register(String username, String password, boolean isAdmin, int x, int y) {
        String insertSQL = "INSERT INTO users (username, password, is_admin, x, y) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            preparedStatement.setBoolean(3, isAdmin);
            preparedStatement.setInt(4, x);
            preparedStatement.setInt(5, y);

            preparedStatement.executeUpdate();
            System.out.println("[User] Registration successful for: " + username);
            return true;
        } catch (SQLException e) {
            System.err.println("[User] Error registering user: " + e.getMessage());
            return false;
        }
    }

    public static Optional<User> login(String username, String password) {
        System.out.println("[DEBUG] Login attempt: username=" + username + ", password=" + password);

        String querySQL = "SELECT username, password, is_admin, x, y FROM users WHERE username = ? AND password = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = connection.prepareStatement(querySQL)) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                boolean isAdmin = resultSet.getInt("is_admin") == 1;
                int x = resultSet.getInt("x");
                int y = resultSet.getInt("y");

                return Optional.of(new User(username, password, isAdmin, x, y));
            } else {
                System.out.println("[User] Invalid credentials for: " + username);
                return Optional.empty();
            }
        } catch (SQLException e) {
            System.err.println("[User] Error logging in: " + e.getMessage());
        }
        return Optional.empty();
    }


    public static Optional<User> getUserByUsername(String username) {
        String querySQL = "SELECT username, password, is_admin, x, y FROM users WHERE username = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = connection.prepareStatement(querySQL)) {

            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String password = resultSet.getString("password");
                boolean isAdmin = resultSet.getInt("is_admin") == 1;
                int x = resultSet.getInt("x");
                int y = resultSet.getInt("y");

                return Optional.of(new User(username, password, isAdmin, x, y));
            }
        } catch (SQLException e) {
            System.err.println("[User] Error retrieving user by username: " + e.getMessage());
        }
        return Optional.empty();
    }


    public static User updateCoordinates(String username, int newX, int newY) {
        String updateSQL = "UPDATE users SET x = ?, y = ? WHERE username = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = connection.prepareStatement(updateSQL)) {

            preparedStatement.setInt(1, newX);
            preparedStatement.setInt(2, newY);
            preparedStatement.setString(3, username);

            int rowsUpdated = preparedStatement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("[User] Coordinates updated successfully for: " + username);

                // Utilizează orElse pentru a obține valoarea din Optional
                return getUserByUsername(username).orElse(null);
            } else {
                System.err.println("[User] No user found with username: " + username);
                return null;
            }
        } catch (SQLException e) {
            System.err.println("[User] Error updating coordinates: " + e.getMessage());
            return null;
        }
    }

}
