package dev.southside.module.impl.world;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.events.PacketEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.client.Notification;
import dev.southside.orbit.EventHandler;
import net.minecraft.class_1299;
import net.minecraft.class_2604;
import net.minecraft.class_2767;
import net.minecraft.class_3417;

public class LightningTracker extends Module {
   private final Switch useAddEntity = new Switch("Use AddEntity", false);
   private final Switch useSound = new Switch("Use Sound", false);

   public LightningTracker() {
      super(Category.World, "LightningTracker");
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (this.useAddEntity.getValue() && event.getPacket() instanceof class_2604 packet && packet.method_11169() == class_1299.field_6112) {
         double x = packet.method_11175();
         double y = packet.method_11174();
         double z = packet.method_11176();
         String msg = String.format("[Lightning Tracker] X=%.1f, Y=%.1f, Z=%.1f", x, y, z);
         this.warning(msg, new Object[0]);
         Notification.addNotificationKeepTime(msg, "LightningTracker", Notification.NotificationType.WARN, 10.0);
      }

      if (this.useSound.getValue()
         && event.getPacket() instanceof class_2767 packet
         && (packet.method_11894().equals(class_3417.field_14865) || packet.method_11894().equals(class_3417.field_14956))) {
         double x = packet.method_11890();
         double y = packet.method_11889();
         double z = packet.method_11893();
         String msg = String.format("[Lightning Tracker] X=%.1f, Y=%.1f, Z=%.1f", x, y, z);
         this.warning(msg, new Object[0]);
         Notification.addNotificationKeepTime(msg, "LightningTracker", Notification.NotificationType.WARN, 10.0);
      }
   }
}
