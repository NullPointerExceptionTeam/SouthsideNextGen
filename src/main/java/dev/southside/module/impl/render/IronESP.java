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
import net.minecraft.util.math.ChunkPos;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class IronESP extends Module {
   private final Slider range = new Slider("Range", 32, 16, 64, 1);
   private final Switch boxLines = new Switch("Box Lines", true);
   private final Switch boxSides = new Switch("Box Sides", true);
   private final Switch tag = new Switch("Tag", true);
   private final Color lineColor = new Color("Line Color", new OneColor(255, 255, 255, 200));
   private final Color sideColor = new Color("Side Color", new OneColor(255, 255, 255, 50));
   private static final Set<Block> TARGET_BLOCKS = Set.of(Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.RAW_IRON_BLOCK, Blocks.IRON_BLOCK);
   private final List<BlockPos> foundPositions = new ArrayList<>();
   private final List<String> foundNames = new ArrayList<>();

   public IronESP() {
      super(Category.Render, "IronESP", "铁矿石透视");
   }

   @EventHandler
   public void onRender3D(Render3DEvent event) {
      if (mc.world != null && mc.player != null) {
         this.foundPositions.clear();
         this.foundNames.clear();
         int rangeVal = this.range.getValue().intValue();
         int rangeSq = rangeVal * rangeVal;
         Vec3d playerPos = mc.player.getPos();
         ChunkPos playerChunk = mc.player.getChunkPos();
         int chunkRange = (rangeVal >> 4) + 1;

         for (int cx = playerChunk.x - chunkRange; cx <= playerChunk.x + chunkRange; cx++) {
            for (int cz = playerChunk.z - chunkRange; cz <= playerChunk.z + chunkRange; cz++) {
               if (mc.world.getChunkManager().isChunkLoaded(cx, cz)) {
                  Chunk chunk = mc.world.getChunk(cx, cz);
                  ChunkSection[] sections = chunk.getSectionArray();

                  for (int sectionIdx = 0; sectionIdx < sections.length; sectionIdx++) {
                     ChunkSection section = sections[sectionIdx];
                     if (!section.isEmpty()) {
                        int sectionY = chunk.getBottomY() + (sectionIdx << 4);

                        for (int x = 0; x < 16; x++) {
                           for (int y = 0; y < 16; y++) {
                              for (int z = 0; z < 16; z++) {
                                 BlockState state = section.getBlockState(x, y, z);
                                 Block block = state.getBlock();
                                 if (TARGET_BLOCKS.contains(block)) {
                                    int worldX = (cx << 4) + x;
                                    int worldY = sectionY + y;
                                    int worldZ = (cz << 4) + z;
                                    double dx = worldX + 0.5 - playerPos.x;
                                    double dy = worldY + 0.5 - playerPos.y;
                                    double dz = worldZ + 0.5 - playerPos.z;
                                    if (!(dx * dx + dy * dy + dz * dz > rangeSq)) {
                                       BlockPos pos = new BlockPos(worldX, worldY, worldZ);
                                       Box box = new Box(pos);
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
      if (mc.world != null && mc.player != null && this.tag.getValue()) {
         DrawContext context = event.getDrawContext();
         TextRenderer textRenderer = mc.textRenderer;

         for (int i = 0; i < this.foundPositions.size(); i++) {
            BlockPos pos = this.foundPositions.get(i);
            String name = this.foundNames.get(i);
            Vec3d center = pos.toCenterPos().add(0.0, 0.5, 0.0);
            Vec2f screen = ProjectionUtil.worldScreenPos(center);
            if (screen != null) {
               int textWidth = textRenderer.getWidth(name);
               float scaleVal = 0.8F;
               MatrixStack matrices = context.getMatrices();
               matrices.push();
               matrices.translate(screen.x, screen.y, 0.0F);
               matrices.scale(scaleVal, scaleVal, 1.0F);
               int bg = 1711276032;
               int padding = 3;
               int left = -textWidth / 2 - padding;
               int top = -9 - padding;
               int right = textWidth / 2 + padding;
               context.fill(left, top, right, padding, bg);
               context.drawText(textRenderer, Text.literal(name), -textWidth / 2, top + padding, -1, true);
               matrices.pop();
            }
         }
      }
   }

   private String getBlockDisplayName(Block block) {
      if (block == Blocks.IRON_ORE) {
         return "铁矿";
      } else if (block == Blocks.DEEPSLATE_IRON_ORE) {
         return "深层铁矿";
      } else if (block == Blocks.RAW_IRON_BLOCK) {
         return "粗铁块";
      } else {
         return block == Blocks.IRON_BLOCK ? "铁块" : "铁矿";
      }
   }
}
