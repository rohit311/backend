package com.example.grpc;

import java.io.IOException;

import io.grpc.Server;
import io.grpc.ServerBuilder;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args ) throws IOException, InterruptedException
    {
       // Create a new server to listen on port 8080
      Server server = ServerBuilder.forPort(443)
        .addService(new GreetingServiceImpl())
        .build();

      // Start the server
      server.start();

      // Server threads are running in the background.
      System.out.println("Server started");
      // Don't exit the main thread. Wait until server is terminated.
      server.awaitTermination();
    }
}
