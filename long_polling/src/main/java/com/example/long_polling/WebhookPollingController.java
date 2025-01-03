package com.example.long_polling;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/poll")
public class WebhookPollingController {

  private String webHookData = ""; // Placeholder for webhook data
  private final List<CompletableFuture<String>> clients = new CopyOnWriteArrayList<>(); // List of waiting clients

  @GetMapping("/webhook-data")
  public CompletableFuture<String> getWebHookData() {

    CompletableFuture<String> future = new CompletableFuture<>();

    if (webHookData != null) {
      future.complete("Webhook updated data: " + webHookData);
    } else {
      clients.add(future);

      CompletableFuture.delayedExecutor(30, TimeUnit.SECONDS).execute(() -> {
        if (!future.isDone()) {
          future.complete("No new updates within 30 seconds");
        }

        clients.remove(future); // Remove the client after completing the request
      });
    }

    return future;
  }

  // Webhook endpoint to simulate an external service updating the data
    @PostMapping("/webhook")
    public String receiveWebhook(@RequestBody String data) {
        // Update the webhook data
        webHookData = data;

        // Save the data to the DB if required

        // Notify all clients that are waiting for data
        for (CompletableFuture<String> client : clients) {
            client.complete("Webhook updated data: " + webHookData);
        }
        clients.clear(); // Clear the clients list after notifying them

        return "Data received from webhook: " + data;
    }
}
