package com.example.udpserver;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UdpserverApplication {
  static EchoClient client;

	public static void main(String[] args) throws SocketException, UnknownHostException {
		SpringApplication.run(UdpserverApplication.class, args);

    new EchoServer().start();
        try {
          client = new EchoClient();
          client.fetchUserInput();
        } catch (SocketException | UnknownHostException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
	}

}
