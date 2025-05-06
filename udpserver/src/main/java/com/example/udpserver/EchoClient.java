package com.example.udpserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class EchoClient {
  private DatagramSocket socket;
  private InetAddress address;

  private byte[] buf;

  public EchoClient() throws SocketException, UnknownHostException {
      socket = new DatagramSocket();
      address = InetAddress.getByName("localhost");
  }

  public String sendEcho(String msg) throws IOException {
      buf = msg.getBytes();
      DatagramPacket packet
        = new DatagramPacket(buf, buf.length, address, 4445);
      socket.send(packet);
      packet = new DatagramPacket(buf, buf.length);
      socket.receive(packet);
      String received = new String(
        packet.getData(), 0, packet.getLength());
      return received;
  }

  public void fetchUserInput() throws IOException {
    Scanner myObj = new Scanner(System.in);

    String inp = "";

    while (inp.toLowerCase() != "end") {
      inp = myObj.nextLine();
      String receivedData = sendEcho(inp);
      System.out.println("client data: "+ receivedData);

      System.out.println("has client terminated: "+ (inp));
    }

    myObj.close();
    close();
  }

  public void close() {
      socket.close();
  }
}
