package dev.southside.module.impl.movement;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.RotationEvent;
import dev.southside.event.events.WorldEvent;
import dev.southside.manager.ServerPacketManager;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.ChatUtils;
import dev.southside.utils.network.PacketUtils;
import dev.southside.utils.world.BlockEntityIterator;
import java.util.concurrent.LinkedBlockingQueue;
import net.minecraft.block.BedBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.block.enums.BedPart;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;

public class TP2Bed extends Module {
   private final Slider searchRadius = new Slider("Search Radius", 64, 16, 128, 1);
   private final Switch autoDisable = new Switch("Auto Disable", true);
   private final Switch debug = new Switch("Debug", false);
   private final LinkedBlockingQueue<Packet<?>> packetQueue = new LinkedBlockingQueue<>();
   private boolean inSpectator = false;
   private boolean teleported = false;
   private int graceTicks = 0;
   private double tpX;
   private double tpY;
   private double tpZ;

   public TP2Bed() {
      super(Category.Movement, "TP2Bed", "旁观结束后TP到最近的床");
   }

   @Override
   public void onEnable() {
      this.resetState();
      if (mc.player != null && mc.player.getAbilities().flying) {
         this.inSpectator = true;
      }

      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.releasePackets();
      this.resetState();
   }

   @EventHandler
   public void onWorld(WorldEvent event) {
      this.packetQueue.clear();
      this.resetState();
   }

   private void resetState() {
      this.inSpectator = false;
      this.teleported = false;
      this.graceTicks = 0;
   }

   @EventHandler
   public void onTick(RotationEvent event) {
      if (mc.player != null) {
         if (this.graceTicks > 0) {
            this.graceTicks--;
            mc.player.setPosition(this.tpX, this.tpY, this.tpZ);
            mc.player.setVelocity(0.0, 0.0, 0.0);
            if (this.graceTicks == 0) {
               this.inSpectator = false;
               this.teleported = false;
               if (this.debug.getValue()) {
                  ChatUtils.info("§7[TP2Bed] Grace结束");
               }

               if (this.autoDisable.getValue()) {
                  mc.execute(() -> this.setEnable(false));
               }
            }
         }
      }
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (mc.player != null && mc.world != null) {
         if (event.getOrigin() == TransferOrigin.RECEIVE) {
            if (mc.player.age >= 20) {
               Packet<?> packet = event.getPacket();
               if (packet instanceof PlayerPositionLookS2CPacket pos) {
                  if (this.teleported && this.graceTicks > 0) {
                     event.setCancelled(true);
                     PacketUtils.sendPacketNoEvent(new TeleportConfirmC2SPacket(pos.teleportId()));
                     PacketUtils.sendPacketNoEvent(new PositionAndOnGround(this.tpX, this.tpY, this.tpZ, true, false));
                     mc.player.setPosition(this.tpX, this.tpY, this.tpZ);
                     mc.player.setVelocity(0.0, 0.0, 0.0);
                     if (this.debug.getValue()) {
                        ChatUtils.info("§7[TP2Bed] Grace中拦截Position包");
                     }

                     return;
                  }

                  if (this.inSpectator && !this.teleported) {
                     event.setCancelled(true);
                     PacketUtils.sendPacketNoEvent(new TeleportConfirmC2SPacket(pos.teleportId()));
                     this.doTeleport();
                     return;
                  }
               }

               if (!event.isCancelled()) {
                  if (packet instanceof GameStateChangeS2CPacket gs && gs.getReason() == GameStateChangeS2CPacket.GAME_MODE_CHANGED) {
                     int mode = (int)gs.getValue();
                     if (mode == 3) {
                        this.inSpectator = true;
                        this.teleported = false;
                        this.graceTicks = 0;
                        if (this.debug.getValue()) {
                           ChatUtils.info("§7[TP2Bed] 进入旁观");
                        }
                     } else if (this.inSpectator && !this.teleported) {
                        if (this.debug.getValue()) {
                           ChatUtils.info("§a[TP2Bed] 退出旁观，立即传送");
                        }

                        this.doTeleport();
                     }
                  }

                  if (packet instanceof PlayerAbilitiesS2CPacket abilities && abilities.isFlying() && !this.inSpectator) {
                     this.inSpectator = true;
                     this.teleported = false;
                     this.graceTicks = 0;
                     if (this.debug.getValue()) {
                        ChatUtils.info("§7[TP2Bed] 进入旁观 (Abilities)");
                        mc.player.setPosition(this.tpX, this.tpY, this.tpZ);
                        mc.player.setVelocity(0.0, 0.0, 0.0);
                     }
                  }
               }
            }
         }
      }
   }

   private void doTeleport() {
      if (!this.teleported) {
         BlockPos bed = this.findNearestBed();
         if (bed == null) {
            ChatUtils.warning("§c[TP2Bed] 未找到床");
            this.inSpectator = false;
         } else {
            this.tpX = bed.getX() + 0.5;
            this.tpY = bed.getY() + 1.0;
            this.tpZ = bed.getZ() + 0.5;
            PacketUtils.sendPacketNoEvent(new PositionAndOnGround(this.tpX, this.tpY, this.tpZ, true, false));
            mc.player.setPosition(this.tpX, this.tpY, this.tpZ);
            mc.player.setVelocity(0.0, 0.0, 0.0);
            this.teleported = true;
            this.graceTicks = 5;
            ChatUtils.info("§a[TP2Bed] 已传送到床 (" + bed.getX() + ", " + (bed.getY() + 1) + ", " + bed.getZ() + ")");
         }
      }
   }

   private BlockPos findNearestBed() {
      if (mc.player != null && mc.world != null) {
         Vec3d pos = mc.player.getPos();
         double maxSq = Math.pow(this.searchRadius.getValue().doubleValue(), 2.0);
         BlockPos nearest = null;
         double nearestSq = Double.MAX_VALUE;
         BlockEntityIterator it = new BlockEntityIterator();

         while (it.hasNext()) {
            BlockEntity be = it.next();
            if (be instanceof BedBlockEntity) {
               BlockState state = mc.world.getBlockState(be.getPos());
               if (state.getBlock() instanceof BedBlock) {
                  BlockPos head = this.getHeadPos(be.getPos(), state);
                  if (head != null) {
                     double dSq = pos.squaredDistanceTo(Vec3d.ofCenter(head));
                     if (dSq < maxSq && dSq < nearestSq) {
                        nearestSq = dSq;
                        nearest = head;
                     }
                  }
               }
            }
         }

         return nearest;
      } else {
         return null;
      }
   }

   private BlockPos getHeadPos(BlockPos pos, BlockState state) {
      Direction facing = (Direction)state.get(BedBlock.FACING);
      BedPart part = (BedPart)state.get(BedBlock.PART);
      return part == BedPart.HEAD ? pos.toImmutable() : pos.offset(facing).toImmutable();
   }

   private void releasePackets() {
      while (!this.packetQueue.isEmpty()) {
         Packet<?> p = this.packetQueue.poll();
         if (p != null && !ServerPacketManager.onPacket(p)) {
            PacketUtils.handlePacket(p);
         }
      }
   }
}
