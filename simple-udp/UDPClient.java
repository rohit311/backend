import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UDPClient {
    
    public static void main(String[] args) throws IOException {
        DatagramPacket sendPacket;
        byte[] sendData;

        DatagramSocket clientSocket = new DatagramSocket();
        clientSocket.setSoTimeout(1000);
        Scanner input = new Scanner(System.in);

        while (true) {
            String cmd = input.nextLine();
            // If client types quit, close the socket and exit
            if (cmd.equals("QUIT")) {
                clientSocket.close();
                input.close();
                System.exit(1);
            }
            sendData = cmd.getBytes();
            sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("127.0.0.1"), 5001);
            clientSocket.send(sendPacket);
        }
    }
}
