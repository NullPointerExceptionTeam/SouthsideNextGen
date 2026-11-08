package dev.southside.mixin.render;

import dev.southside.Southside;
import dev.southside.event.events.UpdateHeldItemEvent;
import dev.southside.module.impl.render.OldHitting;
import dev.southside.utils.BlockingHandler;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1743;
import net.minecraft.class_1764;
import net.minecraft.class_1771;
import net.minecraft.class_1776;
import net.minecraft.class_1779;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1803;
import net.minecraft.class_1812;
import net.minecraft.class_1819;
import net.minecraft.class_1823;
import net.minecraft.class_1828;
import net.minecraft.class_1829;
import net.minecraft.class_1839;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_742;
import net.minecraft.class_746;
import net.minecraft.class_759;
import net.minecraft.class_7833;
import net.minecraft.class_811;
import net.minecraft.class_9334;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_759.class})
public abstract class MixinHeldItemRenderer {
   @Shadow
   @Final
   private class_310 field_4050;
   @Shadow
   private class_1799 field_4048;

   @Shadow
   protected abstract void method_3219(class_4587 var1, class_4597 var2, int var3, float var4, float var5, class_1306 var6);

   @Shadow
   protected abstract void method_3231(class_4587 var1, class_4597 var2, int var3, float var4, float var5, float var6);

   @Shadow
   protected abstract void method_3222(class_4587 var1, class_4597 var2, int var3, float var4, class_1306 var5, float var6, class_1799 var7);

   @Shadow
   protected abstract void method_3224(class_4587 var1, class_1306 var2, float var3);

   @Shadow
   protected abstract void method_65816(float var1, float var2, class_4587 var3, int var4, class_1306 var5);

   @Shadow
   public abstract void method_3233(class_1309 var1, class_1799 var2, class_811 var3, boolean var4, class_4587 var5, class_4597 var6, int var7);

   @Shadow
   protected abstract void method_3218(class_4587 var1, float var2, class_1306 var3, class_1799 var4, class_1657 var5);

   @Shadow
   protected abstract void method_49340(class_4587 var1, float var2, class_1306 var3, class_1799 var4, class_1657 var5, float var6);

   @Inject(
      method = {"method_3228(Lnet/minecraft/class_742;FFLnet/minecraft/class_1268;FLnet/minecraft/class_1799;FLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void renderFirstPersonItem(
      class_742 player,
      float tickDelta,
      float pitch,
      class_1268 hand,
      float swingProgress,
      class_1799 item,
      float equipProgress,
      class_4587 matrices,
      class_4597 vertexConsumers,
      int light,
      CallbackInfo ci
   ) {
      if (!player.method_31550()) {
         OldHitting oldHitting = Southside.get().getModuleManager().get(OldHitting.class);
         boolean isMainHand = hand == class_1268.field_5808;
         class_1306 arm = isMainHand ? player.method_6068() : player.method_6068().method_5928();
         boolean isRightArm = arm == class_1306.field_6183;
         int side = isRightArm ? 1 : -1;
         matrices.method_22903();
         if (item.method_7960()) {
            if (isMainHand && !player.method_5767()) {
               this.method_3219(matrices, vertexConsumers, light, equipProgress, swingProgress, arm);
            }
         } else if (item.method_57826(class_9334.field_49646)) {
            if (isMainHand && this.field_4048.method_7960()) {
               this.method_3231(matrices, vertexConsumers, light, pitch, equipProgress, swingProgress);
            } else {
               this.method_3222(matrices, vertexConsumers, light, equipProgress, arm, swingProgress, item);
            }
         } else if (item.method_31574(class_1802.field_8399)) {
            boolean isCharged = class_1764.method_7781(item);
            if (player.method_6115() && player.method_6014() > 0 && player.method_6058() == hand) {
               this.method_3224(matrices, arm, equipProgress);
               matrices.method_46416(side * -0.4785682F, -0.094387F, 0.05731531F);
               matrices.method_22907(class_7833.field_40714.rotationDegrees(-11.935F));
               matrices.method_22907(class_7833.field_40716.rotationDegrees(side * 65.3F));
               matrices.method_22907(class_7833.field_40718.rotationDegrees(side * -9.785F));
               float timeDelta = item.method_7935(player) - (player.method_6014() - tickDelta + 1.0F);
               float pullDelta = timeDelta / class_1764.method_7775(item, player);
               if (pullDelta > 1.0F) {
                  pullDelta = 1.0F;
               }

               if (pullDelta > 0.1F) {
                  float sx = class_3532.method_15374((timeDelta - 0.1F) * 1.3F);
                  float dt = pullDelta - 0.1F;
                  float px = sx * dt;
                  matrices.method_46416(px * 0.0F, px * 0.004F, px * 0.0F);
               }

               matrices.method_46416(pullDelta * 0.0F, pullDelta * 0.0F, pullDelta * 0.04F);
               matrices.method_22905(1.0F, 1.0F, 1.0F + pullDelta * 0.2F);
               matrices.method_22907(class_7833.field_40715.rotationDegrees(side * 45.0F));
            } else {
               this.method_65816(swingProgress, equipProgress, matrices, side, arm);
               if (isCharged && swingProgress < 0.001F && isMainHand) {
                  matrices.method_46416(side * -0.641864F, 0.0F, 0.0F);
                  matrices.method_22907(class_7833.field_40716.rotationDegrees(side * 10.0F));
               }
            }

            this.method_3233(player, item, isRightArm ? class_811.field_4322 : class_811.field_4321, !isRightArm, matrices, vertexConsumers, light);
         } else {
            boolean throwable = this.isThrowable(player);
            boolean isEatOrDrinkMain = player.method_6047().method_7976() == class_1839.field_8950
               || player.method_6047().method_7976() == class_1839.field_8946;
            boolean isEatOrDrinkOff = player.method_6079().method_7976() == class_1839.field_8950
               || player.method_6079().method_7976() == class_1839.field_8946;
            boolean tryingToUseOffhand = !isMainHand
               && this.field_4050.field_1690.field_1904.method_1434()
               && (throwable || isEatOrDrinkOff && player.method_6058() == class_1268.field_5810);
            boolean shouldBlock = false;
            if (isMainHand) {
               shouldBlock = BlockingHandler.isBlocking()
                  || this.field_4050.field_1690.field_1904.method_1434()
                     && player.field_7512 == player.field_7498
                     && !throwable
                     && (!isEatOrDrinkMain || player.method_6058() != class_1268.field_5808)
                     && (!isEatOrDrinkOff || player.method_6058() != class_1268.field_5810);
            }

            boolean isAllowedBlockingItem = item.method_7909() instanceof class_1829;
            if (oldHitting.axe.getValue()) {
               isAllowedBlockingItem = item.method_7909() instanceof class_1829 || item.method_7909() instanceof class_1743;
            }

            if (oldHitting.isEnabled() && isAllowedBlockingItem && shouldBlock && !tryingToUseOffhand) {
               oldHitting.transform(matrices, side, equipProgress, swingProgress, hand, arm);
            } else {
               if (oldHitting.isEnabled() && isMainHand) {
                  matrices.method_22904(oldHitting.itemX.getDouble(), oldHitting.itemY.getDouble(), oldHitting.itemZ.getDouble());
               }

               if (player.method_6115() && player.method_6014() > 0 && player.method_6058() == hand) {
                  class_1839 action = item.method_7976();
                  if (action == class_1839.field_8952) {
                     this.method_3224(matrices, arm, equipProgress);
                  } else if (action != class_1839.field_8950 && action != class_1839.field_8946) {
                     if (action == class_1839.field_8949) {
                        this.method_3224(matrices, arm, equipProgress);
                        if (!(item.method_7909() instanceof class_1819)) {
                           matrices.method_46416(side * -0.14142136F, 0.08F, 0.14142136F);
                           matrices.method_22907(class_7833.field_40714.rotationDegrees(-102.25F));
                           matrices.method_22907(class_7833.field_40716.rotationDegrees(side * 13.365F));
                           matrices.method_22907(class_7833.field_40718.rotationDegrees(side * 78.05F));
                        }
                     } else if (action == class_1839.field_8953) {
                        this.method_3224(matrices, arm, equipProgress);
                        matrices.method_46416(side * -0.2785682F, 0.18344387F, 0.15731531F);
                        matrices.method_22907(class_7833.field_40714.rotationDegrees(-13.935F));
                        matrices.method_22907(class_7833.field_40716.rotationDegrees(side * 35.3F));
                        matrices.method_22907(class_7833.field_40718.rotationDegrees(side * -9.785F));
                        float timeDeltax = item.method_7935(player) - (player.method_6014() - tickDelta + 1.0F);
                        float normalized = timeDeltax / 20.0F;
                        normalized = (normalized * normalized + normalized * 2.0F) / 3.0F;
                        if (normalized > 1.0F) {
                           normalized = 1.0F;
                        }

                        if (normalized > 0.1F) {
                           float g = class_3532.method_15374((timeDeltax - 0.1F) * 1.3F);
                           float h = normalized - 0.1F;
                           float j = g * h;
                           matrices.method_46416(j * 0.0F, j * 0.004F, j * 0.0F);
                        }

                        matrices.method_46416(normalized * 0.0F, normalized * 0.0F, normalized * 0.04F);
                        matrices.method_22905(1.0F, 1.0F, 1.0F + normalized * 0.2F);
                        matrices.method_22907(class_7833.field_40715.rotationDegrees(side * 45.0F));
                     } else if (action == class_1839.field_8951) {
                        this.method_3224(matrices, arm, equipProgress);
                        matrices.method_46416(side * -0.5F, 0.7F, 0.1F);
                        matrices.method_22907(class_7833.field_40714.rotationDegrees(-55.0F));
                        matrices.method_22907(class_7833.field_40716.rotationDegrees(side * 35.3F));
                        matrices.method_22907(class_7833.field_40718.rotationDegrees(side * -9.785F));
                        float m = item.method_7935(player) - (player.method_6014() - tickDelta + 1.0F);
                        float f = m / 10.0F;
                        if (f > 1.0F) {
                           f = 1.0F;
                        }

                        if (f > 0.1F) {
                           float g = class_3532.method_15374((m - 0.1F) * 1.3F);
                           float h = f - 0.1F;
                           float j = g * h;
                           matrices.method_46416(j * 0.0F, j * 0.004F, j * 0.0F);
                        }

                        matrices.method_46416(0.0F, 0.0F, f * 0.2F);
                        matrices.method_22905(1.0F, 1.0F, 1.0F + f * 0.2F);
                        matrices.method_22907(class_7833.field_40715.rotationDegrees(side * 45.0F));
                     } else if (action == class_1839.field_42717) {
                        this.method_49340(matrices, tickDelta, arm, item, player, equipProgress);
                     } else if (action == class_1839.field_55494) {
                        this.method_65816(swingProgress, equipProgress, matrices, side, arm);
                     }
                  } else if (oldHitting.isEnabled()) {
                     if (swingProgress != 0.0F) {
                        matrices.method_22904(side * 0.56, -0.52 + equipProgress * -0.6, -0.72);
                        float progress = class_3532.method_15374(swingProgress * swingProgress * (float) Math.PI);
                        matrices.method_22907(class_7833.field_40715.rotationDegrees(progress * 20.0F));
                        matrices.method_22907(class_7833.field_40717.rotationDegrees(progress * 20.0F));
                        matrices.method_22907(class_7833.field_40713.rotationDegrees(progress * 80.0F));
                        matrices.method_46416(-0.8F, 0.2F, 0.0F);
                        matrices.method_22907(class_7833.field_40716.rotationDegrees(30.0F));
                        matrices.method_22907(class_7833.field_40714.rotationDegrees(-80.0F));
                        matrices.method_22907(class_7833.field_40716.rotationDegrees(60.0F));
                        matrices.method_22905(1.4F, 1.4F, 1.4F);
                     } else {
                        this.method_3218(matrices, tickDelta, arm, item, player);
                        this.method_3224(matrices, arm, equipProgress);
                        this.doSwingAnimation(matrices, swingProgress);
                     }
                  } else {
                     this.method_3218(matrices, tickDelta, arm, item, player);
                     this.method_3224(matrices, arm, equipProgress);
                  }
               } else if (player.method_6123()) {
                  this.method_3224(matrices, arm, equipProgress);
                  matrices.method_46416(side * -0.4F, 0.8F, 0.3F);
                  matrices.method_22907(class_7833.field_40716.rotationDegrees(side * 65.0F));
                  matrices.method_22907(class_7833.field_40718.rotationDegrees(side * -85.0F));
               } else {
                  this.method_65816(swingProgress, equipProgress, matrices, side, arm);
               }
            }

            this.method_3233(player, item, isRightArm ? class_811.field_4322 : class_811.field_4321, !isRightArm, matrices, vertexConsumers, light);
         }

         matrices.method_22909();
         ci.cancel();
      }
   }

   private boolean isThrowable(class_742 player) {
      class_1799 stack = player.method_6079();
      return !stack.method_7960()
         && (
            stack.method_7909() instanceof class_1823
               || stack.method_7909() instanceof class_1771
               || stack.method_7909() instanceof class_1776
               || stack.method_7909() instanceof class_1779
               || stack.method_7909() instanceof class_1812
               || stack.method_7909() instanceof class_1803
               || stack.method_7909() instanceof class_1828
         );
   }

   private void doSwingAnimation(class_4587 m, float swingProgress) {
      float f = class_3532.method_15374(swingProgress * swingProgress * (float) Math.PI);
      float f1 = class_3532.method_15374(class_3532.method_15355(swingProgress) * (float) Math.PI);
      m.method_22907(class_7833.field_40716.rotationDegrees(f * -20.0F));
      m.method_22907(class_7833.field_40718.rotationDegrees(f1 * -20.0F));
      m.method_22907(class_7833.field_40714.rotationDegrees(f1 * -80.0F));
   }

   @Redirect(
      method = {"method_3220()V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_746;method_6047()Lnet/minecraft/class_1799;"
      )
   )
   public class_1799 hookMainHand(class_746 player) {
      UpdateHeldItemEvent event = new UpdateHeldItemEvent(class_1268.field_5808, player.method_6047());
      if (player == class_310.method_1551().field_1724) {
         Southside.get().getEventBus().call(event);
      }

      return event.getItem();
   }

   @Redirect(
      method = {"method_3220()V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_746;method_6079()Lnet/minecraft/class_1799;"
      )
   )
   public class_1799 hookOffHand(class_746 player) {
      UpdateHeldItemEvent event = new UpdateHeldItemEvent(class_1268.field_5810, player.method_6079());
      if (player == class_310.method_1551().field_1724) {
         Southside.get().getEventBus().call(event);
      }

      return event.getItem();
   }
}
