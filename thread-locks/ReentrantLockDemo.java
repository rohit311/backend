import java.util.concurrent.locks.ReentrantLock;

class ReentrantLockDemo {
  private static int sharedResource = 0;

  private static ReentrantLock lock = new ReentrantLock();

  public static void increment()  {

      // acquire the lock
      lock.lock();
      try
      {
        sharedResource++;
          System.out.println(Thread.currentThread().getName()
                              + " incremented counter to: " + sharedResource);
      }

    finally
        {

          // unlock used to
          // release the lock
          lock.unlock();
        }
  }

  public static void main(String[] args) {

    Runnable task = () -> {
        for (int i = 1; i < 3 ; i++) {
            increment();
        }
    };

    Thread t1 = new Thread(task, "Thread-1");
    Thread t2 = new Thread(task, "Thread-2");

    t1.start();
    t2.start();
}
}