package dev.southside.module.impl.client;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.gui.animations.Animation;
import cc.polyfrost.oneconfig.gui.animations.EaseInOutQuad;
import cc.polyfrost.oneconfig.internal.assets.SVGs;
import cc.polyfrost.oneconfig.renderer.asset.Icon;
import cc.polyfrost.oneconfig.utils.Notifications;
import cc.polyfrost.oneconfig.utils.gui.GuiUtils;
import cc.polyfrost.oneconfig.utils.gui.OneUIScreen;
import dev.southside.module.Category;
import dev.southside.module.Module;

public class Notification extends Module {
   private static Notification INSTANCE;
   public final Slider keepTimeValue = new Slider("Keep Time", 1, 0, 5, 0.1);
   public static final Icon ENABLED_ICON = new Icon(SVGs.NOTIFICATION_ENABLED);
   public static final Icon DISABLED_ICON = new Icon(SVGs.NOTIFICATION_DISABLED);
   public static final Icon INFO_ICON = new Icon(SVGs.INFO_ARROW);
   public static final Icon WARNING_ICON = new Icon(SVGs.WARNING);
   public static final Icon ERROR_ICON = new Icon(SVGs.ERROR);

   @Override
   public void onEnable() {
      this.setEnable(false);
   }

   public Notification() {
      super(Category.Client, "Notification");
      INSTANCE = this;
   }

   public static void addNotification(String message, String title, Notification.NotificationType type) {
      if (mc.field_1687 != null || mc.field_1755 instanceof OneUIScreen) {
         addNotificationKeepTime(message, title, type, INSTANCE.keepTimeValue.getValue().doubleValue());
      }
   }

   public static void addNotificationKeepTime(String message, String title, Notification.NotificationType type, double keepTime) {
      if (mc.field_1687 != null || mc.field_1755 instanceof OneUIScreen) {
         long time = System.currentTimeMillis();
         Animation animation = new EaseInOutQuad((int)(keepTime * 1000.0), 0.0F, 1.0F, false);
         Notifications.INSTANCE.send(title, message, getIcon(type), (float)(keepTime * 1000.0), () -> animation.get(GuiUtils.getDeltaTime()));
      }
   }

   private static Icon getIcon(Notification.NotificationType type) {
      return switch (type) {
         case INFO -> INFO_ICON;
         case WARN -> WARNING_ICON;
         case ERROR -> ERROR_ICON;
         case ENABLED -> ENABLED_ICON;
         case DISABLED -> DISABLED_ICON;
      };
   }

   public static enum NotificationType {
      INFO,
      WARN,
      ERROR,
      ENABLED,
      DISABLED;
   }
}
