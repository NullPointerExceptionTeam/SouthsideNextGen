package cc.polyfrost.oneconfig.gui;

import cc.polyfrost.oneconfig.internal.gui.GuiNotification;
import cc.polyfrost.oneconfig.renderer.asset.SVG;
import cc.polyfrost.oneconfig.utils.InputHandler;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class GuiNotifications {
   public static final GuiNotifications INSTANCE = new GuiNotifications();
   private final CopyOnWriteArrayList<GuiNotification> notifications = new CopyOnWriteArrayList<>();
   private final ArrayList<GuiNotification> toRemove = new ArrayList<>();
   private final float DEFAULT_DURATION = 4000.0F;

   private GuiNotifications() {
   }

   public void sendNotification(String message) {
      this.sendNotification(message, 4000.0F);
   }

   public void sendNotification(String message, float duration) {
      this.sendNotification(message, duration, null);
   }

   public void sendNotification(String message, SVG icon) {
      this.sendNotification(message, 4000.0F, icon);
   }

   public void sendNotification(String message, float duration, SVG icon) {
      this.notifications.add(new GuiNotification(message, duration, icon));
   }

   public void draw(long vg, int x, int y, InputHandler inputHandler) {
      for (GuiNotification remove : this.toRemove) {
         this.notifications.remove(remove);
      }

      for (GuiNotification notification : this.notifications) {
         int draw = notification.draw(vg, x, y, inputHandler);
         if (draw == -1) {
            this.toRemove.add(notification);
         }

         y -= draw + 10;
      }
   }
}
