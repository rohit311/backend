import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;

public class LeakyBucket {
  private final long capacity;        // Maximum number of requests the bucket can hold
  private final double leakRate;      // Rate at which requests leak out of the bucket (requests per second)
  private final Queue<Instant> bucket; // Queue to hold timestamps of requests
  private Instant lastLeakTimestamp;   // Last time we leaked from the bucket

  public LeakyBucket(long capacity, double leakRate) {
    this.capacity = capacity;
    this.leakRate = leakRate;
    this.bucket = new LinkedList<>();
    this.lastLeakTimestamp = Instant.now();
  }

  public synchronized boolean allowRequest() {
    leak();

    if (bucket.size() < capacity) {
      bucket.offer(Instant.now());

      return true;
    }

    return false;
  }

  private void leak() {
    Instant now = Instant.now();
    long elapsedTimeInMills = now.toEpochMilli() - this.lastLeakTimestamp.toEpochMilli();
    int leakCount = (int)(elapsedTimeInMills*this.leakRate / 1000.0);

    for (int i =0; i< leakCount && !bucket.isEmpty(); i++) {
      bucket.poll();
    }

    lastLeakTimestamp = now;
  }

}
