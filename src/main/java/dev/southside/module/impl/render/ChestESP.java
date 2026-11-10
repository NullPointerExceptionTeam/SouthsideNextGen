package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.options.impl.Color;
import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import dev.southside.Southside;
import dev.southside.event.events.Render3DEvent;
import dev.southside.manager.ChestManager;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.render.Render3D;
import dev.southside.utils.world.WorldUtils;
import net.minecraft.util.math.Box;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;

public class ChestESP extends Module {
   private final Dropdown mode = new Dropdown("Mode", "Both", "Sides", "Lines", "Both");
   private final Slider renderRange = new Slider("Range", 64.0, 30.0, 256.0, 1.0);
   private final Color uColor = new Color("UnOpened Color Sides", new OneColor(43, 248, 54, 45));
   private final Color oColor = new Color("Opened Color Sides", new OneColor(248, 48, 34, 45));
   private final Color uColorLine = new Color("UnOpened Color Line", new OneColor(43, 248, 54, 90));
   private final Color oColorLine = new Color("Opened Color Line", new OneColor(248, 48, 34, 90));
   private final Color sColor = new Color("Self Stolen Color Sides", new OneColor(210, 80, 100, 45));
   private final Color sColorLine = new Color("Self Stolen Color Line", new OneColor(210, 80, 100, 90));

   public ChestESP() {
      super(Category.Render, "ChestESP", "视奸附近的箱子");
   }

   @Override
   public void initPostRunnable() {
      super.initPostRunnable();
      this.hideIf(this.uColorLine.getLabel(), () -> this.mode.isMode("Sides"));
      this.hideIf(this.oColorLine.getLabel(), () -> this.mode.isMode("Sides"));
   }

   @EventHandler
   public void onRender(Render3DEvent event) {
      if (mc.world != null && mc.player != null) {
         double maxRange = this.renderRange.getValue().doubleValue();
         double maxRangeSq = maxRange * maxRange;

         for (BlockEntity e : WorldUtils.blockEntities()) {
            if (e instanceof ChestBlockEntity) {
               double distSq = mc.player
                  .getPos()
                  .squaredDistanceTo(e.getPos().getX() + 0.5, e.getPos().getY() + 0.5, e.getPos().getZ() + 0.5);
               if (!(distSq > maxRangeSq)) {
                  Box box = new Box(e.getPos());
                  boolean renderLines = this.mode.isMode("Lines") || this.mode.isMode("Both");
                  boolean renderSides = this.mode.isMode("Sides") || this.mode.isMode("Both");
                  ChestManager manager = Southside.get().getChestManager();
                  if (!manager.isStolen(e.getPos())) {
                     if (renderLines) {
                        Render3D.boxLines(box, this.uColorLine.getValue().toJavaColor());
                     }

                     if (renderSides) {
                        Render3D.boxSides(box, this.uColor.getValue().toJavaColor());
                     }
                  } else if (manager.isSelfStolen(e.getPos())) {
                     if (renderLines) {
                        Render3D.boxLines(box, this.sColorLine.getValue().toJavaColor());
                     }

                     if (renderSides) {
                        Render3D.boxSides(box, this.sColor.getValue().toJavaColor());
                     }
                  } else {
                     if (renderLines) {
                        Render3D.boxLines(box, this.oColorLine.getValue().toJavaColor());
                     }

                     if (renderSides) {
                        Render3D.boxSides(box, this.oColor.getValue().toJavaColor());
                     }
                  }
               }
            }
         }
      }
   }
}
