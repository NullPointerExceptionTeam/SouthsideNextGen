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
import net.minecraft.block.AirBlock;
import net.minecraft.util.math.BlockPos;

public class Eagle extends Module {
   Dropdown sneakMode = new Dropdown("Sneak Mode", "Always", "Always", "Hold");
   private boolean shouldSneak = false;

   public Eagle() {
      super(Category.Player, "Eagle", "适合蹲搭");
   }

   @EventHandler
   public void onUpdate(MotionEvent event) {
      if (event.getState() == Stage.PRE && mc.world != null && mc.player != null) {
         if (!mc.player.isOnGround()) {
            this.shouldSneak = false;
         } else {
            float pitch = mc.player.getPitch();
            boolean lookingDown = pitch > 70.0F;
            boolean onEdge = mc.world
               .getBlockState(new BlockPos(mc.player.getBlockX(), mc.player.getBlockY() - 1, mc.player.getBlockZ()))
               .getBlock() instanceof AirBlock;
            boolean edgeAndLookingDown = onEdge && lookingDown;
            if (this.sneakMode.getMode().equals("Hold")) {
               boolean keyPressed = mc.options.sneakKey.isPressed();
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
