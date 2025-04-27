import java.io.*;
import java.net.Socket;

class Client {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 8010;

        try (Socket socket = new Socket(host, port);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             InputStream inputStream = socket.getInputStream()) {

            // Send a basic HTTP GET request
            String requestedFile = "/index.html"; // Change this to the file you want to request
            writer.println("GET " + requestedFile + " HTTP/1.1");
            writer.println("Host: " + host);
            writer.println(); // Blank line to indicate end of request headers

            // Read and parse the response headers
            String line;
            int contentLength = 0;
            boolean headersDone = false;

            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    headersDone = true;
                    break;
                }
                System.out.println(line); // Print headers for debugging
                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
            }

            if (!headersDone) {
                System.out.println("Invalid response from server.");
                return;
            }

            // Read the file content
            if (contentLength > 0) {
                byte[] buffer = new byte[contentLength];
                int bytesRead = 0, totalRead = 0;

                File outputFile = new File("downloaded_" + requestedFile.substring(1));
                try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
                    while (totalRead < contentLength && (bytesRead = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                        totalRead += bytesRead;
                    }
                }

                System.out.println("File downloaded: " + outputFile.getAbsolutePath());
            } else {
                System.out.println("No content received from server.");
            }

        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
