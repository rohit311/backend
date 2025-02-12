import java.time.Instant;

public class FixedWindowCounter {
  private final long windowSizeInSeconds;
  private final long maxRequestsPerWindow;
  private long currentWindowStart;
  private long requestCount;

  public FixedWindowCounter(long windowSizeInSeconds, long maxRequestsPerWindow) {
    this.windowSizeInSeconds = windowSizeInSeconds;
    this.maxRequestsPerWindow = maxRequestsPerWindow;
    this.currentWindowStart = Instant.now().getEpochSecond();
    this.requestCount = 0;
  }

  public synchronized boolean allowRequest() {
    long now = Instant.now().getEpochSecond();

    if (now - currentWindowStart >= windowSizeInSeconds) {
      currentWindowStart = now;
      requestCount = 0;
    }

    if (requestCount < maxRequestsPerWindow) {
      requestCount++;
      return true;
    }

    return false;
  }
}
