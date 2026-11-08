package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.events.MotionEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import net.minecraft.class_1297;
import net.minecraft.class_1531;
import net.minecraft.class_1686;
import net.minecraft.class_1297.class_5529;

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
      if (mc.field_1724 != null && mc.field_1687 != null) {
         for (class_1297 ent : mc.field_1687.method_18112()) {
            if (ent instanceof class_1686 && this.potions.getValue()) {
               mc.field_1687.method_2945(ent.method_5628(), class_5529.field_26998);
            }

            if (ent instanceof class_1531 && this.noArmorStands.getValue()) {
               mc.field_1687.method_2945(ent.method_5628(), class_5529.field_26998);
            }
         }
      }
   }
}
