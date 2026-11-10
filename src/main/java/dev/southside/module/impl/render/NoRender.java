package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.events.MotionEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.Entity.RemovalReason;

public class NoRender extends Module {
   public final Switch potions = new Switch("Potions", false);
   public final Switch explosions = new Switch("Explosions", false);
   public final Switch fireworks = new Switch("FlowerRocket", false);
   public final Switch hurtCam = new Switch("HurtCam", false);
   public final Switch armor = new Switch("Armor", false);
   public final Switch noArmorStands = new Switch("NoArmorStands", false);
   public final Switch breakParticles = new Switch("BreakParticles", false);
   public final Switch bossbar = new Switch("Bossbar", false);
   public final Switch nausea = new Switch("Nausea", false);
   public final Switch blindness = new Switch("Blindness", false);
   public final Switch vanillaEffects = new Switch("Vanilla Effects", false);
   public final Switch fishingHook = new Switch("Fishing Hook", true);

   public NoRender() {
      super(Category.Render, "NoRender", "remove rendering");
   }

   @EventHandler
   public void onMotion(MotionEvent event) {
      if (mc.player != null && mc.world != null) {
         for (Entity ent : mc.world.getEntities()) {
            if (ent instanceof PotionEntity && this.potions.getValue()) {
               mc.world.removeEntity(ent.getId(), RemovalReason.KILLED);
            }

            if (ent instanceof ArmorStandEntity && this.noArmorStands.getValue()) {
               mc.world.removeEntity(ent.getId(), RemovalReason.KILLED);
            }
         }
      }
   }
}
