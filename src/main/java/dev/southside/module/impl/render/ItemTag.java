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
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.math.MatrixStack;

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
      if (mc.world != null && mc.player != null) {
         List<ItemTag.ItemCluster> clusters = this.collectClusters(event.getRenderTick());
         if (!clusters.isEmpty()) {
            for (ItemTag.ItemCluster cluster : clusters) {
               Vec3d anchor = cluster.getCenter().add(0.0, 0.5, 0.0);
               Vec2f screenPos = ProjectionUtil.worldScreenPos(anchor);
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
      Iterator var8 = mc.world.getEntities().iterator();

      while (true) {
         ItemEntity itemEntity;
         while (true) {
            if (!var8.hasNext()) {
               return clusters;
            }

            Entity entity = (Entity)var8.next();
            if (entity instanceof ItemEntity candidate && !candidate.isRemoved() && !candidate.getStack().isEmpty()) {
               itemEntity = candidate;
               if (!this.excludeJunk.getValue()) {
                  break;
               }

               InvManagerN invManager = Southside.INSTANCE.getModuleManager().get(InvManagerN.class);
               if (invManager == null || InvManagerN.isItemUseful(itemEntity.getStack())) {
                  break;
               }
            }
         }

         Vec3d pos = itemEntity.getLerpedPos(partialTicks);
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

   private void renderTag(DrawContext context, Vec2f screenPos, ItemTag.ItemCluster cluster) {
      List<ItemTag.TagLine> lines = cluster.getLines();
      if (!lines.isEmpty()) {
         TextRenderer textRenderer = mc.textRenderer;
         int maxWidth = 0;

         for (ItemTag.TagLine line : lines) {
            maxWidth = Math.max(maxWidth, textRenderer.getWidth(line.text()));
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
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.translate(screenPos.x, screenPos.y, 0.0F);
            float scaleValue = this.scale.getValue().floatValue();
            matrices.scale(scaleValue, scaleValue, 1.0F);
            float alpha = MathHelper.clamp(this.backgroundAlpha.getValue().floatValue(), 0.0F, 1.0F);
            int background = ((int)(alpha * 255.0F) & 0xFF) << 24;
            context.fill(left, top, right, bottom, background);
            int textY = top + paddingY;

            for (ItemTag.TagLine line : lines) {
               int lineWidth = textRenderer.getWidth(line.text());
               int textX = left + (totalWidth - lineWidth) / 2;
               context.drawText(textRenderer, Text.literal(line.text()), textX, textY, line.color(), true);
               textY += lineHeight;
            }

            matrices.pop();
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

      void add(ItemEntity entity, Vec3d pos) {
         this.sumX = this.sumX + pos.x;
         this.sumY = this.sumY + pos.y;
         this.sumZ = this.sumZ + pos.z;
         this.samples++;
         ItemStack stack = entity.getStack();
         String display = stack.getName().getString();
         int count = stack.getCount();
         int color = getHighlightColor(stack);
         String key = display + "\u0000" + (color != -1 ? 1 : 0);
         ItemTag.ItemCluster.ItemEntry entry = this.items.get(key);
         if (entry == null) {
            entry = new ItemTag.ItemCluster.ItemEntry(display, color);
            this.items.put(key, entry);
         }

         entry.increment(count);
      }

      double squaredDistanceTo(Vec3d pos) {
         if (this.samples == 0) {
            return Double.MAX_VALUE;
         } else {
            double cx = this.sumX / this.samples;
            double cy = this.sumY / this.samples;
            double cz = this.sumZ / this.samples;
            double dx = cx - pos.x;
            double dy = cy - pos.y;
            double dz = cz - pos.z;
            return dx * dx + dy * dy + dz * dz;
         }
      }

      Vec3d getCenter() {
         return this.samples == 0 ? Vec3d.ZERO : new Vec3d(this.sumX / this.samples, this.sumY / this.samples, this.sumZ / this.samples);
      }

      List<ItemTag.TagLine> getLines() {
         List<ItemTag.TagLine> lines = new ArrayList<>();

         for (ItemTag.ItemCluster.ItemEntry entry : this.items.values()) {
            lines.add(new ItemTag.TagLine(entry.count + "x " + entry.display, entry.color));
         }

         return lines;
      }

      private static int getHighlightColor(ItemStack stack) {
         if (stack != null && !stack.isEmpty()) {
            if (stack.getItem() == Items.DIAMOND) {
               return -10308865;
            } else if (stack.getItem() == Items.EMERALD || stack.getItem() == Items.SLIME_BALL) {
               return -10099826;
            } else if (InventoryUtils.isGodAxe(stack) || stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
               return -9101;
            } else {
               return stack.getItem() == Items.END_CRYSTAL ? -4027393 : -1;
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
