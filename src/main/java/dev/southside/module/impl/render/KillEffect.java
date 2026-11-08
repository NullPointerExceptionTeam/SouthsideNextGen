package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.Stage;
import dev.southside.event.events.AttackEvent;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.RemoveEntityEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.event.events.WorldEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.misc.AntiBot;
import dev.southside.orbit.EventHandler;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1538;
import net.minecraft.class_1657;
import net.minecraft.class_2663;

public class KillEffect extends Module {
   private static final long ATTACK_TTL_MS = 15000L;
   private final Switch all = new Switch("All", false);
   private final Switch mobs = new Switch("Mobs", false);
   private final Map<Integer, Long> attackedEntities = new ConcurrentHashMap<>();
   private final Map<Integer, Long> lightningEntities = new ConcurrentHashMap<>();

   public KillEffect() {
      super(Category.Render, "KillEffect", "Kill special effects");
   }

   @Override
   public void initPostRunnable() {
      super.initPostRunnable();
      this.addListener(this.all.getLabel(), () -> this.setSuffix(this.all.getValue() ? "All" : "Attacked"));
      this.setSuffix(this.all.getValue() ? "All" : "Attacked");
   }

   @Override
   public void onDisable() {
      this.attackedEntities.clear();
      this.lightningEntities.clear();
   }

   @EventHandler
   public void onWorld(WorldEvent event) {
      this.attackedEntities.clear();
   }

   @EventHandler
   public void onAttack(AttackEvent event) {
      if (event.stage == Stage.POST) {
         if (this.shouldTrackAttack(event.target)) {
            this.attackedEntities.put(event.target.method_5628(), System.currentTimeMillis());
         }
      }
   }

   @EventHandler
   public void onRemoveEntity(RemoveEntityEvent event) {
      if (this.shouldTrigger(event.entity)) {
         if (!this.lightningEntities.containsKey(event.entity.method_5628())) {
            this.spawnLightning(event.entity);
         }
      }
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (event.getPacket() instanceof class_2663 packet) {
         if (mc.field_1687 != null && mc.field_1724 != null) {
            if (packet.method_11470() == 3) {
               class_1297 entity = packet.method_11469(mc.field_1687);
               if (this.shouldTrigger(entity)) {
                  if (!this.lightningEntities.containsKey(entity.method_5628())) {
                     this.spawnLightning(entity);
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (event.getState() == Stage.POST) {
         if (mc.field_1687 != null && mc.field_1724 != null) {
            if (!this.attackedEntities.isEmpty()) {
               this.attackedEntities.forEach((id, time) -> {
                  if (System.currentTimeMillis() - time > 15000L) {
                     this.attackedEntities.remove(id);
                  }
               });
            }

            if (!this.lightningEntities.isEmpty()) {
               this.lightningEntities.forEach((id, time) -> {
                  if (System.currentTimeMillis() - time > 5000L) {
                     this.lightningEntities.remove(id);
                  }
               });
            }
         }
      }
   }

   private void spawnLightning(class_1297 entity) {
      class_1538 lightning = new class_1538(class_1299.field_6112, mc.field_1687);
      lightning.method_24203(entity.method_23317(), entity.method_23318(), entity.method_23321());
      mc.field_1687.method_53875(lightning);
      this.lightningEntities.put(entity.method_5628(), System.currentTimeMillis());
   }

   private boolean shouldTrigger(class_1297 entity) {
      if (entity == null || mc.field_1687 == null || mc.field_1724 == null) {
         return false;
      } else if (!this.mobs.getValue() && !(entity instanceof class_1657)) {
         return false;
      } else if (!(entity instanceof class_1309) || entity == mc.field_1724) {
         return false;
      } else {
         return entity instanceof class_1657 && AntiBot.isBot(entity) ? false : this.all.getValue() || this.attackedEntities.containsKey(entity.method_5628());
      }
   }

   private boolean shouldTrackAttack(class_1297 entity) {
      if (entity == null || mc.field_1687 == null || mc.field_1724 == null) {
         return false;
      } else if (!this.mobs.getValue() && !(entity instanceof class_1657)) {
         return false;
      } else {
         return entity instanceof class_1309 && entity != mc.field_1724 ? !(entity instanceof class_1657) || !AntiBot.isBot(entity) : false;
      }
   }
}
