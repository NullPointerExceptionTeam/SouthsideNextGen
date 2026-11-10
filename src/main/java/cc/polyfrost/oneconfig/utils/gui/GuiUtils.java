package cc.polyfrost.oneconfig.utils.gui;

import cc.polyfrost.oneconfig.events.EventManager;
import cc.polyfrost.oneconfig.gui.OneConfigGui;
import cc.polyfrost.oneconfig.libs.universal.UScreen;
import cc.polyfrost.oneconfig.platform.Platform;
import cc.polyfrost.oneconfig.utils.TickDelay;
import dev.southside.event.Stage;
import dev.southside.event.events.GameRendererDrawEvent;
import dev.southside.orbit.EventHandler;
import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import net.minecraft.util.Util;
import net.minecraft.client.gui.screen.Screen;

public final class GuiUtils {
   private static long time = -1L;
   private static long deltaTime = 17L;
   private static boolean wasMouseDown = false;
   private static final Deque<Optional<Screen>> screenQueue = new ConcurrentLinkedDeque<>();

   @Deprecated
   public static void displayScreen(Object screen) {
      displayScreen((Screen)screen);
   }

   public static void displayScreen(Screen screen) {
      displayScreen(screen, screen instanceof OneConfigGui ? 2 : 1);
   }

   public static void displayScreen(Screen screen, int ticks) {
      Optional<Screen> optional = screen == null ? Optional.empty() : Optional.of(screen);
      screenQueue.add(optional);
      new TickDelay(() -> {
         UScreen.displayScreen(optional.orElse(null));
         screenQueue.remove(optional);
      }, ticks);
   }

   public static Deque<Optional<Screen>> getScreenQueue() {
      return screenQueue;
   }

   public static void closeScreen() {
      UScreen.displayScreen(null);
   }

   public static float getDeltaTime() {
      return (float)deltaTime;
   }

   public static boolean wasMouseDown() {
      return wasMouseDown;
   }

   @EventHandler
   private void onRenderEvent(GameRendererDrawEvent event) {
      if (event.stage == Stage.PRE) {
         if (time == -1L) {
            time = Util.getMeasuringTimeMs();
         } else {
            long currentTime = Util.getMeasuringTimeMs();
            deltaTime = currentTime - time;
            time = currentTime;
         }
      } else if (event.stage == Stage.POST) {
         wasMouseDown = Platform.getMousePlatform().isButtonDown(0);
      }
   }

   static {
      EventManager.INSTANCE.register(new GuiUtils());
   }
}
