package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.options.impl.Color;
import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import dev.southside.event.events.Render3DEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.render.Render3D;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public class ItemHighlight extends Module {
   private final Dropdown mode = new Dropdown("Mode", "Both", "Sides", "Lines", "Both");
   private final Color uColor = new Color("Color Sides", new OneColor(43, 248, 54, 45));
   private final Color uColorLine = new Color("Color Line", new OneColor(43, 248, 54, 90));
   private final Slider radius = new Slider("Radius", 32, 8, 64, 1);

   public ItemHighlight() {
      super(Category.Render, "ItemHighlight");
   }

   @EventHandler
   public void onRender3d(Render3DEvent event) {
      if (mc.player != null) {
         ItemStack stack = mc.player.getMainHandStack();
         if (stack.getItem() instanceof BlockItem blockItem) {
            Block var15 = blockItem.getBlock();
            int px = mc.player.getBlockX();
            int py = mc.player.getBlockY();
            int pz = mc.player.getBlockZ();
            int radius = this.radius.getValue().intValue();

            for (int x = px - radius; x <= px + radius; x++) {
               for (int y = py - radius; y <= py + radius; y++) {
                  for (int z = pz - radius; z <= pz + radius; z++) {
                     BlockPos pos = new BlockPos(x, y, z);
                     if (mc.world.getBlockState(pos).getBlock() == var15) {
                        String var13 = this.mode.getMode();
                        switch (var13) {
                           case "Sides":
                              Render3D.blockSides(pos, this.uColor.getValue().toJavaColor());
                              break;
                           case "Lines":
                              Render3D.blockLines(pos, this.uColorLine.getValue().toJavaColor());
                              break;
                           case "Both":
                           default:
                              Render3D.blockSides(pos, this.uColor.getValue().toJavaColor());
                              Render3D.blockLines(pos, this.uColorLine.getValue().toJavaColor());
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
