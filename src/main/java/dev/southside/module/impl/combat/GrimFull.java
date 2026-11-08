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
import net.minecraft.class_1268;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2626;
import net.minecraft.class_2743;
import net.minecraft.class_3965;

public class GrimFull extends Module {
   private boolean gotKB = false;
   private boolean sendPacket = false;
   private boolean release = false;

   public GrimFull() {
      super(Category.Combat, "GrimFull");
   }

   @EventHandler
   public void onPacketReceive(PacketEvent event) {
      class_2596 packet = event.getPacket();
      if (!event.isCancelled() && packet instanceof class_2743 wrapper && wrapper.method_11818() == mc.field_1724.method_5628() && this.shouldVel(wrapper)) {
         class_3965 result = ClientRayTraceUtil.getFacedBlock(mc.field_1724.method_36454(), 90.0F);
         if (this.checkCollision(result.method_17777())) {
            this.gotKB = true;
            ChatUtils.info("kb");
         }
      }

      if (packet instanceof class_2626 blockUpdateS2CPacket
         && blockUpdateS2CPacket.method_11309() != null
         && mc.field_1724.method_24828()
         && this.checkCollision(blockUpdateS2CPacket.method_11309())) {
         ChatUtils.info("received");
         this.release = true;
         this.gotKB = false;
      }
   }

   @EventHandler
   public void onRotation(RotationEvent event) {
      if (this.release) {
         ServerPacketManager.filteredRelease(packet -> packet instanceof class_2743, false);
      }

      if (this.gotKB) {
         if (ServerPacketManager.deSyncTick > 20) {
            ChatUtils.error("time out");
            ServerPacketManager.reset(false);
            this.gotKB = false;
         }

         class_3965 result = ClientRayTraceUtil.getFacedBlock(mc.field_1724.method_36454(), 90.0F);
         if (result != null && this.checkCollision(result.method_17777()) && mc.field_1724.method_24828()) {
            if (!this.sendPacket) {
               ChatUtils.info("found");
               this.sendPacket = true;
               RotationUtils.setRotation(new Rotation(mc.field_1724.method_36454(), 90.0F), Integer.MAX_VALUE);
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

   private boolean checkCollision(class_2338 pos) {
      if (pos == null) {
         return false;
      } else {
         class_238 aabb = new class_238(pos.method_10084());
         return aabb.method_994(mc.field_1724.method_5829());
      }
   }

   @EventHandler
   public void onApplied(RotationAppliedEvent event) {
      if (this.gotKB && this.sendPacket) {
         ChatUtils.info("send");
         this.sendPacket = false;
         class_3965 result = ClientRayTraceUtil.getFacedBlock(mc.field_1724.method_36454(), 90.0F);
         mc.field_1761.method_2896(mc.field_1724, class_1268.field_5808, result);
      }
   }

   @EventHandler
   public void onMotion(MotionEvent event) {
      if (this.gotKB) {
      }
   }

   private boolean shouldVel(class_2743 packet) {
      return !AutoGApple.eating && new class_243(packet.method_11815(), 0.0, packet.method_11819()).method_1033() >= 0.15;
   }
}
