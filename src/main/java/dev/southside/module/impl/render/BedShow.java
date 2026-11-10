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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.block.BedBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

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
      if (mc.world != null && mc.player != null) {
         List<BedShow.BedInfo> beds = this.collectBeds();
         if (!beds.isEmpty()) {
            DrawContext context = event.getDrawContext();

            for (BedShow.BedInfo bed : beds) {
               Vec2f screenPos = ProjectionUtil.worldScreenPos(bed.anchor());
               if (screenPos != null) {
                  this.renderBedInfo(context, screenPos, bed.blocks());
               }
            }
         }
      }
   }

   private List<BedShow.BedInfo> collectBeds() {
      List<BedShow.BedInfo> beds = new ArrayList<>();
      PlayerEntity player = mc.player;
      if (player != null && mc.world != null) {
         double radius = this.searchRadius.getValue().doubleValue() + 3.0;
         double radiusSq = radius * radius;
         BlockEntityIterator iterator = new BlockEntityIterator();

         while (iterator.hasNext()) {
            if (iterator.next() instanceof BedBlockEntity bedEntity) {
               BlockPos pos = bedEntity.getPos();
               if (!(player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > radiusSq)) {
                  BlockState state = mc.world.getBlockState(pos);
                  if (state.getBlock() instanceof BedBlock bedBlock && state.get(BedBlock.PART) == BedPart.HEAD) {
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

   private BedShow.BedInfo buildInfo(BlockPos headPos, BlockState headState) {
      if (mc.world == null) {
         return new BedShow.BedInfo(Vec3d.ofCenter(headPos), List.of());
      } else {
         Direction facing = (Direction)headState.get(BedBlock.FACING);
         BlockPos footPos = headPos.offset(facing.getOpposite());
         Set<BlockPos> bedBlocks = new LinkedHashSet<>();
         bedBlocks.add(headPos.toImmutable());
         bedBlocks.add(footPos.toImmutable());
         LinkedHashSet<BlockPos> surrounding = new LinkedHashSet<>();

         for (BlockPos base : bedBlocks) {
            BlockPos above = base.up();
            if (!bedBlocks.contains(above)) {
               surrounding.add(above.toImmutable());
            }

            for (int dx = -3; dx <= 3; dx++) {
               for (int dz = -3; dz <= 3; dz++) {
                  if ((dx != 0 || dz != 0) && Math.max(Math.abs(dx), Math.abs(dz)) <= 3) {
                     BlockPos target = base.add(dx, 0, dz);
                     if (!bedBlocks.contains(target)) {
                        surrounding.add(target.toImmutable());
                     }
                  }
               }
            }
         }

         List<ItemStack> stacks = new ArrayList<>();
         Set<Item> seenItems = new LinkedHashSet<>();

         for (BlockPos pos : surrounding) {
            BlockState state = mc.world.getBlockState(pos);
            if (!state.isAir() && SlotUtils.isGoodForBridging(state.getBlock())) {
               Item item = state.getBlock().asItem();
               if (item != Items.AIR && seenItems.add(item)) {
                  stacks.add(new ItemStack(item));
                  if (stacks.size() >= 12) {
                     break;
                  }
               }
            }
         }

         Vec3d anchor = Vec3d.ofCenter(headPos).add(0.0, 0.75, 0.0);
         return new BedShow.BedInfo(anchor, stacks);
      }
   }

   private void renderBedInfo(DrawContext context, Vec2f screenPos, List<ItemStack> stacks) {
      if (!stacks.isEmpty()) {
         int rows = (int)Math.ceil(stacks.size() / 4.0);
         int cols = Math.min(4, stacks.size());
         int totalWidth = cols * 16 + Math.max(0, cols - 1) * 2 + 8;
         int totalHeight = rows * 16 + Math.max(0, rows - 1) * 2 + 8;
         int left = -totalWidth / 2;
         int top = -(totalHeight + 2);
         int right = left + totalWidth;
         int bottom = top + totalHeight;
         MatrixStack matrices = context.getMatrices();
         matrices.push();
         matrices.translate(screenPos.x, screenPos.y, 0.0F);
         float scaleValue = this.scale.getValue().floatValue();
         matrices.scale(scaleValue, scaleValue, 1.0F);
         float alpha = this.backgroundAlpha.getValue().floatValue();
         alpha = Math.max(0.0F, Math.min(1.0F, alpha));
         int background = ((int)(alpha * 255.0F) & 0xFF) << 24;
         context.fill(left, top, right, bottom, background);

         for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            if (!stack.isEmpty()) {
               int row = i / 4;
               int col = i % 4;
               int x = left + 4 + col * 18;
               int y = top + 4 + row * 18;
               ItemStack displayStack = stack.copy();
               displayStack.setCount(1);
               context.drawItem(displayStack, x, y);
               if (mc.textRenderer != null) {
                  context.drawStackOverlay(mc.textRenderer, displayStack, x, y);
               }
            }
         }

         matrices.pop();
      }
   }

   private record BedInfo(Vec3d anchor, List<ItemStack> blocks) {
   }
}
