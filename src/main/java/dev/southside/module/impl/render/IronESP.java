package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.options.impl.Color;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.events.Render2DEvent;
import dev.southside.event.events.Render3DEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.render.ProjectionUtil;
import dev.southside.render.Render3D;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2680;
import net.minecraft.class_2791;
import net.minecraft.class_2826;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_4587;

public class IronESP extends Module {
   private final Slider range = new Slider("Range", 32, 16, 64, 1);
   private final Switch boxLines = new Switch("Box Lines", true);
   private final Switch boxSides = new Switch("Box Sides", true);
   private final Switch tag = new Switch("Tag", true);
   private final Color lineColor = new Color("Line Color", new OneColor(255, 255, 255, 200));
   private final Color sideColor = new Color("Side Color", new OneColor(255, 255, 255, 50));
   private static final Set<class_2248> TARGET_BLOCKS = Set.of(class_2246.field_10212, class_2246.field_29027, class_2246.field_33508, class_2246.field_10085);
   private final List<class_2338> foundPositions = new ArrayList<>();
   private final List<String> foundNames = new ArrayList<>();

   public IronESP() {
      super(Category.Render, "IronESP", "铁矿石透视");
   }

   @EventHandler
   public void onRender3D(Render3DEvent event) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         this.foundPositions.clear();
         this.foundNames.clear();
         int rangeVal = this.range.getValue().intValue();
         int rangeSq = rangeVal * rangeVal;
         class_243 playerPos = mc.field_1724.method_19538();
         class_1923 playerChunk = mc.field_1724.method_31476();
         int chunkRange = (rangeVal >> 4) + 1;

         for (int cx = playerChunk.field_9181 - chunkRange; cx <= playerChunk.field_9181 + chunkRange; cx++) {
            for (int cz = playerChunk.field_9180 - chunkRange; cz <= playerChunk.field_9180 + chunkRange; cz++) {
               if (mc.field_1687.method_2935().method_12123(cx, cz)) {
                  class_2791 chunk = mc.field_1687.method_8497(cx, cz);
                  class_2826[] sections = chunk.method_12006();

                  for (int sectionIdx = 0; sectionIdx < sections.length; sectionIdx++) {
                     class_2826 section = sections[sectionIdx];
                     if (!section.method_38292()) {
                        int sectionY = chunk.method_31607() + (sectionIdx << 4);

                        for (int x = 0; x < 16; x++) {
                           for (int y = 0; y < 16; y++) {
                              for (int z = 0; z < 16; z++) {
                                 class_2680 state = section.method_12254(x, y, z);
                                 class_2248 block = state.method_26204();
                                 if (TARGET_BLOCKS.contains(block)) {
                                    int worldX = (cx << 4) + x;
                                    int worldY = sectionY + y;
                                    int worldZ = (cz << 4) + z;
                                    double dx = worldX + 0.5 - playerPos.field_1352;
                                    double dy = worldY + 0.5 - playerPos.field_1351;
                                    double dz = worldZ + 0.5 - playerPos.field_1350;
                                    if (!(dx * dx + dy * dy + dz * dz > rangeSq)) {
                                       class_2338 pos = new class_2338(worldX, worldY, worldZ);
                                       class_238 box = new class_238(pos);
                                       if (this.boxLines.getValue()) {
                                          Render3D.boxLines(box, this.lineColor.getValue().toJavaColor());
                                       }

                                       if (this.boxSides.getValue()) {
                                          Render3D.boxSides(box, this.sideColor.getValue().toJavaColor());
                                       }

                                       if (this.tag.getValue()) {
                                          this.foundPositions.add(pos);
                                          this.foundNames.add(this.getBlockDisplayName(block));
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onRender2D(Render2DEvent event) {
      if (mc.field_1687 != null && mc.field_1724 != null && this.tag.getValue()) {
         class_332 context = event.getDrawContext();
         class_327 textRenderer = mc.field_1772;

         for (int i = 0; i < this.foundPositions.size(); i++) {
            class_2338 pos = this.foundPositions.get(i);
            String name = this.foundNames.get(i);
            class_243 center = pos.method_46558().method_1031(0.0, 0.5, 0.0);
            class_241 screen = ProjectionUtil.worldScreenPos(center);
            if (screen != null) {
               int textWidth = textRenderer.method_1727(name);
               float scaleVal = 0.8F;
               class_4587 matrices = context.method_51448();
               matrices.method_22903();
               matrices.method_46416(screen.field_1343, screen.field_1342, 0.0F);
               matrices.method_22905(scaleVal, scaleVal, 1.0F);
               int bg = 1711276032;
               int padding = 3;
               int left = -textWidth / 2 - padding;
               int top = -9 - padding;
               int right = textWidth / 2 + padding;
               context.method_25294(left, top, right, padding, bg);
               context.method_51439(textRenderer, class_2561.method_43470(name), -textWidth / 2, top + padding, -1, true);
               matrices.method_22909();
            }
         }
      }
   }

   private String getBlockDisplayName(class_2248 block) {
      if (block == class_2246.field_10212) {
         return "铁矿";
      } else if (block == class_2246.field_29027) {
         return "深层铁矿";
      } else if (block == class_2246.field_33508) {
         return "粗铁块";
      } else {
         return block == class_2246.field_10085 ? "铁块" : "铁矿";
      }
   }
}
