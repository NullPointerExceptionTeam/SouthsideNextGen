package dev.southside.module.impl.movement;

import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import dev.southside.event.Stage;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.MotionEvent;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.PlayerUpdateEvent;
import dev.southside.event.events.Render2DEvent;
import dev.southside.event.events.RotationEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.ChatUtils;
import dev.southside.utils.network.PacketUtils;
import dev.southside.utils.player.PlayerUtils;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2596;
import net.minecraft.class_2708;
import net.minecraft.class_2743;
import org.lwjgl.glfw.GLFW;

public class LongJump extends Module {
   private boolean delayed = false;
   public static Rotation rotation = null;
   private int receivedKnockbacks = 0;
   private int usedFireballCount = 0;
   private int releasedKnockbacks = 0;
   private boolean enabled = false;
   private boolean notMoving = false;
   private boolean shouldDelayed;
   private boolean mouse5Pressed = false;
   private boolean mouse4Pressed = false;
   private boolean isUsingItem = false;
   private int lastSlot = -1;
   private int rotateTick = 0;
   private int initialFireballCount = 0;
   private final List<Integer> knockbackPositions = new ArrayList<>();
   private final LinkedBlockingQueue<class_2596<?>> packets = new LinkedBlockingQueue<>();
   private final List<Double> knockbackStrengths = new ArrayList<>();

   public LongJump() {
      super(Category.Movement, "LongJump", "Allows you to jump higher");
   }

   @EventHandler
   private void onRender2D(Render2DEvent event) {
      NanoVGHelper instance = NanoVGHelper.INSTANCE;
      instance.setupAndDraw(true, vg -> {
         double displayValue = this.knockbackStrengths.stream().skip(this.releasedKnockbacks).mapToDouble(Double::doubleValue).sum();
         int count = this.knockbackStrengths.size();
         String text = String.format("Knockback Count: %d", count);
         double ratio = Math.min(displayValue / 5.0, 1.0);
         ratio = Math.sqrt(ratio);
         float hue = (float)((1.0 - ratio) * 120.0 / 360.0);
         int rgb = Color.HSBtoRGB(hue, 1.0F, 1.0F);
         int color = 0xFF000000 | rgb & 16777215;
         float textWidth = instance.getTextWidth(vg, text, 10.0F, Fonts.WQY);
         float x = (mc.method_22683().method_4486() - textWidth) / 2.0F;
         float y = mc.method_22683().method_4502() / 2.0F + 50.0F;
         instance.drawTextWithFormatting(vg, text, x, y, color, 10.0F, Fonts.WQY);
      });
   }

   @Override
   public void onEnable() {
      ChatUtils.message("Press Mouse4 Start && Mouse5 to release");
      this.delayed = false;
      this.rotateTick = 0;
      this.lastSlot = -1;
      this.isUsingItem = false;
      this.enabled = true;
      this.notMoving = false;
      rotation = null;
      this.usedFireballCount = 0;
      this.receivedKnockbacks = 0;
      this.releasedKnockbacks = 0;
      this.initialFireballCount = 0;
      this.knockbackPositions.clear();
      this.knockbackStrengths.clear();
      this.mouse4Pressed = false;
      this.mouse5Pressed = false;
      this.shouldDelayed = true;
      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.releaseAll();
      this.slotBack();
      mc.field_1690.field_1904.method_23481(false);
      mc.field_1690.field_1903.method_23481(false);
      rotation = null;
      this.isUsingItem = false;
      this.usedFireballCount = 0;
      this.receivedKnockbacks = 0;
      this.releasedKnockbacks = 0;
      this.knockbackPositions.clear();
      this.mouse4Pressed = false;
      this.mouse5Pressed = false;
      super.onDisable();
   }

   private void slotBack() {
      if (this.lastSlot != -1 && mc.field_1724 != null) {
         mc.field_1724.method_31548().field_7545 = this.lastSlot;
         this.lastSlot = -1;
      }
   }

   private void releaseAll() {
      while (!this.packets.isEmpty()) {
         try {
            class_2596<?> packet = this.packets.poll();
            if (packet != null && mc.method_1562() != null) {
               PacketUtils.handlePacketNoEvent(packet);
            }
         } catch (Exception var2) {
            var2.printStackTrace();
         }
      }
   }

   private void releaseToKnockback(int knockbackIndex) {
      if (knockbackIndex < this.knockbackPositions.size()) {
         int targetPosition = this.knockbackPositions.get(knockbackIndex);
         int releasedCount = 0;

         while (!this.packets.isEmpty() && releasedCount <= targetPosition) {
            try {
               class_2596<?> packet = this.packets.poll();
               if (packet != null && mc.method_1562() != null) {
                  PacketUtils.handlePacketNoEvent(packet);
               }

               releasedCount++;
            } catch (Exception var5) {
               var5.printStackTrace();
            }
         }

         for (int i = knockbackIndex + 1; i < this.knockbackPositions.size(); i++) {
            this.knockbackPositions.set(i, this.knockbackPositions.get(i) - (targetPosition + 1));
         }
      }
   }

   @EventHandler
   public void onUpdate(PlayerUpdateEvent event) {
      if (this.isEnabled()) {
         if (this.shouldDelayed) {
            this.usedFireballCount++;
            this.shouldDelayed = false;
         }

         if (this.enabled) {
            if (!PlayerUtils.isMoving()) {
               this.notMoving = true;
            }

            this.enabled = false;
         }

         boolean currentMouse4 = GLFW.glfwGetMouseButton(mc.method_22683().method_4490(), 3) == 1;
         if (currentMouse4 && !this.mouse4Pressed) {
            this.mouse4Pressed = true;
            if (!this.isUsingItem && this.rotateTick == 0) {
               int fireballSlot = this.setupFireballSlot();
               if (fireballSlot != -1) {
                  this.lastSlot = mc.field_1724.method_31548().field_7545;
                  mc.field_1724.method_31548().field_7545 = fireballSlot;
                  this.rotateTick = 1;
               }
            }
         } else if (!currentMouse4) {
            this.mouse4Pressed = false;
         }

         boolean currentMouse5 = GLFW.glfwGetMouseButton(mc.method_22683().method_4490(), 4) == 1;
         if (currentMouse5 && !this.mouse5Pressed) {
            this.mouse5Pressed = true;
            if (this.delayed && this.releasedKnockbacks < this.receivedKnockbacks) {
               this.releaseToKnockback(this.releasedKnockbacks);
               this.releasedKnockbacks++;
               if (this.releasedKnockbacks >= this.receivedKnockbacks) {
                  this.delayed = false;
                  this.setEnable(false);
               }
            } else if (!this.delayed) {
               this.setEnable(false);
            }
         } else if (!currentMouse5) {
            this.mouse5Pressed = false;
         }
      }
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (this.isEnabled() && mc.field_1724 != null) {
         if (this.delayed && event.getOrigin() == TransferOrigin.RECEIVE) {
            class_2596<?> packet = event.getPacket();
            if (packet instanceof class_2708) {
               event.setCancelled(true);
            } else {
               if (packet instanceof class_2743 vel && vel.method_11818() == mc.field_1724.method_5628()) {
                  this.receivedKnockbacks++;
                  this.knockbackPositions.add(this.packets.size());
                  double vx = vel.method_11815() / 8000.0;
                  double vy = vel.method_11816() / 8000.0;
                  double vz = vel.method_11819() / 8000.0;
                  double strength = Math.sqrt(vx * vx + vy * vy + vz * vz);
                  this.knockbackStrengths.add(strength);
               }

               event.setCancelled(true);
               this.packets.add(packet);
            }
         } else {
            if (event.getPacket() instanceof class_2743 vel
               && event.getOrigin() == TransferOrigin.RECEIVE
               && vel.method_11818() == mc.field_1724.method_5628()
               && this.usedFireballCount > 0
               && !this.delayed) {
               this.receivedKnockbacks++;
               this.knockbackPositions.add(this.packets.size());
               double vx = vel.method_11815() / 8000.0;
               double vy = vel.method_11816() / 8000.0;
               double vz = vel.method_11819() / 8000.0;
               double strength = Math.sqrt(vx * vx + vy * vy + vz * vz);
               this.knockbackStrengths.add(strength);
               event.setCancelled(true);
               this.packets.add(event.getPacket());
               this.delayed = true;
            }
         }
      } else {
         if (this.delayed) {
            mc.execute(() -> {
               this.releaseAll();
               this.delayed = false;
            });
         }
      }
   }

   @EventHandler
   public void onMotion(MotionEvent event) {
      if (mc.field_1724 != null) {
         if (this.isEnabled()) {
            if (event.getState() == Stage.PRE) {
               if (this.rotateTick > 0) {
                  if (this.rotateTick == 1) {
                     this.usedFireballCount++;
                     mc.field_1690.field_1903.method_23481(true);
                     float yaw;
                     float pitch;
                     if (!this.notMoving) {
                        yaw = mc.field_1724.method_36454() - 180.0F;
                        pitch = 88.0F;
                     } else {
                        yaw = mc.field_1724.method_36454();
                        pitch = 90.0F;
                     }

                     rotation = new Rotation(yaw, pitch);
                  }

                  if (this.rotateTick >= 2) {
                     this.rotateTick = 0;
                     int fireballSlot = this.setupFireballSlot();
                     if (fireballSlot != -1) {
                        mc.field_1724.method_31548().field_7545 = fireballSlot;
                        this.initialFireballCount = this.getFireballCount();
                        mc.field_1690.field_1904.method_23481(true);
                        this.isUsingItem = true;
                     } else {
                        this.setEnable(false);
                     }
                  }

                  if (this.rotateTick != 0) {
                     this.rotateTick++;
                  }
               }
            } else if (this.isUsingItem) {
               int currentFireballCount = this.getFireballCount();
               if (currentFireballCount < this.initialFireballCount) {
                  mc.field_1690.field_1904.method_23481(false);
                  mc.field_1690.field_1903.method_23481(false);
                  rotation = null;
                  this.isUsingItem = false;
               } else if (this.getFireballSlot() == -1) {
                  mc.field_1690.field_1904.method_23481(false);
                  mc.field_1690.field_1903.method_23481(false);
                  rotation = null;
                  this.isUsingItem = false;
                  this.slotBack();
               }
            }
         }

         if (rotation != null) {
            RotationUtils.setRotation(rotation);
         }
      }
   }

   @EventHandler
   public void onTick(RotationEvent event) {
   }

   private int getFireballSlot() {
      for (int i = 0; i < 9; i++) {
         class_1799 stack = mc.field_1724.method_31548().method_5438(i);
         if (!stack.method_7960() && stack.method_7909() == class_1802.field_8814) {
            return i;
         }
      }

      return -1;
   }

   private int getFireballCount() {
      int count = 0;

      for (int i = 0; i < 9; i++) {
         class_1799 itemStack = mc.field_1724.method_31548().method_5438(i);
         if (itemStack.method_7909() == class_1802.field_8814) {
            count += itemStack.method_7947();
         }
      }

      return count;
   }

   private int setupFireballSlot() {
      int fireballSlot = this.getFireballSlot();
      if (fireballSlot == -1) {
         this.setEnable(false);
      }

      return fireballSlot;
   }

   @Override
   public int rotationPriority() {
      return 1;
   }
}
