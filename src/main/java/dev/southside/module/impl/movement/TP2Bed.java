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
import net.minecraft.class_2244;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2586;
import net.minecraft.class_2587;
import net.minecraft.class_2596;
import net.minecraft.class_2668;
import net.minecraft.class_2680;
import net.minecraft.class_2696;
import net.minecraft.class_2708;
import net.minecraft.class_2742;
import net.minecraft.class_2793;
import net.minecraft.class_2828.class_2829;

public class TP2Bed extends Module {
   private final Slider searchRadius = new Slider("Search Radius", 64, 16, 128, 1);
   private final Switch autoDisable = new Switch("Auto Disable", true);
   private final Switch debug = new Switch("Debug", false);
   private final LinkedBlockingQueue<class_2596<?>> packetQueue = new LinkedBlockingQueue<>();
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
      if (mc.field_1724 != null && mc.field_1724.method_31549().field_7479) {
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
      if (mc.field_1724 != null) {
         if (this.graceTicks > 0) {
            this.graceTicks--;
            mc.field_1724.method_5814(this.tpX, this.tpY, this.tpZ);
            mc.field_1724.method_18800(0.0, 0.0, 0.0);
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
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (event.getOrigin() == TransferOrigin.RECEIVE) {
            if (mc.field_1724.field_6012 >= 20) {
               class_2596<?> packet = event.getPacket();
               if (packet instanceof class_2708 pos) {
                  if (this.teleported && this.graceTicks > 0) {
                     event.setCancelled(true);
                     PacketUtils.sendPacketNoEvent(new class_2793(pos.comp_3133()));
                     PacketUtils.sendPacketNoEvent(new class_2829(this.tpX, this.tpY, this.tpZ, true, false));
                     mc.field_1724.method_5814(this.tpX, this.tpY, this.tpZ);
                     mc.field_1724.method_18800(0.0, 0.0, 0.0);
                     if (this.debug.getValue()) {
                        ChatUtils.info("§7[TP2Bed] Grace中拦截Position包");
                     }

                     return;
                  }

                  if (this.inSpectator && !this.teleported) {
                     event.setCancelled(true);
                     PacketUtils.sendPacketNoEvent(new class_2793(pos.comp_3133()));
                     this.doTeleport();
                     return;
                  }
               }

               if (!event.isCancelled()) {
                  if (packet instanceof class_2668 gs && gs.method_11491() == class_2668.field_25648) {
                     int mode = (int)gs.method_11492();
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

                  if (packet instanceof class_2696 abilities && abilities.method_11698() && !this.inSpectator) {
                     this.inSpectator = true;
                     this.teleported = false;
                     this.graceTicks = 0;
                     if (this.debug.getValue()) {
                        ChatUtils.info("§7[TP2Bed] 进入旁观 (Abilities)");
                        mc.field_1724.method_5814(this.tpX, this.tpY, this.tpZ);
                        mc.field_1724.method_18800(0.0, 0.0, 0.0);
                     }
                  }
               }
            }
         }
      }
   }

   private void doTeleport() {
      if (!this.teleported) {
         class_2338 bed = this.findNearestBed();
         if (bed == null) {
            ChatUtils.warning("§c[TP2Bed] 未找到床");
            this.inSpectator = false;
         } else {
            this.tpX = bed.method_10263() + 0.5;
            this.tpY = bed.method_10264() + 1.0;
            this.tpZ = bed.method_10260() + 0.5;
            PacketUtils.sendPacketNoEvent(new class_2829(this.tpX, this.tpY, this.tpZ, true, false));
            mc.field_1724.method_5814(this.tpX, this.tpY, this.tpZ);
            mc.field_1724.method_18800(0.0, 0.0, 0.0);
            this.teleported = true;
            this.graceTicks = 5;
            ChatUtils.info("§a[TP2Bed] 已传送到床 (" + bed.method_10263() + ", " + (bed.method_10264() + 1) + ", " + bed.method_10260() + ")");
         }
      }
   }

   private class_2338 findNearestBed() {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         class_243 pos = mc.field_1724.method_19538();
         double maxSq = Math.pow(this.searchRadius.getValue().doubleValue(), 2.0);
         class_2338 nearest = null;
         double nearestSq = Double.MAX_VALUE;
         BlockEntityIterator it = new BlockEntityIterator();

         while (it.hasNext()) {
            class_2586 be = it.next();
            if (be instanceof class_2587) {
               class_2680 state = mc.field_1687.method_8320(be.method_11016());
               if (state.method_26204() instanceof class_2244) {
                  class_2338 head = this.getHeadPos(be.method_11016(), state);
                  if (head != null) {
                     double dSq = pos.method_1025(class_243.method_24953(head));
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

   private class_2338 getHeadPos(class_2338 pos, class_2680 state) {
      class_2350 facing = (class_2350)state.method_11654(class_2244.field_11177);
      class_2742 part = (class_2742)state.method_11654(class_2244.field_9967);
      return part == class_2742.field_12560 ? pos.method_10062() : pos.method_10093(facing).method_10062();
   }

   private void releasePackets() {
      while (!this.packetQueue.isEmpty()) {
         class_2596<?> p = this.packetQueue.poll();
         if (p != null && !ServerPacketManager.onPacket(p)) {
            PacketUtils.handlePacket(p);
         }
      }
   }
}
