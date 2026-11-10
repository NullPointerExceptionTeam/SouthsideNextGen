package dev.southside.module.impl.misc;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import com.mojang.authlib.GameProfile;
import dev.southside.Southside;
import dev.southside.anticheat.check.Check;
import dev.southside.anticheat.check.NormalCheck;
import dev.southside.anticheat.check.PacketCheck;
import dev.southside.anticheat.check.impl.BlinkCheck;
import dev.southside.anticheat.check.impl.FastWebCheck;
import dev.southside.anticheat.check.impl.ScaffoldBatchCheck;
import dev.southside.anticheat.check.impl.ScaffoldJumpingCheck;
import dev.southside.anticheat.check.impl.ScaffoldRotationCheck;
import dev.southside.anticheat.player.PlayerData;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.PlayerUpdateEvent;
import dev.southside.event.events.WorldEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.ChatUtils;
import dev.southside.utils.misc.ClientUtils;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.network.PlayerListEntry;

public class HackerDetector extends Module {
   public final Switch logValue = new Switch("Log", false);
   public final Switch NotificationValue = new Switch("Notification", true);
   public final Switch persistValue = new Switch("Persist", false);
   public final Slider flagVlValue = new Slider("Flag VL", 10.0, 1.0, 100.0);
   public final Switch fastWebCheckValue = new Switch("FastWeb Check", true);
   public final Switch blinkCheckValue = new Switch("Blink Check", true);
   public final Switch scaffoldCheckValue = new Switch("Scaffold Check", true);
   public final Switch testMode = new Switch("Test (Self)", false);
   public final Slider vlDecay = new Slider("VL Decay/s", 0.5, 0.0, 5.0, 0.1);
   private final List<Check> manageChecks = new ArrayList<>();
   private final ConcurrentHashMap<UUID, PlayerData> playDataMap = new ConcurrentHashMap<>();
   public List<PlayerEntity> hackerList = new ArrayList<>();
   private Map<UUID, Float> tempHackerList;
   public List<PlayerEntity> filteredHackerList = new ArrayList<>();
   private boolean initialized = false;

   public HackerDetector() {
      super(Category.Misc, "HackerDetector", "Detects hackers");
   }

   public static void save(Map<UUID, Float> flagMap) {
      File configFile = new File(Southside.FOLDER, "hackers.dat");

      try (ObjectOutputStream outputStream = new ObjectOutputStream(Files.newOutputStream(configFile.toPath()))) {
         outputStream.writeObject(flagMap);
      } catch (IOException var7) {
         var7.printStackTrace();
      }
   }

   public static Map<UUID, Float> loadCache() {
      File configFile = new File(Southside.FOLDER, "hackers.dat");

      try {
         Map var2;
         try (ObjectInputStream inputStream = new ObjectInputStream(Files.newInputStream(configFile.toPath()))) {
            var2 = (Map)inputStream.readObject();
         }

         return var2;
      } catch (ClassNotFoundException | IOException var6) {
         return null;
      }
   }

   public PlayerData getPlayData(UUID uuid) {
      return this.playDataMap.get(uuid);
   }

   @Override
   public void onEnable() {
      this.manageChecks.clear();
      this.manageChecks.add(new FastWebCheck());
      this.manageChecks.add(new BlinkCheck());
      this.manageChecks.add(new ScaffoldBatchCheck());
      this.manageChecks.add(new ScaffoldRotationCheck());
      this.manageChecks.add(new ScaffoldJumpingCheck());
      this.initialized = true;
      if (this.persistValue.getValue()) {
         this.tempHackerList = loadCache();
      }

      super.onEnable();
   }

   @Override
   public void onDisable() {
      if (this.persistValue.getValue()) {
         save(this.playDataMap.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getVl())));
      }

      this.manageChecks.clear();
      this.playDataMap.clear();
      this.hackerList.clear();
      this.filteredHackerList.clear();
      this.initialized = false;
      super.onDisable();
   }

   @EventHandler
   public void onWorld(WorldEvent event) {
      if (mc.world != null) {
         if (this.persistValue.getValue()) {
            save(this.playDataMap.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getVl())));
         }

         this.playDataMap.clear();
         this.hackerList.clear();
         this.filteredHackerList.clear();
      }
   }

   @EventHandler
   public void onUpdate(PlayerUpdateEvent event) {
      if (!ClientUtils.nullCheck()) {
         if (mc.player.age > 10) {
            float decayPerTick = this.vlDecay.getValue().floatValue() / 20.0F;

            for (Entity entity : mc.world.getEntities()) {
               if (entity instanceof PlayerEntity player
                  && (player != mc.player || this.testMode.getValue())
                  && (player == mc.player || !this.testMode.getValue())) {
                  PlayerData playerData;
                  if (this.playDataMap.containsKey(player.getUuid())) {
                     playerData = this.playDataMap.get(player.getUuid());
                  } else {
                     playerData = new PlayerData(player);
                     if (this.persistValue.getValue() && this.tempHackerList != null && this.tempHackerList.containsKey(player.getUuid())) {
                        playerData.setVl(this.tempHackerList.get(player.getUuid()));
                     }

                     this.playDataMap.put(player.getUuid(), playerData);
                  }

                  if (decayPerTick > 0.0F && playerData.getVl() > 0.0F) {
                     playerData.setVl(Math.max(0.0F, playerData.getVl() - decayPerTick));
                  }

                  for (Check check : this.manageChecks) {
                     if (check instanceof NormalCheck normalCheck && this.isCheckEnabled(check)) {
                        normalCheck.onUpdate(playerData);
                     }
                  }

                  if (playerData.getVl() > this.flagVlValue.getValue().intValue() && playerData.isFirstDectected()) {
                     ChatUtils.warning(player.getName().getString() + " flagged as hacker (VL: " + String.format("%.1f", playerData.getVl()) + ")");
                     if (!this.hackerList.contains(player)) {
                        this.hackerList.add(player);
                     }

                     playerData.setFirstDectected(false);
                  }

                  if (playerData.getVl() <= this.flagVlValue.getValue().intValue() * 0.5F) {
                     playerData.setFirstDectected(true);
                  }
               }
            }

            Set<UUID> tabPlayers = mc.getNetworkHandler()
               .getPlayerList()
               .stream()
               .map(PlayerListEntry::getProfile)
               .<UUID>map(GameProfile::getId)
               .collect(Collectors.toSet());
            this.hackerList.removeIf(p -> !tabPlayers.contains(p.getUuid()));
            this.filteredHackerList = this.hackerList
               .stream()
               .filter(playerx -> tabPlayers.contains(playerx.getUuid()))
               .filter(playerx -> playerx.getHealth() != 0.0F)
               .collect(Collectors.toList());
         }
      }
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (this.initialized) {
         for (Check check : this.manageChecks) {
            if (check instanceof PacketCheck packetCheck && this.isCheckEnabled(check)) {
               packetCheck.onPacket(event);
            }
         }
      }
   }

   private boolean isCheckEnabled(Check check) {
      if (check instanceof FastWebCheck) {
         return this.fastWebCheckValue.getValue();
      } else if (check instanceof BlinkCheck) {
         return this.blinkCheckValue.getValue();
      } else if (check instanceof ScaffoldBatchCheck) {
         return this.scaffoldCheckValue.getValue();
      } else if (check instanceof ScaffoldRotationCheck) {
         return this.scaffoldCheckValue.getValue();
      } else {
         return check instanceof ScaffoldJumpingCheck ? this.scaffoldCheckValue.getValue() : false;
      }
   }

   public boolean isHacker(Entity player) {
      PlayerData playerData = this.playDataMap.get(player.getUuid());
      return playerData != null && playerData.getVl() > this.flagVlValue.getValue().intValue();
   }
}
