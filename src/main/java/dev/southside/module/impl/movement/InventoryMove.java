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
import net.minecraft.util.PlayerInput;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.lwjgl.glfw.GLFW;

public class InventoryMove extends Module {
   private boolean wasInContainer = false;
   private final LinkedBlockingQueue<Packet<?>> packetQueue = new LinkedBlockingQueue<>();
   private boolean invOpen = false;
   private boolean shouldSuppressInput = false;
   private boolean lastInputSuppression = false;
   private final TimerUtil timer = new TimerUtil();
   private final TimerUtil timer2 = new TimerUtil();
   private final TimerUtil timer3 = new TimerUtil();
   private boolean shouldRestoreSprint = false;
   private boolean sprintKeyPressed = false;
   public boolean isReleasing = false;
   private Screen screen;

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
      if (mc.currentScreen instanceof HandledScreen && this.invOpen && event.screen == null) {
         this.handleSprintBeforeClose();
         this.screen = null;
         event.setCancelled(true);
         this.timer3.reset();
         this.lastInputSuppression = true;
         mc.options.useKey.setPressed(false);
      }
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (mc.currentScreen instanceof HandledScreen) {
         this.updateKeyBindings();
      }
   }

   @EventHandler(
      priority = 200
   )
   public void onMotion(MotionEvent event) {
      if (mc.currentScreen != null && !(mc.currentScreen instanceof ChatScreen)) {
         boolean inContainer = mc.currentScreen instanceof HandledScreen;
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
            mc.setScreen(this.screen);
            this.sendNoMovementInput();
            this.releasePackets();
            this.resetState();
         }
      }
   }

   private void updateKeyBindings() {
      if (mc.player != null) {
         KeyBinding.setKeyPressed(
            mc.options.forwardKey.getDefaultKey(), GLFW.glfwGetKey(mc.getWindow().getHandle(), mc.options.forwardKey.getDefaultKey().getCode()) == 1
         );
         KeyBinding.setKeyPressed(
            mc.options.backKey.getDefaultKey(), GLFW.glfwGetKey(mc.getWindow().getHandle(), mc.options.backKey.getDefaultKey().getCode()) == 1
         );
         KeyBinding.setKeyPressed(
            mc.options.leftKey.getDefaultKey(), GLFW.glfwGetKey(mc.getWindow().getHandle(), mc.options.leftKey.getDefaultKey().getCode()) == 1
         );
         KeyBinding.setKeyPressed(
            mc.options.rightKey.getDefaultKey(), GLFW.glfwGetKey(mc.getWindow().getHandle(), mc.options.rightKey.getDefaultKey().getCode()) == 1
         );
         KeyBinding.setKeyPressed(
            mc.options.jumpKey.getDefaultKey(), GLFW.glfwGetKey(mc.getWindow().getHandle(), mc.options.jumpKey.getDefaultKey().getCode()) == 1
         );
         KeyBinding.setKeyPressed(
            mc.options.sprintKey.getDefaultKey(), GLFW.glfwGetKey(mc.getWindow().getHandle(), mc.options.sprintKey.getDefaultKey().getCode()) == 1
         );
         KeyBinding.setKeyPressed(
            mc.options.useKey.getDefaultKey(), GLFW.glfwGetKey(mc.getWindow().getHandle(), mc.options.useKey.getDefaultKey().getCode()) == 1
         );
      }
   }

   @EventHandler
   public void onPacketSend(PacketEvent event) {
      Packet<?> packet = event.getPacket();
      if (!this.wasInContainer || !(packet instanceof ClickSlotC2SPacket) && !(packet instanceof CloseHandledScreenC2SPacket)) {
         if (this.shouldSuppressInput && packet instanceof PlayerInputC2SPacket p) {
            PlayerInput input = p.input();
            PlayerInputC2SPacket modifiedPacket = new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, input.jump(), input.sneak(), input.sprint()));
            event.setCancelled(true);
            PacketUtils.sendPacketNoEvent(modifiedPacket);
         }
      } else {
         event.setCancelled(true);
         this.packetQueue.add(packet);
      }
   }

   private void handleSprintBeforeClose() {
      if (mc.player != null) {
         this.sprintKeyPressed = GLFW.glfwGetKey(mc.getWindow().getHandle(), mc.options.sprintKey.getDefaultKey().getCode()) == 1;
         this.shouldRestoreSprint = mc.player.isSprinting() || this.sprintKeyPressed;
         if (mc.player.isSprinting()) {
            mc.player.setSprinting(false);
         }

         mc.options.sprintKey.setPressed(false);
         mc.options.useKey.setPressed(false);
      }
   }

   private void sendNoMovementInput() {
      try {
         PlayerInputC2SPacket noMovementPacket = new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, false, false, false));
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
            Packet<?> packet = this.packetQueue.poll();
            if (packet != null) {
               if (packet instanceof CloseHandledScreenC2SPacket) {
                  this.sendNoMovementInput();
                  closePacketSent = true;
               }

               PacketUtils.sendPacketNoEvent(packet);
            }
         }

         if (closePacketSent && this.shouldRestoreSprint && mc.player != null) {
            if (!this.timer2.hasTimeElapsed(50L)) {
               return;
            }

            this.timer2.reset();
            if (this.canSprint()) {
               mc.player.setSprinting(true);
            }

            if (this.sprintKeyPressed) {
               mc.options.sprintKey.setPressed(true);
            }
         }

         this.shouldSuppressInput = false;
         mc.options.useKey.setPressed(GLFW.glfwGetKey(mc.getWindow().getHandle(), mc.options.useKey.getDefaultKey().getCode()) == 1);
      }
   }

   private boolean canSprint() {
      return mc.player == null
         ? false
         : !mc.player.isSneaking()
            && !mc.player.isUsingItem()
            && !mc.player.horizontalCollision
            && mc.player.input.movementForward > 0.0F
            && GLFW.glfwGetKey(mc.getWindow().getHandle(), mc.options.forwardKey.getDefaultKey().getCode()) == 1;
   }
}
