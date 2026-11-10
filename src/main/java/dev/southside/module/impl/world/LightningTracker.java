package dev.southside.module.impl.world;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.events.PacketEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.client.Notification;
import dev.southside.orbit.EventHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;

public class LightningTracker extends Module {
   private final Switch useAddEntity = new Switch("Use AddEntity", false);
   private final Switch useSound = new Switch("Use Sound", false);

   public LightningTracker() {
      super(Category.World, "LightningTracker");
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (this.useAddEntity.getValue() && event.getPacket() instanceof EntitySpawnS2CPacket packet && packet.getEntityType() == EntityType.LIGHTNING_BOLT) {
         double x = packet.getX();
         double y = packet.getY();
         double z = packet.getZ();
         String msg = String.format("[Lightning Tracker] X=%.1f, Y=%.1f, Z=%.1f", x, y, z);
         this.warning(msg, new Object[0]);
         Notification.addNotificationKeepTime(msg, "LightningTracker", Notification.NotificationType.WARN, 10.0);
      }

      if (this.useSound.getValue()
         && event.getPacket() instanceof PlaySoundS2CPacket packet
         && (packet.getSound().equals(SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER) || packet.getSound().equals(SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT))) {
         double x = packet.getX();
         double y = packet.getY();
         double z = packet.getZ();
         String msg = String.format("[Lightning Tracker] X=%.1f, Y=%.1f, Z=%.1f", x, y, z);
         this.warning(msg, new Object[0]);
         Notification.addNotificationKeepTime(msg, "LightningTracker", Notification.NotificationType.WARN, 10.0);
      }
   }
}
