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
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;

public class MurderFinder extends Module {
   private final Set<UUID> notifiedPlayers = new HashSet<>();
   private final Map<UUID, String> lastItemInfo = new HashMap<>();
   private static final Set<Entity> murders = new HashSet<>();

   public MurderFinder() {
      super(Category.Misc, "MurderFinder", "Finds players who are murdering you.");
   }

   public static boolean isMurder(Entity e) {
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
      if (mc.world != null && mc.player != null) {
         for (Entity entity : mc.world.getEntities()) {
            if (Targets.check(entity) && entity instanceof LivingEntity living && living != mc.player) {
               UUID uuid = living.getUuid();
               ItemStack main = living.getMainHandStack();
               ItemStack off = living.getOffHandStack();
               String currentInfo = (main.isEmpty() ? "" : main.getItem().toString() + main.getComponents())
                  + "|"
                  + (off.isEmpty() ? "" : off.getItem().toString() + off.getComponents());
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

   private void checkAndMarkItem(LivingEntity entity, ItemStack stack, boolean isOffhand) {
      UUID uuid = entity.getUuid();
      if (!stack.isEmpty() && !this.notifiedPlayers.contains(uuid)) {
         String name = entity.getDisplayName().getString();
         if (entity instanceof PlayerEntity player && player.getGameProfile() != null) {
            name = player.getGameProfile().getName();
         }

         if (stack.getItem() instanceof SwordItem) {
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
