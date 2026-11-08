package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import dev.southside.event.Stage;
import dev.southside.event.events.AttackEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;

public class CriticalParticle extends Module {
   public final Dropdown mode = new Dropdown("Mode", "Both", "Crit", "Enchant", "Both");
   public final Slider multiplier = new Slider("Multiplier", 1, 1, 10, 1);

   public CriticalParticle() {
      super(Category.Render, "CriticalParticle", "每次攻击显示暴击/击打粒子");
   }

   @EventHandler
   public void onAttack(AttackEvent event) {
      if (event.stage == Stage.POST) {
         if (mc.field_1724 != null && mc.field_1687 != null) {
            if (event.target != null) {
               int count = this.multiplier.getValue().intValue();
               int modeValue = this.mode.getValue();
               boolean showCrit = modeValue == 0 || modeValue == 2;
               boolean showEnchant = modeValue == 1 || modeValue == 2;

               for (int i = 0; i < count; i++) {
                  if (showCrit) {
                     mc.field_1724.method_7277(event.target);
                  }

                  if (showEnchant) {
                     mc.field_1724.method_7304(event.target);
                  }
               }
            }
         }
      }
   }
}
