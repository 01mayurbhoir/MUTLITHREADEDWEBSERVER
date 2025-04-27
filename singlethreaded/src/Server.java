import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;

public class Server {

    public void run() throws IOException {
        int port=8080;
        ServerSocket socket =new ServerSocket(port);
        socket.setSoTimeout(10000);
        while(true){
            try{
                System.out .println("Server is listening on port"+port);
                Socket acceptedConnection = socket.accept();
                System.out.println("connection accepted from client"+acceptedConnection.getRemoteSocketAddress());
                PrintWriter toclient= new PrintWriter(acceptedConnection.getOutputStream());
                BufferedReader fromclient= new BufferedReader(new InputStreamReader(acceptedConnection.getInputStream()));
                toclient.println("hello from the server");

                File htmlFile= new File("index.html");
                String content = new String(Files.readAllBytes(htmlFile.toPath()));

                PrintWriter out = new PrintWriter(acceptedConnection.getOutputStream(),true);
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/html");
                out.println("Content-Length:"+content.length());
                out.println();
                out.println(content);
                toclient.close();
                fromclient.close();
                out.close();;
                acceptedConnection.close();
                System.out.println("Response sent to client.");
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {
        Server server=new Server();
        try {
            server.run();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}