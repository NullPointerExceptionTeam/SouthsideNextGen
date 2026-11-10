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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

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
            this.attackedEntities.put(event.target.getId(), System.currentTimeMillis());
         }
      }
   }

   @EventHandler
   public void onRemoveEntity(RemoveEntityEvent event) {
      if (this.shouldTrigger(event.entity)) {
         if (!this.lightningEntities.containsKey(event.entity.getId())) {
            this.spawnLightning(event.entity);
         }
      }
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (event.getPacket() instanceof EntityStatusS2CPacket packet) {
         if (mc.world != null && mc.player != null) {
            if (packet.getStatus() == 3) {
               Entity entity = packet.getEntity(mc.world);
               if (this.shouldTrigger(entity)) {
                  if (!this.lightningEntities.containsKey(entity.getId())) {
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
         if (mc.world != null && mc.player != null) {
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

   private void spawnLightning(Entity entity) {
      LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, mc.world);
      lightning.refreshPositionAfterTeleport(entity.getX(), entity.getY(), entity.getZ());
      mc.world.addEntity(lightning);
      this.lightningEntities.put(entity.getId(), System.currentTimeMillis());
   }

   private boolean shouldTrigger(Entity entity) {
      if (entity == null || mc.world == null || mc.player == null) {
         return false;
      } else if (!this.mobs.getValue() && !(entity instanceof PlayerEntity)) {
         return false;
      } else if (!(entity instanceof LivingEntity) || entity == mc.player) {
         return false;
      } else {
         return entity instanceof PlayerEntity && AntiBot.isBot(entity) ? false : this.all.getValue() || this.attackedEntities.containsKey(entity.getId());
      }
   }

   private boolean shouldTrackAttack(Entity entity) {
      if (entity == null || mc.world == null || mc.player == null) {
         return false;
      } else if (!this.mobs.getValue() && !(entity instanceof PlayerEntity)) {
         return false;
      } else {
         return entity instanceof LivingEntity && entity != mc.player ? !(entity instanceof PlayerEntity) || !AntiBot.isBot(entity) : false;
      }
   }
}
