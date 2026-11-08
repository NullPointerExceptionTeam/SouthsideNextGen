package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import dev.southside.event.events.Render2DEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.render.ProjectionUtil;
import dev.southside.utils.player.SlotUtils;
import dev.southside.utils.world.BlockEntityIterator;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2244;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_2587;
import net.minecraft.class_2680;
import net.minecraft.class_2742;
import net.minecraft.class_332;
import net.minecraft.class_4587;

public class BedShow extends Module {
   private static final int HORIZONTAL_RANGE = 3;
   private static final int ICON_SIZE = 16;
   private static final int ICON_GAP = 2;
   private static final int ICONS_PER_ROW = 4;
   private static final int PADDING = 4;
   private static final int MAX_ICONS = 12;
   private final Slider searchRadius = new Slider("Search Radius", 18.0F, 6.0F, 48.0F, 1.0F);
   private final Slider scale = new Slider("Scale", 1.0F, 0.5F, 2.5F, 0.05F);
   private final Slider backgroundAlpha = new Slider("Background Alpha", 0.2F, 0.0F, 1.0F, 0.05F);

   public BedShow() {
      super(Category.Render, "BedShow", "显示床防御所用方块");
   }

   @EventHandler
   private void onRender2D(Render2DEvent event) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         List<BedShow.BedInfo> beds = this.collectBeds();
         if (!beds.isEmpty()) {
            class_332 context = event.getDrawContext();

            for (BedShow.BedInfo bed : beds) {
               class_241 screenPos = ProjectionUtil.worldScreenPos(bed.anchor());
               if (screenPos != null) {
                  this.renderBedInfo(context, screenPos, bed.blocks());
               }
            }
         }
      }
   }

   private List<BedShow.BedInfo> collectBeds() {
      List<BedShow.BedInfo> beds = new ArrayList<>();
      class_1657 player = mc.field_1724;
      if (player != null && mc.field_1687 != null) {
         double radius = this.searchRadius.getValue().doubleValue() + 3.0;
         double radiusSq = radius * radius;
         BlockEntityIterator iterator = new BlockEntityIterator();

         while (iterator.hasNext()) {
            if (iterator.next() instanceof class_2587 bedEntity) {
               class_2338 pos = bedEntity.method_11016();
               if (!(player.method_5649(pos.method_10263() + 0.5, pos.method_10264() + 0.5, pos.method_10260() + 0.5) > radiusSq)) {
                  class_2680 state = mc.field_1687.method_8320(pos);
                  if (state.method_26204() instanceof class_2244 bedBlock && state.method_11654(class_2244.field_9967) == class_2742.field_12560) {
                     BedShow.BedInfo info = this.buildInfo(pos, state);
                     if (!info.blocks().isEmpty()) {
                        beds.add(info);
                     }
                  }
               }
            }
         }

         return beds;
      } else {
         return beds;
      }
   }

   private BedShow.BedInfo buildInfo(class_2338 headPos, class_2680 headState) {
      if (mc.field_1687 == null) {
         return new BedShow.BedInfo(class_243.method_24953(headPos), List.of());
      } else {
         class_2350 facing = (class_2350)headState.method_11654(class_2244.field_11177);
         class_2338 footPos = headPos.method_10093(facing.method_10153());
         Set<class_2338> bedBlocks = new LinkedHashSet<>();
         bedBlocks.add(headPos.method_10062());
         bedBlocks.add(footPos.method_10062());
         LinkedHashSet<class_2338> surrounding = new LinkedHashSet<>();

         for (class_2338 base : bedBlocks) {
            class_2338 above = base.method_10084();
            if (!bedBlocks.contains(above)) {
               surrounding.add(above.method_10062());
            }

            for (int dx = -3; dx <= 3; dx++) {
               for (int dz = -3; dz <= 3; dz++) {
                  if ((dx != 0 || dz != 0) && Math.max(Math.abs(dx), Math.abs(dz)) <= 3) {
                     class_2338 target = base.method_10069(dx, 0, dz);
                     if (!bedBlocks.contains(target)) {
                        surrounding.add(target.method_10062());
                     }
                  }
               }
            }
         }

         List<class_1799> stacks = new ArrayList<>();
         Set<class_1792> seenItems = new LinkedHashSet<>();

         for (class_2338 pos : surrounding) {
            class_2680 state = mc.field_1687.method_8320(pos);
            if (!state.method_26215() && SlotUtils.isGoodForBridging(state.method_26204())) {
               class_1792 item = state.method_26204().method_8389();
               if (item != class_1802.field_8162 && seenItems.add(item)) {
                  stacks.add(new class_1799(item));
                  if (stacks.size() >= 12) {
                     break;
                  }
               }
            }
         }

         class_243 anchor = class_243.method_24953(headPos).method_1031(0.0, 0.75, 0.0);
         return new BedShow.BedInfo(anchor, stacks);
      }
   }

   private void renderBedInfo(class_332 context, class_241 screenPos, List<class_1799> stacks) {
      if (!stacks.isEmpty()) {
         int rows = (int)Math.ceil(stacks.size() / 4.0);
         int cols = Math.min(4, stacks.size());
         int totalWidth = cols * 16 + Math.max(0, cols - 1) * 2 + 8;
         int totalHeight = rows * 16 + Math.max(0, rows - 1) * 2 + 8;
         int left = -totalWidth / 2;
         int top = -(totalHeight + 2);
         int right = left + totalWidth;
         int bottom = top + totalHeight;
         class_4587 matrices = context.method_51448();
         matrices.method_22903();
         matrices.method_46416(screenPos.field_1343, screenPos.field_1342, 0.0F);
         float scaleValue = this.scale.getValue().floatValue();
         matrices.method_22905(scaleValue, scaleValue, 1.0F);
         float alpha = this.backgroundAlpha.getValue().floatValue();
         alpha = Math.max(0.0F, Math.min(1.0F, alpha));
         int background = ((int)(alpha * 255.0F) & 0xFF) << 24;
         context.method_25294(left, top, right, bottom, background);

         for (int i = 0; i < stacks.size(); i++) {
            class_1799 stack = stacks.get(i);
            if (!stack.method_7960()) {
               int row = i / 4;
               int col = i % 4;
               int x = left + 4 + col * 18;
               int y = top + 4 + row * 18;
               class_1799 displayStack = stack.method_7972();
               displayStack.method_7939(1);
               context.method_51427(displayStack, x, y);
               if (mc.field_1772 != null) {
                  context.method_51431(mc.field_1772, displayStack, x, y);
               }
            }
         }

         matrices.method_22909();
      }
   }

   private record BedInfo(class_243 anchor, List<class_1799> blocks) {
   }
}
