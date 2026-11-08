package dev.southside.module.impl.player;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.events.MotionEvent;
import dev.southside.event.events.MovementInputEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.world.Scaffold;
import dev.southside.orbit.EventHandler;
import net.minecraft.class_2189;
import net.minecraft.class_2338;

public class Eagle extends Module {
   Dropdown sneakMode = new Dropdown("Sneak Mode", "Always", "Always", "Hold");
   private boolean shouldSneak = false;

   public Eagle() {
      super(Category.Player, "Eagle", "适合蹲搭");
   }

   @EventHandler
   public void onUpdate(MotionEvent event) {
      if (event.getState() == Stage.PRE && mc.field_1687 != null && mc.field_1724 != null) {
         if (!mc.field_1724.method_24828()) {
            this.shouldSneak = false;
         } else {
            float pitch = mc.field_1724.method_36455();
            boolean lookingDown = pitch > 70.0F;
            boolean onEdge = mc.field_1687
               .method_8320(new class_2338(mc.field_1724.method_31477(), mc.field_1724.method_31478() - 1, mc.field_1724.method_31479()))
               .method_26204() instanceof class_2189;
            boolean edgeAndLookingDown = onEdge && lookingDown;
            if (this.sneakMode.getMode().equals("Hold")) {
               boolean keyPressed = mc.field_1690.field_1832.method_1434();
               this.shouldSneak = !keyPressed && edgeAndLookingDown;
            } else {
               this.shouldSneak = edgeAndLookingDown;
            }
         }
      }
   }

   @EventHandler
   public void onMoveInput(MovementInputEvent event) {
      Scaffold scaffold = Southside.get().getModuleManager().get(Scaffold.class);
      if (scaffold == null || !scaffold.eagle.getValue() || !scaffold.isEnabled()) {
         if (this.shouldSneak) {
            event.setSneak(true);
         }
      }
   }
}
