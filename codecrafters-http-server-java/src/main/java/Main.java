import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class Main {

  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage
    //
    try {
      ServerSocket serverSocket = new ServerSocket(4221);

       // Since the tester restarts your program quite often, setting SO_REUSEADDR
       // ensures that we don't run into 'Address already in use' errors
      serverSocket.setReuseAddress(true);

      Socket clientSocket = serverSocket.accept(); // Wait for connection from client.

      BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      String requestMessage = reader.readLine();
      List<String> requestParts = Arrays.stream(requestMessage.split(" ")).toList();

      System.out.println("request message: "+ requestMessage);
      OutputStream outputStream = clientSocket.getOutputStream();
      String responseMessage = "";

      if (requestParts.get(0).equals("GET")) {
        if ("/".equals(requestParts.get(1))) {
          responseMessage = "HTTP/1.1 200 OK\r\n\r\n";
        } else if (requestParts.get(1).startsWith("/echo/")) {
          String str = requestParts.get(1).split("/")[2];
          responseMessage =
              "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " +
              str.length() + "\r\n\r\n" + str;
        } else {
          responseMessage = "HTTP/1.1 404 Not Found\r\n\r\n";
        }
      }

      outputStream.write(responseMessage.getBytes(StandardCharsets.UTF_8));
      clientSocket.close();
     } catch (IOException e) {
       System.out.println("IOException: " + e.getMessage());
     }
  }
}
