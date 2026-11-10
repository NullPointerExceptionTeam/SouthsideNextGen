package dev.southside.module.impl.player;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.PlayerUpdateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.network.packet.s2c.play.ScoreboardScoreUpdateS2CPacket;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;

public class HealthBypass extends Module {
   public final Switch smooth = new Switch("Smooth", false);
   public static final Map<String, AtomicInteger> HEALTHS = new HashMap<>();
   private static final Map<String, Float> SMOOTH_HEALTHS = new HashMap<>();

   public HealthBypass() {
      super(Category.Player, "HealthBypass", "视奸正确的血量");
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (event.getPacket() instanceof ScoreboardScoreUpdateS2CPacket packet) {
         if (mc.world == null || packet.objectiveName() == null) {
            return;
         }

         ScoreboardObjective objective = mc.world.getScoreboard().getNullableObjective(packet.objectiveName());
         if (mc.world.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME) == objective) {
            mc.execute(() -> {
               if (mc.player != null && !packet.scoreHolderName().equals(mc.player.getGameProfile().getName())) {
                  if (!HEALTHS.containsKey(packet.scoreHolderName())) {
                     AtomicInteger atomic = new AtomicInteger();
                     HEALTHS.put(packet.scoreHolderName(), atomic);
                  }

                  HEALTHS.get(packet.scoreHolderName()).set(packet.score());
               }
            });
         }
      }
   }

   @EventHandler
   public void onUpdate(PlayerUpdateEvent event) {
      if (mc.world != null) {
         for (PlayerEntity player : mc.world.getPlayers()) {
            if (player != mc.player && HEALTHS.containsKey(player.getName().getString())) {
               String name = player.getName().getString();
               float target = Math.max(1, HEALTHS.get(name).get());
               if (this.smooth.getValue()) {
                  float current = SMOOTH_HEALTHS.getOrDefault(name, target);
                  float diff = target - current;
                  current += diff * 0.3F;
                  if (Math.abs(target - current) < 0.1F) {
                     current = target;
                  }

                  SMOOTH_HEALTHS.put(name, current);
                  player.setHealth(current);
               } else {
                  player.setHealth(target);
               }
            }
         }
      }
   }

   @Override
   public void onDisable() {
      SMOOTH_HEALTHS.clear();
   }
}
