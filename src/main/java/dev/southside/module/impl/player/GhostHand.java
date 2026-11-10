package dev.southside.module.impl.player;

import dev.southside.event.events.ItemUseEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.misc.ClientUtils;
import dev.southside.utils.network.PacketUtils;
import dev.southside.utils.raytrace.ClientRayTraceUtil;
import net.minecraft.util.Hand;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;

public class GhostHand extends Module {
   private static final double MAX_REACH_DISTANCE = 4.5;

   public GhostHand() {
      super(Category.Player, "GhostHand");
   }

   @EventHandler
   public void onItemUse(ItemUseEvent event) {
      if (!ClientUtils.nullCheck()) {
         if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == Type.BLOCK) {
            BlockHitResult hitResult = (BlockHitResult)mc.crosshairTarget;
            if (this.isChestBlock(mc.world.getBlockState(hitResult.getBlockPos()))) {
               return;
            }
         }

         BlockHitResult hitResult = this.raycastForChest();
         if (hitResult != null) {
            PacketUtils.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hitResult, id));
            event.setCancelled(true);
         }
      }
   }

   private BlockHitResult raycastForChest() {
      float tickDelta = mc.getRenderTickCounter().getTickDelta(false);
      Vec3d start = mc.player.getCameraPosVec(tickDelta);
      Vec3d direction = mc.player.getRotationVec(tickDelta);
      Vec3d end = start.add(direction.multiply(4.5));
      double stepSize = 0.1;
      int steps = (int)(4.5 / stepSize);
      Vec3d step = direction.multiply(stepSize);
      Vec3d current = start;

      for (int i = 0; i <= steps; i++) {
         BlockPos pos = new BlockPos((int)Math.floor(current.x), (int)Math.floor(current.y), (int)Math.floor(current.z));
         BlockState state = mc.world.getBlockState(pos);
         if (this.isChestBlock(state) && this.hasChestCollision(pos, current)) {
            return ClientRayTraceUtil.getFacedContainerBlock(mc.player.getYaw(), mc.player.getPitch());
         }

         current = current.add(step);
      }

      return null;
   }

   private boolean isChestBlock(BlockState state) {
      return state.getBlock() instanceof ChestBlock || state.getBlock() instanceof EnderChestBlock || state.getBlock() instanceof ShulkerBoxBlock;
   }

   private boolean hasChestCollision(BlockPos pos, Vec3d rayPos) {
      try {
         double minX = pos.getX() + 0.1;
         double minY = pos.getY() + 0.1;
         double minZ = pos.getZ() + 0.1;
         double maxX = pos.getX() + 0.9;
         double maxY = pos.getY() + 0.9;
         double maxZ = pos.getZ() + 0.9;
         return rayPos.x >= minX
            && rayPos.x <= maxX
            && rayPos.y >= minY
            && rayPos.y <= maxY
            && rayPos.z >= minZ
            && rayPos.z <= maxZ;
      } catch (Exception var15) {
         return false;
      }
   }
}
