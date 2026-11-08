package dev.southside.module.impl.movement;

import dev.southside.event.Stage;
import dev.southside.event.events.MotionEvent;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.SetScreenEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.network.PacketUtils;
import dev.southside.utils.render.animation.TimerUtil;
import java.util.concurrent.LinkedBlockingQueue;
import net.minecraft.class_10185;
import net.minecraft.class_2596;
import net.minecraft.class_2813;
import net.minecraft.class_2815;
import net.minecraft.class_2851;
import net.minecraft.class_304;
import net.minecraft.class_408;
import net.minecraft.class_437;
import net.minecraft.class_465;
import org.lwjgl.glfw.GLFW;

public class InventoryMove extends Module {
   private boolean wasInContainer = false;
   private final LinkedBlockingQueue<class_2596<?>> packetQueue = new LinkedBlockingQueue<>();
   private boolean invOpen = false;
   private boolean shouldSuppressInput = false;
   private boolean lastInputSuppression = false;
   private final TimerUtil timer = new TimerUtil();
   private final TimerUtil timer2 = new TimerUtil();
   private final TimerUtil timer3 = new TimerUtil();
   private boolean shouldRestoreSprint = false;
   private boolean sprintKeyPressed = false;
   public boolean isReleasing = false;
   private class_437 screen;

   public InventoryMove() {
      super(Category.Movement, "InventoryMove", "在物品栏中移动");
   }

   @Override
   public void onEnable() {
      this.resetState();
   }

   @Override
   public void onDisable() {
      this.resetState();
      this.releasePackets();
   }

   private void resetState() {
      this.wasInContainer = false;
      this.invOpen = false;
      this.shouldSuppressInput = false;
      this.shouldRestoreSprint = false;
      this.sprintKeyPressed = false;
      this.timer.reset();
      this.timer2.reset();
      this.timer3.reset();
      this.packetQueue.clear();
      this.isReleasing = false;
   }

   @EventHandler
   public void onScreen(SetScreenEvent event) {
      if (mc.field_1755 instanceof class_465 && this.invOpen && event.screen == null) {
         this.handleSprintBeforeClose();
         this.screen = null;
         event.setCancelled(true);
         this.timer3.reset();
         this.lastInputSuppression = true;
         mc.field_1690.field_1904.method_23481(false);
      }
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (mc.field_1755 instanceof class_465) {
         this.updateKeyBindings();
      }
   }

   @EventHandler(
      priority = 200
   )
   public void onMotion(MotionEvent event) {
      if (mc.field_1755 != null && !(mc.field_1755 instanceof class_408)) {
         boolean inContainer = mc.field_1755 instanceof class_465;
         if (event.getState() == Stage.PRE) {
            if (this.wasInContainer && !inContainer) {
               this.wasInContainer = false;
               this.shouldSuppressInput = false;
            }

            if (inContainer) {
               this.wasInContainer = true;
               this.invOpen = true;
               this.shouldSuppressInput = true;
            }
         } else if (event.getState() == Stage.POST && this.timer3.hasTimeElapsed(100L) && this.invOpen && this.lastInputSuppression) {
            this.timer3.reset();
            this.lastInputSuppression = false;
            this.invOpen = false;
            mc.method_1507(this.screen);
            this.sendNoMovementInput();
            this.releasePackets();
            this.resetState();
         }
      }
   }

   private void updateKeyBindings() {
      if (mc.field_1724 != null) {
         class_304.method_1416(
            mc.field_1690.field_1894.method_1429(), GLFW.glfwGetKey(mc.method_22683().method_4490(), mc.field_1690.field_1894.method_1429().method_1444()) == 1
         );
         class_304.method_1416(
            mc.field_1690.field_1881.method_1429(), GLFW.glfwGetKey(mc.method_22683().method_4490(), mc.field_1690.field_1881.method_1429().method_1444()) == 1
         );
         class_304.method_1416(
            mc.field_1690.field_1913.method_1429(), GLFW.glfwGetKey(mc.method_22683().method_4490(), mc.field_1690.field_1913.method_1429().method_1444()) == 1
         );
         class_304.method_1416(
            mc.field_1690.field_1849.method_1429(), GLFW.glfwGetKey(mc.method_22683().method_4490(), mc.field_1690.field_1849.method_1429().method_1444()) == 1
         );
         class_304.method_1416(
            mc.field_1690.field_1903.method_1429(), GLFW.glfwGetKey(mc.method_22683().method_4490(), mc.field_1690.field_1903.method_1429().method_1444()) == 1
         );
         class_304.method_1416(
            mc.field_1690.field_1867.method_1429(), GLFW.glfwGetKey(mc.method_22683().method_4490(), mc.field_1690.field_1867.method_1429().method_1444()) == 1
         );
         class_304.method_1416(
            mc.field_1690.field_1904.method_1429(), GLFW.glfwGetKey(mc.method_22683().method_4490(), mc.field_1690.field_1904.method_1429().method_1444()) == 1
         );
      }
   }

   @EventHandler
   public void onPacketSend(PacketEvent event) {
      class_2596<?> packet = event.getPacket();
      if (!this.wasInContainer || !(packet instanceof class_2813) && !(packet instanceof class_2815)) {
         if (this.shouldSuppressInput && packet instanceof class_2851 p) {
            class_10185 input = p.comp_3139();
            class_2851 modifiedPacket = new class_2851(new class_10185(false, false, false, false, input.comp_3163(), input.comp_3164(), input.comp_3165()));
            event.setCancelled(true);
            PacketUtils.sendPacketNoEvent(modifiedPacket);
         }
      } else {
         event.setCancelled(true);
         this.packetQueue.add(packet);
      }
   }

   private void handleSprintBeforeClose() {
      if (mc.field_1724 != null) {
         this.sprintKeyPressed = GLFW.glfwGetKey(mc.method_22683().method_4490(), mc.field_1690.field_1867.method_1429().method_1444()) == 1;
         this.shouldRestoreSprint = mc.field_1724.method_5624() || this.sprintKeyPressed;
         if (mc.field_1724.method_5624()) {
            mc.field_1724.method_5728(false);
         }

         mc.field_1690.field_1867.method_23481(false);
         mc.field_1690.field_1904.method_23481(false);
      }
   }

   private void sendNoMovementInput() {
      try {
         class_2851 noMovementPacket = new class_2851(new class_10185(false, false, false, false, false, false, false));
         PacketUtils.sendPacketNoEvent(noMovementPacket);
      } catch (Exception var2) {
      }
   }

   private void releasePackets() {
      this.sendNoMovementInput();
      if (this.timer.hasTimeElapsed(20L)) {
         this.timer.reset();
         boolean closePacketSent = false;

         while (!this.packetQueue.isEmpty()) {
            class_2596<?> packet = this.packetQueue.poll();
            if (packet != null) {
               if (packet instanceof class_2815) {
                  this.sendNoMovementInput();
                  closePacketSent = true;
               }

               PacketUtils.sendPacketNoEvent(packet);
            }
         }

         if (closePacketSent && this.shouldRestoreSprint && mc.field_1724 != null) {
            if (!this.timer2.hasTimeElapsed(50L)) {
               return;
            }

            this.timer2.reset();
            if (this.canSprint()) {
               mc.field_1724.method_5728(true);
            }

            if (this.sprintKeyPressed) {
               mc.field_1690.field_1867.method_23481(true);
            }
         }

         this.shouldSuppressInput = false;
         mc.field_1690.field_1904.method_23481(GLFW.glfwGetKey(mc.method_22683().method_4490(), mc.field_1690.field_1904.method_1429().method_1444()) == 1);
      }
   }

   private boolean canSprint() {
      return mc.field_1724 == null
         ? false
         : !mc.field_1724.method_5715()
            && !mc.field_1724.method_6115()
            && !mc.field_1724.field_5976
            && mc.field_1724.field_3913.field_3905 > 0.0F
            && GLFW.glfwGetKey(mc.method_22683().method_4490(), mc.field_1690.field_1894.method_1429().method_1444()) == 1;
   }
}
