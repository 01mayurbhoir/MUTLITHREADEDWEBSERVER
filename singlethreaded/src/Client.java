import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    public void run() throws UnknownHostException , IOException {
        int port=8080;
        InetAddress address=InetAddress.getByName("localhost");
        Socket socket =new Socket(address,port);
        PrintWriter toSocket=new PrintWriter(socket.getOutputStream());
        BufferedReader fromSocket=new BufferedReader(new InputStreamReader(socket.getInputStream()));
        toSocket.println("hello froom the client");
        String line= fromSocket.readLine();
        System.out.println("Response from the socket is :"+line);




        toSocket.println("GET /index.html"); // Request for the HTML file

        // Prepare to save the received HTML
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter("received.html"));

        // Read the server's response line by line
        String lines;
        while ((lines = fromSocket.readLine()) != null) {
            // Save each line of the response to a file
            fileWriter.write(lines);
            fileWriter.newLine();
        }

        System.out.println("HTML file received and saved as 'received.html'.");

        // Close the file writer




        toSocket.close();
        fromSocket.close();
        socket.close();
        fileWriter.close();

    }

    public static void main(String[] args){
        try {
            Client client =new Client();
            client.run();
        }catch (Exception ex){
           ex.printStackTrace();
        }
    }

}
