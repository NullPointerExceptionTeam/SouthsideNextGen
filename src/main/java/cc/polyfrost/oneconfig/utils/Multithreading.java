package cc.polyfrost.oneconfig.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Multithreading {
   private static final ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("OneConfig-%d").build());
   private static final ScheduledExecutorService runnableExecutor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() + 1);

   public static void runAsync(Runnable runnable) {
      submit(runnable);
   }

   public static void runAsync(Runnable... runnables) {
      for (Runnable runnable : runnables) {
         runAsync(runnable);
      }
   }

   public static Future<?> submit(Runnable runnable) {
      return executorService.submit(runnable);
   }

   public static void schedule(Runnable runnable, long delay, TimeUnit timeUnit) {
      submitScheduled(runnable, delay, timeUnit);
   }

   public static ScheduledFuture<?> submitScheduled(Runnable runnable, long delay, TimeUnit timeUnit) {
      return runnableExecutor.schedule(runnable, delay, timeUnit);
   }
}
