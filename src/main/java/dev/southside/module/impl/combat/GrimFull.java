package dev.southside.module.impl.combat;

import dev.southside.event.events.MotionEvent;
import dev.southside.event.events.MovementInputEvent;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.RotationAppliedEvent;
import dev.southside.event.events.RotationEvent;
import dev.southside.manager.ServerPacketManager;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.misc.AutoGApple;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.ChatUtils;
import dev.southside.utils.player.MovementUtils;
import dev.southside.utils.raytrace.ClientRayTraceUtil;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.hit.BlockHitResult;

public class GrimFull extends Module {
   private boolean gotKB = false;
   private boolean sendPacket = false;
   private boolean release = false;

   public GrimFull() {
      super(Category.Combat, "GrimFull");
   }

   @EventHandler
   public void onPacketReceive(PacketEvent event) {
      Packet packet = event.getPacket();
      if (!event.isCancelled() && packet instanceof EntityVelocityUpdateS2CPacket wrapper && wrapper.getEntityId() == mc.player.getId() && this.shouldVel(wrapper)) {
         BlockHitResult result = ClientRayTraceUtil.getFacedBlock(mc.player.getYaw(), 90.0F);
         if (this.checkCollision(result.getBlockPos())) {
            this.gotKB = true;
            ChatUtils.info("kb");
         }
      }

      if (packet instanceof BlockUpdateS2CPacket blockUpdateS2CPacket
         && blockUpdateS2CPacket.getPos() != null
         && mc.player.isOnGround()
         && this.checkCollision(blockUpdateS2CPacket.getPos())) {
         ChatUtils.info("received");
         this.release = true;
         this.gotKB = false;
      }
   }

   @EventHandler
   public void onRotation(RotationEvent event) {
      if (this.release) {
         ServerPacketManager.filteredRelease(packet -> packet instanceof EntityVelocityUpdateS2CPacket, false);
      }

      if (this.gotKB) {
         if (ServerPacketManager.deSyncTick > 20) {
            ChatUtils.error("time out");
            ServerPacketManager.reset(false);
            this.gotKB = false;
         }

         BlockHitResult result = ClientRayTraceUtil.getFacedBlock(mc.player.getYaw(), 90.0F);
         if (result != null && this.checkCollision(result.getBlockPos()) && mc.player.isOnGround()) {
            if (!this.sendPacket) {
               ChatUtils.info("found");
               this.sendPacket = true;
               RotationUtils.setRotation(new Rotation(mc.player.getYaw(), 90.0F), Integer.MAX_VALUE);
            }

            MovementUtils.cancelMove();
         }
      }
   }

   @EventHandler
   public void onMoveInput(MovementInputEvent event) {
      if (this.gotKB && MovementUtils.cancelMove) {
         event.setSneak(true);
      }
   }

   private boolean checkCollision(BlockPos pos) {
      if (pos == null) {
         return false;
      } else {
         Box aabb = new Box(pos.up());
         return aabb.intersects(mc.player.getBoundingBox());
      }
   }

   @EventHandler
   public void onApplied(RotationAppliedEvent event) {
      if (this.gotKB && this.sendPacket) {
         ChatUtils.info("send");
         this.sendPacket = false;
         BlockHitResult result = ClientRayTraceUtil.getFacedBlock(mc.player.getYaw(), 90.0F);
         mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, result);
      }
   }

   @EventHandler
   public void onMotion(MotionEvent event) {
      if (this.gotKB) {
      }
   }

   private boolean shouldVel(EntityVelocityUpdateS2CPacket packet) {
      return !AutoGApple.eating && new Vec3d(packet.getVelocityX(), 0.0, packet.getVelocityZ()).length() >= 0.15;
   }
}
