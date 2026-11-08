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
import net.minecraft.class_1657;
import net.minecraft.class_266;
import net.minecraft.class_2757;
import net.minecraft.class_8646;

public class HealthBypass extends Module {
   public final Switch smooth = new Switch("Smooth", false);
   public static final Map<String, AtomicInteger> HEALTHS = new HashMap<>();
   private static final Map<String, Float> SMOOTH_HEALTHS = new HashMap<>();

   public HealthBypass() {
      super(Category.Player, "HealthBypass", "视奸正确的血量");
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (event.getPacket() instanceof class_2757 packet) {
         if (mc.field_1687 == null || packet.comp_2123() == null) {
            return;
         }

         class_266 objective = mc.field_1687.method_8428().method_1170(packet.comp_2123());
         if (mc.field_1687.method_8428().method_1189(class_8646.field_45158) == objective) {
            mc.execute(() -> {
               if (mc.field_1724 != null && !packet.comp_2122().equals(mc.field_1724.method_7334().getName())) {
                  if (!HEALTHS.containsKey(packet.comp_2122())) {
                     AtomicInteger atomic = new AtomicInteger();
                     HEALTHS.put(packet.comp_2122(), atomic);
                  }

                  HEALTHS.get(packet.comp_2122()).set(packet.comp_2124());
               }
            });
         }
      }
   }

   @EventHandler
   public void onUpdate(PlayerUpdateEvent event) {
      if (mc.field_1687 != null) {
         for (class_1657 player : mc.field_1687.method_18456()) {
            if (player != mc.field_1724 && HEALTHS.containsKey(player.method_5477().getString())) {
               String name = player.method_5477().getString();
               float target = Math.max(1, HEALTHS.get(name).get());
               if (this.smooth.getValue()) {
                  float current = SMOOTH_HEALTHS.getOrDefault(name, target);
                  float diff = target - current;
                  current += diff * 0.3F;
                  if (Math.abs(target - current) < 0.1F) {
                     current = target;
                  }

                  SMOOTH_HEALTHS.put(name, current);
                  player.method_6033(current);
               } else {
                  player.method_6033(target);
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
