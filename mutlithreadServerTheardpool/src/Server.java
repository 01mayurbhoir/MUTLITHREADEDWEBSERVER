import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.concurrent.*;

public class Server {
    private final ExecutorService threadPool;
    private static final String WEB_ROOT = "./webroot"; // Directory for files

    public Server(int poolSize) {
        this.threadPool = Executors.newFixedThreadPool(poolSize);
    }

    public void handleClient(Socket clientSocket) {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStream outputStream = clientSocket.getOutputStream()
        ) {
            // Read the request line (only the first line for simplicity)
            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.isEmpty()){
                return;
            }

            String[] parts = requestLine.split(" ");
            if (parts.length < 2 || !"GET".equalsIgnoreCase(parts[0])) {
                sendErrorResponse(outputStream, "400 Bad Request");
                return;
            }

            System.out.println("Received: " + requestLine);

            // Determine the requested file
            String requestedFile = parts[1].equals("/") ? "/index.html" : parts[1];
            File file = new File(WEB_ROOT + requestedFile);

            if (file.exists() && !file.isDirectory()) {
                serveFile(outputStream, file);
            } else {
                sendErrorResponse(outputStream, "404 Not Found");
            }

        } catch (IOException ex) {
            System.err.println("Error handling client: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ex) {
                System.err.println("Error closing client socket: " + ex.getMessage());
            }
        }
    }

    private void serveFile(OutputStream outputStream, File file) {
        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());

            // Write HTTP response headers
            PrintWriter writer = new PrintWriter(outputStream, true);
            writer.println("HTTP/1.1 200 OK");
            writer.println("Content-Type: " + Files.probeContentType(file.toPath()));
            writer.println("Content-Length: " + fileContent.length);
            writer.println();
            writer.flush();

            // Write file content
            outputStream.write(fileContent);
            outputStream.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void sendErrorResponse(OutputStream outputStream, String error) {
        try (PrintWriter writer = new PrintWriter(outputStream, true)) {
            writer.println("HTTP/1.1 " + error);
            writer.println("Content-Type: text/plain");
            writer.println();
            writer.println(error);
        }
    }

    public static void main(String[] args) {
        int port = 8010;
        int poolSize = 10; // Adjust the pool size as needed
        Server server = new Server(poolSize);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setSoTimeout(70000);
            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();

                // Use the thread pool to handle the client
                server.threadPool.execute(() -> server.handleClient(clientSocket));
            }
        } catch (SocketTimeoutException ex) {
            System.out.println("Server timed out waiting for connections.");
        } catch (IOException ex) {
            System.err.println("Error starting server: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            // Shutdown the thread pool when the server exits
            server.threadPool.shutdown();
        }
    }
}
