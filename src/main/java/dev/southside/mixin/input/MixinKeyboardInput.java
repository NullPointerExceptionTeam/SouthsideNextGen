package dev.southside.mixin.input;

import dev.southside.Southside;
import dev.southside.event.events.MovementInputEvent;
import dev.southside.event.events.PostMoveInput;
import dev.southside.utils.player.MovementUtils;
import dev.southside.utils.player.PlayerUtils;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import java.util.ArrayList;
import net.minecraft.util.PlayerInput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({KeyboardInput.class})
public abstract class MixinKeyboardInput extends Input {
   @Unique
   private final ArrayList<Rotation> possible = new ArrayList<>();
   @Shadow
   @Final
   private GameOptions settings;

   @Shadow
   private static float getMovementMultiplier(boolean positive, boolean negative) {
      return 0.0F;
   }

   @Inject(
      method = {"tick()V"},
      at = {@At("TAIL")},
      allow = 1
   )
   private void injectMovementInputEvent(CallbackInfo ci) {
      this.playerInput = new PlayerInput(
         this.settings.forwardKey.isPressed(),
         this.settings.backKey.isPressed(),
         this.settings.leftKey.isPressed(),
         this.settings.rightKey.isPressed(),
         this.settings.jumpKey.isPressed(),
         this.settings.sneakKey.isPressed(),
         this.settings.sprintKey.isPressed()
      );
      this.movementForward = getMovementMultiplier(this.playerInput.forward(), this.playerInput.backward());
      this.movementSideways = getMovementMultiplier(this.playerInput.left(), this.playerInput.right());
      MovementInputEvent event = Southside.INSTANCE
         .getEventBus()
         .post(
            new MovementInputEvent(
               this.playerInput.forward(),
               this.playerInput.backward(),
               this.playerInput.left(),
               this.playerInput.right(),
               this.playerInput.sneak(),
               this.playerInput.jump(),
               this.playerInput.sprint(),
               false
            )
         );

      RotationUtils.setSilentFixSkipped(event.isSkipSilent());
      this.playerInput = new PlayerInput(event.isForward(), event.isBack(), event.isLeft(), event.isRight(), event.isJump(), event.isSneak(), event.isSprint());
      this.movementForward = getMovementMultiplier(this.playerInput.forward(), this.playerInput.backward());
      this.movementSideways = getMovementMultiplier(this.playerInput.left(), this.playerInput.right());
      this.fixStrafeMovement(event);
      this.movementForward = getMovementMultiplier(this.playerInput.forward(), this.playerInput.backward());
      this.movementSideways = getMovementMultiplier(this.playerInput.left(), this.playerInput.right());
      if (MovementUtils.consumeInputHold()) {
         this.playerInput = new PlayerInput(
            false, false, false, false, this.playerInput.jump(), this.playerInput.sneak(), this.playerInput.sprint()
         );
         this.movementForward = getMovementMultiplier(this.playerInput.forward(), this.playerInput.backward());
         this.movementSideways = getMovementMultiplier(this.playerInput.left(), this.playerInput.right());
      }
      Southside.INSTANCE.getEventBus().post(new PostMoveInput());
   }

   @Unique
   private void fixStrafeMovement(MovementInputEvent event) {
      if (!event.isSkipSilent()) {
         ClientPlayerEntity player = MinecraftClient.getInstance().player;
         if (player != null) {
            if (!RotationUtils.isStrictFix()) {
               if (RotationUtils.isStrafeFix()) {
                  if (RotationUtils.getRotation() != null) {
                     Rotation rotation = RotationUtils.getRotationSafely();
                     float forward = this.movementForward;
                     float strafe = this.movementSideways;
                     int angleUnit = 45;
                     float angleTolerance = 22.5F;
                     float directionFactor = Math.max(Math.abs(forward), Math.abs(strafe));
                     double angleDifference = PlayerUtils.wrapDegrees(PlayerUtils.direction(forward, strafe) - rotation.yaw);
                     double angleDistance = Math.abs(angleDifference);
                     forward = 0.0F;
                     strafe = 0.0F;
                     if (angleDistance <= angleUnit + angleTolerance) {
                        forward++;
                     } else if (angleDistance >= 180.0F - angleUnit - angleTolerance) {
                        forward--;
                     }

                     if (angleDifference >= angleUnit - angleTolerance && angleDifference <= 180.0F - angleUnit + angleTolerance) {
                        strafe--;
                     } else if (angleDifference <= -angleUnit + angleTolerance && angleDifference >= -180.0F + angleUnit - angleTolerance) {
                        strafe++;
                     }

                     forward *= directionFactor;
                     strafe *= directionFactor;
                     boolean up;
                     boolean down;
                     if (forward == 1.0F) {
                        up = true;
                        down = false;
                     } else if (forward == 0.0F) {
                        up = false;
                        down = false;
                     } else {
                        up = false;
                        down = true;
                     }

                     boolean left;
                     boolean right;
                     if (strafe == 1.0F) {
                        left = true;
                        right = false;
                     } else if (strafe == 0.0F) {
                        left = false;
                        right = false;
                     } else {
                        left = false;
                        right = true;
                     }

                     this.playerInput = new PlayerInput(up, down, left, right, event.isJump(), event.isSneak(), event.isSprint());
                  }
               }
            }
         }
      }
   }
}
