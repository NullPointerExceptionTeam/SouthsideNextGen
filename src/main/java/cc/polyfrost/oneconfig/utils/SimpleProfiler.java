package cc.polyfrost.oneconfig.utils;

import java.util.HashMap;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class SimpleProfiler {
   static final HashMap<String, Long> startTimes = new HashMap<>();
   static final Logger LOGGER = LogManager.getLogger("OneConfig Profiler");

   public static boolean push(@NotNull String msg) {
      return startTimes.put(msg, System.nanoTime()) == null;
   }

   public static float pop(@NotNull String msg) {
      return pop(msg, Level.DEBUG);
   }

   public static float pop(@NotNull String msg, @NotNull Level level) {
      float time = (float)(System.nanoTime() - startTimes.remove(msg)) / 1000000.0F;
      LOGGER.log(level, msg + " took " + time + "ms");
      return time;
   }
}
