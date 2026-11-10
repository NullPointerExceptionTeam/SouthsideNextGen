package dev.southside.module.impl.world;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import dev.southside.event.events.PlayerUpdateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class FastPlace extends Module {
   public Slider tick = new Slider("Speed", 1, 0, 6, 1);

   public FastPlace() {
      super(Category.World, "FastPlace", "快速放方块");
   }

   @EventHandler
   public void onMotion(PlayerUpdateEvent event) {
      if (mc.player != null) {
         Item mainHand = mc.player.getMainHandStack().getItem();
         Item offHand = mc.player.getOffHandStack().getItem();
         if (mainHand instanceof BlockItem
               && mainHand != Items.ENDER_PEARL
               && mainHand != Items.EGG
               && mainHand != Items.SNOWBALL
               && mainHand != Items.EXPERIENCE_BOTTLE
            || offHand instanceof BlockItem
               && offHand != Items.ENDER_PEARL
               && offHand != Items.EGG
               && offHand != Items.SNOWBALL
               && offHand != Items.EXPERIENCE_BOTTLE) {
            mc.itemUseCooldown = Math.min(mc.itemUseCooldown, this.tick.getValue().intValue());
         }
      }
   }
}
