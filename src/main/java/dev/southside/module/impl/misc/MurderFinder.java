package dev.southside.module.impl.misc;

import dev.southside.Southside;
import dev.southside.event.events.TickEvent;
import dev.southside.event.events.WorldEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.client.Notification;
import dev.southside.module.impl.client.Targets;
import dev.southside.orbit.EventHandler;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1829;

public class MurderFinder extends Module {
   private final Set<UUID> notifiedPlayers = new HashSet<>();
   private final Map<UUID, String> lastItemInfo = new HashMap<>();
   private static final Set<class_1297> murders = new HashSet<>();

   public MurderFinder() {
      super(Category.Misc, "MurderFinder", "Finds players who are murdering you.");
   }

   public static boolean isMurder(class_1297 e) {
      return Southside.get().getModuleManager().get(MurderFinder.class).isEnabled() && murders.contains(e);
   }

   @EventHandler
   private void onWorldChange(WorldEvent event) {
      murders.clear();
      this.notifiedPlayers.clear();
      this.lastItemInfo.clear();
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         for (class_1297 entity : mc.field_1687.method_18112()) {
            if (Targets.check(entity) && entity instanceof class_1309 living && living != mc.field_1724) {
               UUID uuid = living.method_5667();
               class_1799 main = living.method_6047();
               class_1799 off = living.method_6079();
               String currentInfo = (main.method_7960() ? "" : main.method_7909().toString() + main.method_57353())
                  + "|"
                  + (off.method_7960() ? "" : off.method_7909().toString() + off.method_57353());
               String lastInfo = this.lastItemInfo.get(uuid);
               if (!currentInfo.equals(lastInfo)) {
                  this.notifiedPlayers.remove(uuid);
                  this.lastItemInfo.put(uuid, currentInfo);
               }

               this.checkAndMarkItem(living, main, false);
               this.checkAndMarkItem(living, off, true);
            }
         }
      }
   }

   private void checkAndMarkItem(class_1309 entity, class_1799 stack, boolean isOffhand) {
      UUID uuid = entity.method_5667();
      if (!stack.method_7960() && !this.notifiedPlayers.contains(uuid)) {
         String name = entity.method_5476().getString();
         if (entity instanceof class_1657 player && player.method_7334() != null) {
            name = player.method_7334().getName();
         }

         if (stack.method_7909() instanceof class_1829) {
            murders.add(entity);
            this.notifiedPlayers.add(uuid);
            Notification.addNotificationKeepTime(name + " is Murder", "Murder Finder!", Notification.NotificationType.WARN, 4.0);
         }
      }
   }

   @Override
   public void onDisable() {
      murders.clear();
      this.notifiedPlayers.clear();
      this.lastItemInfo.clear();
      super.onDisable();
   }
}
