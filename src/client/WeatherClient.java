package client;

import java.io.*;
import java.net.Socket;

public class WeatherClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connected to Weather Server!");

            String serverResponse;
            boolean awaitingInput = false;

            while ((serverResponse = in.readLine()) != null) {

                System.out.println("Server: " + serverResponse);

                if (serverResponse.equals("End of data.")) {
                    awaitingInput = true;
                    continue;
                }

                if (serverResponse.equalsIgnoreCase("Goodbye!") ||
                        serverResponse.equalsIgnoreCase("Connection closed.")) {
                    System.out.println("Closing client...");
                    break;
                }

                if (serverResponse.endsWith(":") || serverResponse.endsWith("?")) {
                    awaitingInput = true;
                }

                if (awaitingInput) {
                    System.out.print("> ");
                    String userInput = console.readLine();

                    if (userInput != null && !userInput.trim().isEmpty()) {
                        out.println(userInput.trim());
                        awaitingInput = false;
                    } else {
                        System.out.println("[DEBUG] No input provided. Please enter a valid response.");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
