package dev.southside.module.impl.misc;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import dev.southside.Southside;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.event.events.WorldEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.client.Notification;
import dev.southside.module.impl.client.Targets;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.player.InventoryUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2561;
import net.minecraft.class_2616;
import net.minecraft.class_2663;

public class GodItemDetector extends Module {
   private final Set<UUID> notifiedPlayers = new HashSet<>();
   private final Map<UUID, String> lastItemInfo = new HashMap<>();
   public static final Set<class_1297> godAxeHolders = ConcurrentHashMap.newKeySet();
   public static final Set<class_1297> maceHolders = ConcurrentHashMap.newKeySet();
   public static final Set<class_1297> endCrystalHolders = ConcurrentHashMap.newKeySet();
   public static final Set<class_1297> eGappleHolders = ConcurrentHashMap.newKeySet();
   public static final Set<class_1297> kbBallHolders = ConcurrentHashMap.newKeySet();
   public static final Set<class_1297> totemHolders = ConcurrentHashMap.newKeySet();
   public static final Map<UUID, Long> totemPopTimestamps = new ConcurrentHashMap<>();
   private static final Map<UUID, String> totemPopNames = new ConcurrentHashMap<>();
   private static final long TOTEM_EFFECT_DURATION_MS = 10000L;
   private final Dropdown tagDisplay = new Dropdown(
      "God Item Tags", new String[]{"Disabled", "NameTag", "Detector", "Both"}, "Select which module renders God item tags", 4, 1
   );

   public GodItemDetector() {
      super(Category.Misc, "GodItemDetector", "防止被黑客偷袭，，，");
   }

   public static boolean shouldShowTag(GodItemDetector.TagTarget target) {
      GodItemDetector module = getInstance();
      if (module != null && module.isEnabled()) {
         GodItemDetector.TagDisplay display = GodItemDetector.TagDisplay.values()[module.tagDisplay.getValue()];
         return display == GodItemDetector.TagDisplay.BOTH
            || display == GodItemDetector.TagDisplay.NAMETAG && target == GodItemDetector.TagTarget.NAMETAG
            || display == GodItemDetector.TagDisplay.DETECTOR && target == GodItemDetector.TagTarget.DETECTOR;
      } else {
         return false;
      }
   }

   private static GodItemDetector getInstance() {
      try {
         return Southside.get().getModuleManager().get(GodItemDetector.class);
      } catch (Exception var1) {
         return null;
      }
   }

   public static boolean isGodAxe(class_1297 e) {
      GodItemDetector aaa = getInstance();
      return aaa != null && aaa.isEnabled() && godAxeHolders.contains(e);
   }

   public static boolean isMace(class_1297 e) {
      GodItemDetector aaa = getInstance();
      return aaa != null && aaa.isEnabled() && maceHolders.contains(e);
   }

   public static boolean isEndCrystal(class_1297 e) {
      GodItemDetector aaa = getInstance();
      return aaa != null && aaa.isEnabled() && endCrystalHolders.contains(e);
   }

   public static boolean isEGapple(class_1297 e) {
      GodItemDetector aaa = getInstance();
      return aaa != null && aaa.isEnabled() && eGappleHolders.contains(e);
   }

   public static boolean isKBBall(class_1297 e) {
      GodItemDetector aaa = getInstance();
      return aaa != null && aaa.isEnabled() && kbBallHolders.contains(e);
   }

   public static boolean isTotem(class_1297 e) {
      GodItemDetector aaa = getInstance();
      return aaa != null && aaa.isEnabled() && totemHolders.contains(e);
   }

   public static boolean hasTotemPopped(class_1297 e) {
      GodItemDetector aaa = getInstance();
      if (aaa != null && aaa.isEnabled()) {
         Long popTime = totemPopTimestamps.get(e.method_5667());
         return popTime == null ? false : System.currentTimeMillis() - popTime < 10000L;
      } else {
         return false;
      }
   }

   public static float getTotemRemainingSeconds(class_1297 e) {
      Long popTime = totemPopTimestamps.get(e.method_5667());
      if (popTime == null) {
         return 0.0F;
      } else {
         long elapsed = System.currentTimeMillis() - popTime;
         long remaining = 10000L - elapsed;
         return remaining > 0L ? (float)remaining / 1000.0F : 0.0F;
      }
   }

   private static void clearDetections() {
      godAxeHolders.clear();
      maceHolders.clear();
      endCrystalHolders.clear();
      eGappleHolders.clear();
      kbBallHolders.clear();
      totemHolders.clear();
      totemPopTimestamps.clear();
      totemPopNames.clear();
   }

   @EventHandler
   private void onWorldChange(WorldEvent event) {
      clearDetections();
      this.notifiedPlayers.clear();
      this.lastItemInfo.clear();
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         long now = System.currentTimeMillis();
         totemPopTimestamps.forEach((uuidx, popTime) -> {
            if (now - popTime >= 10000L) {
               totemPopTimestamps.remove(uuidx);
               totemPopNames.remove(uuidx);
            }
         });
         if (!totemPopTimestamps.isEmpty() && mc.field_1705 != null) {
            StringBuilder sb = new StringBuilder();

            for (Entry<UUID, Long> entry : totemPopTimestamps.entrySet()) {
               long elapsed = now - entry.getValue();
               float remaining = (float)(10000L - elapsed) / 1000.0F;
               if (!(remaining <= 0.0F)) {
                  String playerName = totemPopNames.getOrDefault(entry.getKey(), "???");
                  if (!sb.isEmpty()) {
                     sb.append("  ");
                  }

                  sb.append("§c[TotemPop] §f").append(playerName).append(" §eStrength: §c").append(String.format("%.1fs", remaining));
               }
            }

            if (!sb.isEmpty()) {
               mc.field_1705.method_1758(class_2561.method_43470(sb.toString()), false);
            }
         }

         godAxeHolders.removeIf(e -> !e.method_5805());
         maceHolders.removeIf(e -> !e.method_5805());
         endCrystalHolders.removeIf(e -> !e.method_5805());
         eGappleHolders.removeIf(e -> !e.method_5805());
         kbBallHolders.removeIf(e -> !e.method_5805());
         totemHolders.removeIf(e -> !e.method_5805());

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
         String name;
         if (entity instanceof class_1657 player && player.method_7334() != null && player.method_7334().getName() != null) {
            name = player.method_7334().getName();
         } else {
            name = entity.method_5476() != null ? entity.method_5476().getString() : "???";
         }

         String handStr = isOffhand ? " on offhand" : "";
         if (stack.method_7909().equals(class_1802.field_8825)) {
            if (stack.method_57353() != null && stack.method_57353().toString().contains("一刀999!")) {
               godAxeHolders.add(entity);
               this.notifiedPlayers.add(uuid);
               Notification.addNotificationKeepTime(
                  name + " has " + stack.method_7964().getString() + handStr, "GodAxe Detected!", Notification.NotificationType.WARN, 4.0
               );
            }
         } else if (stack.method_7909().equals(class_1802.field_49814)) {
            maceHolders.add(entity);
            this.notifiedPlayers.add(uuid);
            Notification.addNotificationKeepTime(
               name + " has " + stack.method_7964().getString() + handStr, "Mace Detected!", Notification.NotificationType.WARN, 4.0
            );
         } else if (stack.method_7909().equals(class_1802.field_8367)) {
            eGappleHolders.add(entity);
            this.notifiedPlayers.add(uuid);
            Notification.addNotificationKeepTime(
               name + " has " + stack.method_7964().getString() + handStr, "AppleCare+ Detected!", Notification.NotificationType.WARN, 4.0
            );
         } else if (stack.method_7909().equals(class_1802.field_8301)) {
            endCrystalHolders.add(entity);
            this.notifiedPlayers.add(uuid);
            Notification.addNotificationKeepTime(
               name + " has " + stack.method_7964().getString() + handStr, "EndCrystal Detected!", Notification.NotificationType.WARN, 4.0
            );
         } else if (InventoryUtils.isKBBall(stack)) {
            kbBallHolders.add(entity);
            this.notifiedPlayers.add(uuid);
            Notification.addNotificationKeepTime(
               name + " has " + stack.method_7964().getString() + handStr, "KBBall Detected!", Notification.NotificationType.WARN, 4.0
            );
         } else if (stack.method_7909().equals(class_1802.field_8288)) {
            totemHolders.add(entity);
            this.notifiedPlayers.add(uuid);
            Notification.addNotificationKeepTime(name + " has Totem" + handStr, "Totem Detected!", Notification.NotificationType.WARN, 4.0);
         }
      }
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (event.getOrigin() == TransferOrigin.RECEIVE) {
         if (mc.field_1687 != null) {
            if (event.getPacket() instanceof class_2616 animation && animation.method_11267() == 0) {
               class_1297 entity = mc.field_1687.method_8469(animation.method_11269());
               if (entity == null || !godAxeHolders.contains(entity)) {
                  return;
               }

               godAxeHolders.remove(entity);
               this.notifiedPlayers.remove(entity.method_5667());
               this.lastItemInfo.remove(entity.method_5667());
               String name;
               if (entity instanceof class_1657 player && player.method_7334() != null && player.method_7334().getName() != null) {
                  name = player.method_7334().getName();
               } else {
                  name = entity.method_5476() != null ? entity.method_5476().getString() : "???";
               }

               Notification.addNotificationKeepTime(name + "'s GodAxe has been used up!", "GodAxe Consumed!", Notification.NotificationType.INFO, 4.0);
            }

            if (event.getPacket() instanceof class_2663 statusPacket && statusPacket.method_11470() == 35) {
               class_1297 entityx = statusPacket.method_11469(mc.field_1687);
               if (entityx == null) {
                  return;
               }

               if (!(entityx instanceof class_1657 player)) {
                  return;
               }

               String name;
               if (player.method_7334() != null && player.method_7334().getName() != null) {
                  name = player.method_7334().getName();
               } else {
                  name = player.method_5476() != null ? player.method_5476().getString() : "???";
               }

               UUID uuid = player.method_5667();
               totemPopTimestamps.put(uuid, System.currentTimeMillis());
               totemPopNames.put(uuid, name);
               Notification.addNotificationKeepTime(name + " popped totem! Strength: 10s", "Totem Popped!", Notification.NotificationType.WARN, 4.0);
            }
         }
      }
   }

   @Override
   public void onDisable() {
      clearDetections();
      this.notifiedPlayers.clear();
      this.lastItemInfo.clear();
      super.onDisable();
   }

   private static enum TagDisplay {
      DISABLED,
      NAMETAG,
      DETECTOR,
      BOTH;
   }

   public static enum TagTarget {
      NAMETAG,
      DETECTOR;
   }
}
