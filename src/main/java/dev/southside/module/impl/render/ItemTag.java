package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.Southside;
import dev.southside.event.events.Render2DEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.player.InvManagerN;
import dev.southside.orbit.EventHandler;
import dev.southside.render.ProjectionUtil;
import dev.southside.utils.player.InventoryUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1297;
import net.minecraft.class_1542;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4587;

public class ItemTag extends Module {
   private final Slider scale = new Slider("Scale", 0.8F, 0.2F, 3.0F, 0.1F);
   private final Slider mergeRadius = new Slider("Merge Radius", 1.0F, 0.0F, 16.0F, 0.5F);
   private final Slider backgroundAlpha = new Slider("Background Alpha", 0.15F, 0.05F, 1.0F, 0.05F);
   private final Switch excludeJunk = new Switch("Ignore Trash", false);

   public ItemTag() {
      super(Category.Render, "ItemTag", "Display dropped item name tags");
   }

   @EventHandler
   public void onRender2D(Render2DEvent event) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         List<ItemTag.ItemCluster> clusters = this.collectClusters(event.getRenderTick());
         if (!clusters.isEmpty()) {
            for (ItemTag.ItemCluster cluster : clusters) {
               class_243 anchor = cluster.getCenter().method_1031(0.0, 0.5, 0.0);
               class_241 screenPos = ProjectionUtil.worldScreenPos(anchor);
               if (screenPos != null) {
                  this.renderTag(event.getDrawContext(), screenPos, cluster);
               }
            }
         }
      }
   }

   private List<ItemTag.ItemCluster> collectClusters(float partialTicks) {
      List<ItemTag.ItemCluster> clusters = new ArrayList<>();
      double radius = this.mergeRadius.getValue().doubleValue();
      double mergeDistanceSq = radius * radius;
      boolean allowMerge = radius > 0.0;
      Iterator var8 = mc.field_1687.method_18112().iterator();

      while (true) {
         class_1542 itemEntity;
         while (true) {
            if (!var8.hasNext()) {
               return clusters;
            }

            class_1297 entity = (class_1297)var8.next();
            if (entity instanceof class_1542 candidate && !candidate.method_31481() && !candidate.method_6983().method_7960()) {
               itemEntity = candidate;
               if (!this.excludeJunk.getValue()) {
                  break;
               }

               InvManagerN invManager = Southside.INSTANCE.getModuleManager().get(InvManagerN.class);
               if (invManager == null || InvManagerN.isItemUseful(itemEntity.method_6983())) {
                  break;
               }
            }
         }

         class_243 pos = itemEntity.method_30950(partialTicks);
         ItemTag.ItemCluster target = null;
         if (allowMerge) {
            for (ItemTag.ItemCluster cluster : clusters) {
               if (cluster.squaredDistanceTo(pos) <= mergeDistanceSq) {
                  target = cluster;
                  break;
               }
            }
         }

         if (target == null) {
            target = new ItemTag.ItemCluster();
            clusters.add(target);
         }

         target.add(itemEntity, pos);
      }
   }

   private void renderTag(class_332 context, class_241 screenPos, ItemTag.ItemCluster cluster) {
      List<ItemTag.TagLine> lines = cluster.getLines();
      if (!lines.isEmpty()) {
         class_327 textRenderer = mc.field_1772;
         int maxWidth = 0;

         for (ItemTag.TagLine line : lines) {
            maxWidth = Math.max(maxWidth, textRenderer.method_1727(line.text()));
         }

         if (maxWidth > 0) {
            int fontHeight = 9;
            int lineHeight = fontHeight + 2;
            int contentHeight = lineHeight * lines.size();
            int paddingX = 4;
            int paddingY = 2;
            int totalWidth = maxWidth + paddingX * 2;
            int totalHeight = contentHeight + paddingY * 2;
            int left = -totalWidth / 2;
            int top = -(totalHeight + 2);
            int right = left + totalWidth;
            int bottom = top + totalHeight;
            class_4587 matrices = context.method_51448();
            matrices.method_22903();
            matrices.method_46416(screenPos.field_1343, screenPos.field_1342, 0.0F);
            float scaleValue = this.scale.getValue().floatValue();
            matrices.method_22905(scaleValue, scaleValue, 1.0F);
            float alpha = class_3532.method_15363(this.backgroundAlpha.getValue().floatValue(), 0.0F, 1.0F);
            int background = ((int)(alpha * 255.0F) & 0xFF) << 24;
            context.method_25294(left, top, right, bottom, background);
            int textY = top + paddingY;

            for (ItemTag.TagLine line : lines) {
               int lineWidth = textRenderer.method_1727(line.text());
               int textX = left + (totalWidth - lineWidth) / 2;
               context.method_51439(textRenderer, class_2561.method_43470(line.text()), textX, textY, line.color(), true);
               textY += lineHeight;
            }

            matrices.method_22909();
         }
      }
   }

   private static class ItemCluster {
      private static final int DEFAULT_COLOR = -1;
      private static final int BLUE_COLOR = -10308865;
      private static final int GREEN_COLOR = -10099826;
      private static final int GOLD_COLOR = -9101;
      private static final int PURPLE_COLOR = -4027393;
      private double sumX;
      private double sumY;
      private double sumZ;
      private int samples;
      private final Map<String, ItemTag.ItemCluster.ItemEntry> items = new LinkedHashMap<>();

      void add(class_1542 entity, class_243 pos) {
         this.sumX = this.sumX + pos.field_1352;
         this.sumY = this.sumY + pos.field_1351;
         this.sumZ = this.sumZ + pos.field_1350;
         this.samples++;
         class_1799 stack = entity.method_6983();
         String display = stack.method_7964().getString();
         int count = stack.method_7947();
         int color = getHighlightColor(stack);
         String key = display + "\u0000" + (color != -1 ? 1 : 0);
         ItemTag.ItemCluster.ItemEntry entry = this.items.get(key);
         if (entry == null) {
            entry = new ItemTag.ItemCluster.ItemEntry(display, color);
            this.items.put(key, entry);
         }

         entry.increment(count);
      }

      double squaredDistanceTo(class_243 pos) {
         if (this.samples == 0) {
            return Double.MAX_VALUE;
         } else {
            double cx = this.sumX / this.samples;
            double cy = this.sumY / this.samples;
            double cz = this.sumZ / this.samples;
            double dx = cx - pos.field_1352;
            double dy = cy - pos.field_1351;
            double dz = cz - pos.field_1350;
            return dx * dx + dy * dy + dz * dz;
         }
      }

      class_243 getCenter() {
         return this.samples == 0 ? class_243.field_1353 : new class_243(this.sumX / this.samples, this.sumY / this.samples, this.sumZ / this.samples);
      }

      List<ItemTag.TagLine> getLines() {
         List<ItemTag.TagLine> lines = new ArrayList<>();

         for (ItemTag.ItemCluster.ItemEntry entry : this.items.values()) {
            lines.add(new ItemTag.TagLine(entry.count + "x " + entry.display, entry.color));
         }

         return lines;
      }

      private static int getHighlightColor(class_1799 stack) {
         if (stack != null && !stack.method_7960()) {
            if (stack.method_7909() == class_1802.field_8477) {
               return -10308865;
            } else if (stack.method_7909() == class_1802.field_8687 || stack.method_7909() == class_1802.field_8777) {
               return -10099826;
            } else if (InventoryUtils.isGodAxe(stack) || stack.method_7909() == class_1802.field_8367) {
               return -9101;
            } else {
               return stack.method_7909() == class_1802.field_8301 ? -4027393 : -1;
            }
         } else {
            return -1;
         }
      }

      private static class ItemEntry {
         private final String display;
         private int count;
         private int color;

         ItemEntry(String display, int color) {
            this.display = display;
            this.color = color;
         }

         void increment(int value) {
            this.count += value;
         }

         void applyColor(int newColor) {
            if (this.color == -1 && newColor != -1) {
               this.color = newColor;
            }
         }
      }
   }

   private record TagLine(String text, int color) {
   }
}
