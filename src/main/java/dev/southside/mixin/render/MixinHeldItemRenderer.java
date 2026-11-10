package dev.southside.mixin.render;

import dev.southside.Southside;
import dev.southside.event.events.UpdateHeldItemEvent;
import dev.southside.module.impl.render.OldHitting;
import dev.southside.utils.BlockingHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.Arm;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EggItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.item.PotionItem;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SnowballItem;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.consume.UseAction;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.component.DataComponentTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({HeldItemRenderer.class})
public abstract class MixinHeldItemRenderer {
   @Shadow
   @Final
   private MinecraftClient client;
   @Shadow
   private ItemStack offHand;

   @Shadow
   protected abstract void renderArmHoldingItem(MatrixStack var1, VertexConsumerProvider var2, int var3, float var4, float var5, Arm var6);

   @Shadow
   protected abstract void renderMapInBothHands(MatrixStack var1, VertexConsumerProvider var2, int var3, float var4, float var5, float var6);

   @Shadow
   protected abstract void renderMapInOneHand(MatrixStack var1, VertexConsumerProvider var2, int var3, float var4, Arm var5, float var6, ItemStack var7);

   @Shadow
   protected abstract void applyEquipOffset(MatrixStack var1, Arm var2, float var3);

   @Shadow
   protected abstract void swingArm(float var1, float var2, MatrixStack var3, int var4, Arm var5);

   @Shadow
   public abstract void renderItem(LivingEntity var1, ItemStack var2, ModelTransformationMode var3, boolean var4, MatrixStack var5, VertexConsumerProvider var6, int var7);

   @Shadow
   protected abstract void applyEatOrDrinkTransformation(MatrixStack var1, float var2, Arm var3, ItemStack var4, PlayerEntity var5);

   @Shadow
   protected abstract void applyBrushTransformation(MatrixStack var1, float var2, Arm var3, ItemStack var4, PlayerEntity var5, float var6);

   @Inject(
      method = {"renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void renderFirstPersonItem(
      AbstractClientPlayerEntity player,
      float tickDelta,
      float pitch,
      Hand hand,
      float swingProgress,
      ItemStack item,
      float equipProgress,
      MatrixStack matrices,
      VertexConsumerProvider vertexConsumers,
      int light,
      CallbackInfo ci
   ) {
      if (!player.isUsingSpyglass()) {
         OldHitting oldHitting = Southside.get().getModuleManager().get(OldHitting.class);
         boolean isMainHand = hand == Hand.MAIN_HAND;
         Arm arm = isMainHand ? player.getMainArm() : player.getMainArm().getOpposite();
         boolean isRightArm = arm == Arm.RIGHT;
         int side = isRightArm ? 1 : -1;
         matrices.push();
         if (item.isEmpty()) {
            if (isMainHand && !player.isInvisible()) {
               this.renderArmHoldingItem(matrices, vertexConsumers, light, equipProgress, swingProgress, arm);
            }
         } else if (item.contains(DataComponentTypes.MAP_ID)) {
            if (isMainHand && this.offHand.isEmpty()) {
               this.renderMapInBothHands(matrices, vertexConsumers, light, pitch, equipProgress, swingProgress);
            } else {
               this.renderMapInOneHand(matrices, vertexConsumers, light, equipProgress, arm, swingProgress, item);
            }
         } else if (item.isOf(Items.CROSSBOW)) {
            boolean isCharged = CrossbowItem.isCharged(item);
            if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
               this.applyEquipOffset(matrices, arm, equipProgress);
               matrices.translate(side * -0.4785682F, -0.094387F, 0.05731531F);
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-11.935F));
               matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * 65.3F));
               matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side * -9.785F));
               float timeDelta = item.getMaxUseTime(player) - (player.getItemUseTimeLeft() - tickDelta + 1.0F);
               float pullDelta = timeDelta / CrossbowItem.getPullTime(item, player);
               if (pullDelta > 1.0F) {
                  pullDelta = 1.0F;
               }

               if (pullDelta > 0.1F) {
                  float sx = MathHelper.sin((timeDelta - 0.1F) * 1.3F);
                  float dt = pullDelta - 0.1F;
                  float px = sx * dt;
                  matrices.translate(px * 0.0F, px * 0.004F, px * 0.0F);
               }

               matrices.translate(pullDelta * 0.0F, pullDelta * 0.0F, pullDelta * 0.04F);
               matrices.scale(1.0F, 1.0F, 1.0F + pullDelta * 0.2F);
               matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(side * 45.0F));
            } else {
               this.swingArm(swingProgress, equipProgress, matrices, side, arm);
               if (isCharged && swingProgress < 0.001F && isMainHand) {
                  matrices.translate(side * -0.641864F, 0.0F, 0.0F);
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * 10.0F));
               }
            }

            this.renderItem(player, item, isRightArm ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND, !isRightArm, matrices, vertexConsumers, light);
         } else {
            boolean throwable = this.isThrowable(player);
            boolean isEatOrDrinkMain = player.getMainHandStack().getUseAction() == UseAction.EAT
               || player.getMainHandStack().getUseAction() == UseAction.DRINK;
            boolean isEatOrDrinkOff = player.getOffHandStack().getUseAction() == UseAction.EAT
               || player.getOffHandStack().getUseAction() == UseAction.DRINK;
            boolean tryingToUseOffhand = !isMainHand
               && this.client.options.useKey.isPressed()
               && (throwable || isEatOrDrinkOff && player.getActiveHand() == Hand.OFF_HAND);
            boolean shouldBlock = false;
            if (isMainHand) {
               shouldBlock = BlockingHandler.isBlocking()
                  || this.client.options.useKey.isPressed()
                     && player.currentScreenHandler == player.playerScreenHandler
                     && !throwable
                     && (!isEatOrDrinkMain || player.getActiveHand() != Hand.MAIN_HAND)
                     && (!isEatOrDrinkOff || player.getActiveHand() != Hand.OFF_HAND);
            }

            boolean isAllowedBlockingItem = item.getItem() instanceof SwordItem;
            if (oldHitting.axe.getValue()) {
               isAllowedBlockingItem = item.getItem() instanceof SwordItem || item.getItem() instanceof AxeItem;
            }

            if (oldHitting.isEnabled() && isAllowedBlockingItem && shouldBlock && !tryingToUseOffhand) {
               oldHitting.transform(matrices, side, equipProgress, swingProgress, hand, arm);
            } else {
               if (oldHitting.isEnabled() && isMainHand) {
                  matrices.translate(oldHitting.itemX.getDouble(), oldHitting.itemY.getDouble(), oldHitting.itemZ.getDouble());
               }

               if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
                  UseAction action = item.getUseAction();
                  if (action == UseAction.NONE) {
                     this.applyEquipOffset(matrices, arm, equipProgress);
                  } else if (action != UseAction.EAT && action != UseAction.DRINK) {
                     if (action == UseAction.BLOCK) {
                        this.applyEquipOffset(matrices, arm, equipProgress);
                        if (!(item.getItem() instanceof ShieldItem)) {
                           matrices.translate(side * -0.14142136F, 0.08F, 0.14142136F);
                           matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25F));
                           matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * 13.365F));
                           matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side * 78.05F));
                        }
                     } else if (action == UseAction.BOW) {
                        this.applyEquipOffset(matrices, arm, equipProgress);
                        matrices.translate(side * -0.2785682F, 0.18344387F, 0.15731531F);
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-13.935F));
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * 35.3F));
                        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side * -9.785F));
                        float timeDeltax = item.getMaxUseTime(player) - (player.getItemUseTimeLeft() - tickDelta + 1.0F);
                        float normalized = timeDeltax / 20.0F;
                        normalized = (normalized * normalized + normalized * 2.0F) / 3.0F;
                        if (normalized > 1.0F) {
                           normalized = 1.0F;
                        }

                        if (normalized > 0.1F) {
                           float g = MathHelper.sin((timeDeltax - 0.1F) * 1.3F);
                           float h = normalized - 0.1F;
                           float j = g * h;
                           matrices.translate(j * 0.0F, j * 0.004F, j * 0.0F);
                        }

                        matrices.translate(normalized * 0.0F, normalized * 0.0F, normalized * 0.04F);
                        matrices.scale(1.0F, 1.0F, 1.0F + normalized * 0.2F);
                        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(side * 45.0F));
                     } else if (action == UseAction.SPEAR) {
                        this.applyEquipOffset(matrices, arm, equipProgress);
                        matrices.translate(side * -0.5F, 0.7F, 0.1F);
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-55.0F));
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * 35.3F));
                        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side * -9.785F));
                        float m = item.getMaxUseTime(player) - (player.getItemUseTimeLeft() - tickDelta + 1.0F);
                        float f = m / 10.0F;
                        if (f > 1.0F) {
                           f = 1.0F;
                        }

                        if (f > 0.1F) {
                           float g = MathHelper.sin((m - 0.1F) * 1.3F);
                           float h = f - 0.1F;
                           float j = g * h;
                           matrices.translate(j * 0.0F, j * 0.004F, j * 0.0F);
                        }

                        matrices.translate(0.0F, 0.0F, f * 0.2F);
                        matrices.scale(1.0F, 1.0F, 1.0F + f * 0.2F);
                        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(side * 45.0F));
                     } else if (action == UseAction.BRUSH) {
                        this.applyBrushTransformation(matrices, tickDelta, arm, item, player, equipProgress);
                     } else if (action == UseAction.BUNDLE) {
                        this.swingArm(swingProgress, equipProgress, matrices, side, arm);
                     }
                  } else if (oldHitting.isEnabled()) {
                     if (swingProgress != 0.0F) {
                        matrices.translate(side * 0.56, -0.52 + equipProgress * -0.6, -0.72);
                        float progress = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
                        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(progress * 20.0F));
                        matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(progress * 20.0F));
                        matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(progress * 80.0F));
                        matrices.translate(-0.8F, 0.2F, 0.0F);
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(30.0F));
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80.0F));
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(60.0F));
                        matrices.scale(1.4F, 1.4F, 1.4F);
                     } else {
                        this.applyEatOrDrinkTransformation(matrices, tickDelta, arm, item, player);
                        this.applyEquipOffset(matrices, arm, equipProgress);
                        this.doSwingAnimation(matrices, swingProgress);
                     }
                  } else {
                     this.applyEatOrDrinkTransformation(matrices, tickDelta, arm, item, player);
                     this.applyEquipOffset(matrices, arm, equipProgress);
                  }
               } else if (player.isUsingRiptide()) {
                  this.applyEquipOffset(matrices, arm, equipProgress);
                  matrices.translate(side * -0.4F, 0.8F, 0.3F);
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * 65.0F));
                  matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side * -85.0F));
               } else {
                  this.swingArm(swingProgress, equipProgress, matrices, side, arm);
               }
            }

            this.renderItem(player, item, isRightArm ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND, !isRightArm, matrices, vertexConsumers, light);
         }

         matrices.pop();
         ci.cancel();
      }
   }

   private boolean isThrowable(AbstractClientPlayerEntity player) {
      ItemStack stack = player.getOffHandStack();
      return !stack.isEmpty()
         && (
            stack.getItem() instanceof SnowballItem
               || stack.getItem() instanceof EggItem
               || stack.getItem() instanceof EnderPearlItem
               || stack.getItem() instanceof ExperienceBottleItem
               || stack.getItem() instanceof PotionItem
               || stack.getItem() instanceof LingeringPotionItem
               || stack.getItem() instanceof SplashPotionItem
         );
   }

   private void doSwingAnimation(MatrixStack m, float swingProgress) {
      float f = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
      float f1 = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
      m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f * -20.0F));
      m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f1 * -20.0F));
      m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(f1 * -80.0F));
   }

   @Redirect(
      method = {"updateHeldItems()V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/network/ClientPlayerEntity;getMainHandStack()Lnet/minecraft/item/ItemStack;"
      )
   )
   public ItemStack hookMainHand(ClientPlayerEntity player) {
      UpdateHeldItemEvent event = new UpdateHeldItemEvent(Hand.MAIN_HAND, player.getMainHandStack());
      if (player == MinecraftClient.getInstance().player) {
         Southside.get().getEventBus().call(event);
      }

      return event.getItem();
   }

   @Redirect(
      method = {"updateHeldItems()V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/network/ClientPlayerEntity;getOffHandStack()Lnet/minecraft/item/ItemStack;"
      )
   )
   public ItemStack hookOffHand(ClientPlayerEntity player) {
      UpdateHeldItemEvent event = new UpdateHeldItemEvent(Hand.OFF_HAND, player.getOffHandStack());
      if (player == MinecraftClient.getInstance().player) {
         Southside.get().getEventBus().call(event);
      }

      return event.getItem();
   }
}
