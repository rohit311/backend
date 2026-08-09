package com.pubsub.events;


import com.pubsub.models.ProcessedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


@Component
@Slf4j
public class ProcessEventManager {
    private final Map<String, List<IProcessEventObserver>> observers = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(10); // Configurable thread pool size

    public void registerObserver(String event, IProcessEventObserver observer) {
        observers.computeIfAbsent(event, k -> new CopyOnWriteArrayList<>()).add(observer);
    }

    public void unregisterObserver(String event, IProcessEventObserver observer) {
        List<IProcessEventObserver> eventObservers = observers.get(event);
        if (eventObservers != null) {
            eventObservers.remove(observer);
        }
    }

    public void notifyObservers(String topic, ProcessedEvent event) throws Exception {
        List<IProcessEventObserver> eventObservers = observers.get(topic);
        if (eventObservers != null) {
            List<Future<?>> futures = new ArrayList<>();
            for (IProcessEventObserver observer : eventObservers) {
                Future<?> future = executor.submit(() -> {
                    try {
                        observer.onEvent(topic, event);
                    } catch (Exception e) {
                        log.error("Observer failed for topic: {}", topic, e);
                    }
                });
                futures.add(future);
            }
            // Wait for all observers to complete
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error waiting for observer completion", e);
                    Thread.currentThread().interrupt();
                }
            }
        } else {
            log.warn("No observers registered for topic: {}", topic);
        }
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Executor shutdown interrupted: {}", e.getMessage(), e);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}