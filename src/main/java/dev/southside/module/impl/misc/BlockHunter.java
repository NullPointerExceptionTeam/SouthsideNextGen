package dev.southside.module.impl.misc;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.options.impl.Color;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.Render3DEvent;
import dev.southside.event.events.WorldEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.render.Render3D;
import dev.southside.utils.ChatUtils;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.util.math.BlockPos;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.block.BlockState;

public class BlockHunter extends Module {
   private final Set<BlockPos> markedBlocks = new HashSet<>();
   private final Color sideColor = new Color("Side Color", true, new OneColor(255, 120, 40, 60));
   private final Color lineColor = new Color("Line Color", true, new OneColor(255, 120, 40, 160));

   public BlockHunter() {
      super(Category.Misc, "BlockHunter");
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (event.getOrigin() == TransferOrigin.RECEIVE && mc.world != null) {
         if (event.getPacket() instanceof BlockUpdateS2CPacket packet) {
            this.markIfAir(packet.getPos(), packet.getState());
         } else if (event.getPacket() instanceof ChunkDeltaUpdateS2CPacket packet) {
            packet.visitUpdates(this::markIfAir);
         }
      }
   }

   @EventHandler
   public void onWorldChange(WorldEvent event) {
      this.markedBlocks.clear();
   }

   @EventHandler
   public void onRender(Render3DEvent event) {
      if (mc.world != null && !this.markedBlocks.isEmpty()) {
         for (BlockPos pos : this.markedBlocks) {
            Render3D.blockSides(pos, this.sideColor.getValue().toJavaColor());
            Render3D.blockLines(pos, this.lineColor.getValue().toJavaColor());
         }
      }
   }

   @Override
   public void onDisable() {
      this.markedBlocks.clear();
      super.onDisable();
   }

   private void markIfAir(BlockPos pos, BlockState newState) {
      if (pos != null && newState != null && mc.world != null) {
         if (newState.isAir()) {
            this.markedBlocks.remove(pos);
         } else {
            if (!newState.isAir() && mc.world.getBlockState(pos).isAir()) {
               BlockPos immutablePos = pos.toImmutable();
               if (this.markedBlocks.add(immutablePos)) {
                  ChatUtils.info("Detected a hider! " + this.formatPos(immutablePos));
               }
            }
         }
      }
   }

   private String formatPos(BlockPos pos) {
      return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
   }
}
