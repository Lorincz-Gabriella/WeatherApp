package server;

import model.User;
import model.WeatherData;
import utils.JsonUtils;
import java.util.Optional;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class WeatherServer {
    private static final int PORT = 12345;
    private static final String FILE_PATH = "C:\\Users\\Asus\\Desktop\\WeatherInfoApp\\out\\production\\WeatherInfoApp\\weather_data.json";

    public static void main(String[] args) {
        System.out.println("Weather Server is running...");

        try {
            JsonUtils.importWeatherData(FILE_PATH);
            System.out.println("[SERVER] Initial weather data loaded from: " + FILE_PATH);
        } catch (Exception e) {
            System.err.println("[SERVER] Failed to load initial data: " + e.getMessage());
        }

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVER] New client connected.");
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                User user = null;

                while (user == null) {
                    out.println("Welcome! Please choose: Register or Login:");

                    String choice = in.readLine();
                    if (choice == null || choice.trim().isEmpty()) {
                        out.println("Invalid input. Please type 'Register' or 'Login'.");
                        continue;
                    }

                    choice = choice.trim().toLowerCase();
                    switch (choice) {
                        case "register" -> {
                            user = handleRegister(in, out);
                            if (user == null) {
                                out.println("Registration failed. Please try again.");
                            }
                        }
                        case "login" -> {
                            user = handleLogin(in, out);
                            if (user == null) {
                                out.println("Login failed. Please try again.");
                            }
                        }
                        default -> out.println("Invalid choice. Please type 'Register' or 'Login'.");
                    }
                }

                out.println("Welcome, " + user.getUsername() + " with coordinates (" + user.getX() + ", " + user.getY() + ")!");
                System.out.println("[SERVER] User " + user.getUsername() + " logged in.");

                boolean isRunning = true;
                while (isRunning) {
                    out.println("Enter command (get_weather, set_location, import_data, exit):");
                    String command = in.readLine();

                    if (command == null || command.isEmpty()) {
                        out.println("Invalid command.");
                        continue;
                    }

                    command = command.trim().toLowerCase();
                    switch (command) {
                        case "get_weather" -> handleGetWeather(user, out);
                        case "set_location" -> handleSetLocation(user,in, out);
                        case "import_data" -> handleImportData(user, out);
                        case "exit" -> {
                            isRunning = false;
                            out.println("Goodbye!");
                        }
                        default -> out.println("Invalid command.");
                    }
                }

                System.out.println("[SERVER] Closing connection for user: " + user.getUsername());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private User handleRegister(BufferedReader in, PrintWriter out) throws IOException {
            out.println("Enter a username:");
            String username = in.readLine().trim();

            out.println("Enter a password:");
            String password = in.readLine().trim();

            out.println("Enter your X coordinate (integer between 0 and 100):");
            int x;
            while (true) {
                try {
                    x = Integer.parseInt(in.readLine().trim());
                    if (x >= 0 && x <= 100) break;
                    out.println("Invalid input. Please enter an integer between 0 and 100 for X coordinate:");
                } catch (NumberFormatException e) {
                    out.println("Invalid input. Please enter an integer:");
                }
            }

            out.println("Enter your Y coordinate (integer between 0 and 100):");
            int y;
            while (true) {
                try {
                    y = Integer.parseInt(in.readLine().trim());
                    if (y >= 0 && y <= 100) break;
                    out.println("Invalid input. Please enter an integer between 0 and 100 for Y coordinate:");
                } catch (NumberFormatException e) {
                    out.println("Invalid input. Please enter an integer:");
                }
            }

            out.println("Are you an admin? (yes/no):");
            String adminResponse = in.readLine().trim().toLowerCase();
            boolean isAdmin = adminResponse.equals("yes");

            boolean success = User.register(username, password, isAdmin, x, y);
            return success ? new User(username, password, isAdmin, x, y) : null;
        }


        private User handleLogin(BufferedReader in, PrintWriter out) throws IOException {
            out.println("Enter your username:");
            String username = in.readLine().trim();

            if (username.isEmpty()) {
                out.println("Username cannot be empty. Please try again.");
                return null;
            }

            out.println("Enter your password:");
            String password = in.readLine().trim();

            if (password.isEmpty()) {
                out.println("Password cannot be empty. Please try again.");
                return null;
            }

            System.out.println("[DEBUG] Login attempt: username=" + username + ", password=" + password);

            // Gestionarea Optional<User>
            Optional<User> userOptional = User.login(username, password);
            if (userOptional.isPresent()) {
                User user = userOptional.get(); // Extrage valoarea din Optional
                out.println("Login successful. Welcome, " + username + "!");
                System.out.println("[DEBUG] Login successful for: " + username);
                return user;
            } else {
                out.println("Login failed. Please check your username and password.");
                System.out.println("[DEBUG] Login failed for: " + username);
                return null;
            }
        }


        private void handleGetWeather(User user, PrintWriter out) {
            try {
                int userX = user.getX();
                int userY = user.getY();

                WeatherData data = WeatherData.getWeatherDataByCoordinates(userX, userY);

                if (data != null) {
                    out.printf("Weather for your location (%s): %s, %d°C%n",
                            data.getLocatie(), data.getStareaVremii(), data.getTemperaturaDeAzi());
                    out.println("Temperatures for next 3 days: " + formatTemperatures(data.getTemperaturaPentruUrmatoarele3Zile()));
                } else {
                    out.println("No weather data available for your location.");
                }
            } catch (Exception e) {
                out.println("An error occurred while fetching weather data: " + e.getMessage());
            }
        }


        private void handleSetLocation(User user, BufferedReader in, PrintWriter out) throws IOException {
            if (!user.isAdmin()) {
                out.println("You don't have permission to set location data.");
                return;
            }

            out.println("Enter new X coordinate (integer between 0 and 100):");
            int newX;
            while (true) {
                try {
                    newX = Integer.parseInt(in.readLine().trim());
                    if (newX >= 0 && newX <= 100) break;
                    out.println("Invalid input. Please enter an integer between 0 and 100 for X coordinate:");
                } catch (NumberFormatException e) {
                    out.println("Invalid input. Please enter an integer:");
                }
            }

            out.println("Enter new Y coordinate (integer between 0 and 100):");
            int newY;
            while (true) {
                try {
                    newY = Integer.parseInt(in.readLine().trim());
                    if (newY >= 0 && newY <= 100) break;
                    out.println("Invalid input. Please enter an integer between 0 and 100 for Y coordinate:");
                } catch (NumberFormatException e) {
                    out.println("Invalid input. Please enter an integer:");
                }
            }

            if (User.updateCoordinates(user.getUsername(), newX, newY) != null) {

                user.setX(newX);
                user.setY(newY);
                out.println("Coordinates updated successfully!");
                System.out.printf("[SERVER] Admin %s updated their coordinates to (%d, %d).%n", user.getUsername(), newX, newY);
            } else {
                out.println("Failed to update coordinates. Please try again.");
            }
        }


        private void handleImportData(User user,PrintWriter out) {
            if (!user.isAdmin()) {
                out.println("You don't have permission to import data.");
                return;
            }
            out.println("[SERVER] Importing weather data from JSON...");
            try {
                JsonUtils.importWeatherData(FILE_PATH);
                out.println("[SERVER] Weather data imported and database updated successfully.");
            } catch (Exception e) {
                out.println("[SERVER] An error occurred during data import: " + e.getMessage());
            }
            out.println("End of data.");
            out.flush();
        }


        private String formatTemperatures(int[] temperatures) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < temperatures.length; i++) {
                sb.append(temperatures[i]).append("°C");
                if (i < temperatures.length - 1) {
                    sb.append(", ");
                }
            }
            return sb.toString();
        }
    }
}
