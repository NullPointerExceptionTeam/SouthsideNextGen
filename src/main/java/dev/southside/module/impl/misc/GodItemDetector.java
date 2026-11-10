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
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

public class GodItemDetector extends Module {
   private final Set<UUID> notifiedPlayers = new HashSet<>();
   private final Map<UUID, String> lastItemInfo = new HashMap<>();
   public static final Set<Entity> godAxeHolders = ConcurrentHashMap.newKeySet();
   public static final Set<Entity> maceHolders = ConcurrentHashMap.newKeySet();
   public static final Set<Entity> endCrystalHolders = ConcurrentHashMap.newKeySet();
   public static final Set<Entity> eGappleHolders = ConcurrentHashMap.newKeySet();
   public static final Set<Entity> kbBallHolders = ConcurrentHashMap.newKeySet();
   public static final Set<Entity> totemHolders = ConcurrentHashMap.newKeySet();
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

   public static boolean isGodAxe(Entity e) {
      GodItemDetector aaa = getInstance();
      return aaa != null && aaa.isEnabled() && godAxeHolders.contains(e);
   }

   public static boolean isMace(Entity e) {
      GodItemDetector aaa = getInstance();
      return aaa != null && aaa.isEnabled() && maceHolders.contains(e);
   }

   public static boolean isEndCrystal(Entity e) {
      GodItemDetector aaa = getInstance();
      return aaa != null && aaa.isEnabled() && endCrystalHolders.contains(e);
   }

   public static boolean isEGapple(Entity e) {
      GodItemDetector aaa = getInstance();
      return aaa != null && aaa.isEnabled() && eGappleHolders.contains(e);
   }

   public static boolean isKBBall(Entity e) {
      GodItemDetector aaa = getInstance();
      return aaa != null && aaa.isEnabled() && kbBallHolders.contains(e);
   }

   public static boolean isTotem(Entity e) {
      GodItemDetector aaa = getInstance();
      return aaa != null && aaa.isEnabled() && totemHolders.contains(e);
   }

   public static boolean hasTotemPopped(Entity e) {
      GodItemDetector aaa = getInstance();
      if (aaa != null && aaa.isEnabled()) {
         Long popTime = totemPopTimestamps.get(e.getUuid());
         return popTime == null ? false : System.currentTimeMillis() - popTime < 10000L;
      } else {
         return false;
      }
   }

   public static float getTotemRemainingSeconds(Entity e) {
      Long popTime = totemPopTimestamps.get(e.getUuid());
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
      if (mc.world != null && mc.player != null) {
         long now = System.currentTimeMillis();
         totemPopTimestamps.forEach((uuidx, popTime) -> {
            if (now - popTime >= 10000L) {
               totemPopTimestamps.remove(uuidx);
               totemPopNames.remove(uuidx);
            }
         });
         if (!totemPopTimestamps.isEmpty() && mc.inGameHud != null) {
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
               mc.inGameHud.setOverlayMessage(Text.literal(sb.toString()), false);
            }
         }

         godAxeHolders.removeIf(e -> !e.isAlive());
         maceHolders.removeIf(e -> !e.isAlive());
         endCrystalHolders.removeIf(e -> !e.isAlive());
         eGappleHolders.removeIf(e -> !e.isAlive());
         kbBallHolders.removeIf(e -> !e.isAlive());
         totemHolders.removeIf(e -> !e.isAlive());

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
         String name;
         if (entity instanceof PlayerEntity player && player.getGameProfile() != null && player.getGameProfile().getName() != null) {
            name = player.getGameProfile().getName();
         } else {
            name = entity.getDisplayName() != null ? entity.getDisplayName().getString() : "???";
         }

         String handStr = isOffhand ? " on offhand" : "";
         if (stack.getItem().equals(Items.GOLDEN_AXE)) {
            if (stack.getComponents() != null && stack.getComponents().toString().contains("一刀999!")) {
               godAxeHolders.add(entity);
               this.notifiedPlayers.add(uuid);
               Notification.addNotificationKeepTime(
                  name + " has " + stack.getName().getString() + handStr, "GodAxe Detected!", Notification.NotificationType.WARN, 4.0
               );
            }
         } else if (stack.getItem().equals(Items.MACE)) {
            maceHolders.add(entity);
            this.notifiedPlayers.add(uuid);
            Notification.addNotificationKeepTime(
               name + " has " + stack.getName().getString() + handStr, "Mace Detected!", Notification.NotificationType.WARN, 4.0
            );
         } else if (stack.getItem().equals(Items.ENCHANTED_GOLDEN_APPLE)) {
            eGappleHolders.add(entity);
            this.notifiedPlayers.add(uuid);
            Notification.addNotificationKeepTime(
               name + " has " + stack.getName().getString() + handStr, "AppleCare+ Detected!", Notification.NotificationType.WARN, 4.0
            );
         } else if (stack.getItem().equals(Items.END_CRYSTAL)) {
            endCrystalHolders.add(entity);
            this.notifiedPlayers.add(uuid);
            Notification.addNotificationKeepTime(
               name + " has " + stack.getName().getString() + handStr, "EndCrystal Detected!", Notification.NotificationType.WARN, 4.0
            );
         } else if (InventoryUtils.isKBBall(stack)) {
            kbBallHolders.add(entity);
            this.notifiedPlayers.add(uuid);
            Notification.addNotificationKeepTime(
               name + " has " + stack.getName().getString() + handStr, "KBBall Detected!", Notification.NotificationType.WARN, 4.0
            );
         } else if (stack.getItem().equals(Items.TOTEM_OF_UNDYING)) {
            totemHolders.add(entity);
            this.notifiedPlayers.add(uuid);
            Notification.addNotificationKeepTime(name + " has Totem" + handStr, "Totem Detected!", Notification.NotificationType.WARN, 4.0);
         }
      }
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (event.getOrigin() == TransferOrigin.RECEIVE) {
         if (mc.world != null) {
            if (event.getPacket() instanceof EntityAnimationS2CPacket animation && animation.getAnimationId() == 0) {
               Entity entity = mc.world.getEntityById(animation.getEntityId());
               if (entity == null || !godAxeHolders.contains(entity)) {
                  return;
               }

               godAxeHolders.remove(entity);
               this.notifiedPlayers.remove(entity.getUuid());
               this.lastItemInfo.remove(entity.getUuid());
               String name;
               if (entity instanceof PlayerEntity player && player.getGameProfile() != null && player.getGameProfile().getName() != null) {
                  name = player.getGameProfile().getName();
               } else {
                  name = entity.getDisplayName() != null ? entity.getDisplayName().getString() : "???";
               }

               Notification.addNotificationKeepTime(name + "'s GodAxe has been used up!", "GodAxe Consumed!", Notification.NotificationType.INFO, 4.0);
            }

            if (event.getPacket() instanceof EntityStatusS2CPacket statusPacket && statusPacket.getStatus() == 35) {
               Entity entityx = statusPacket.getEntity(mc.world);
               if (entityx == null) {
                  return;
               }

               if (!(entityx instanceof PlayerEntity player)) {
                  return;
               }

               String name;
               if (player.getGameProfile() != null && player.getGameProfile().getName() != null) {
                  name = player.getGameProfile().getName();
               } else {
                  name = player.getDisplayName() != null ? player.getDisplayName().getString() : "???";
               }

               UUID uuid = player.getUuid();
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
