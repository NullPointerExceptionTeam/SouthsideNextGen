package dev.southside.mixin.input;

import dev.southside.Southside;
import dev.southside.event.events.MovementInputEvent;
import dev.southside.event.events.PostMoveInput;
import dev.southside.utils.player.MovementUtils;
import dev.southside.utils.player.PlayerUtils;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import java.util.ArrayList;
import net.minecraft.class_10185;
import net.minecraft.class_310;
import net.minecraft.class_315;
import net.minecraft.class_743;
import net.minecraft.class_744;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_743.class})
public abstract class MixinKeyboardInput extends class_744 {
   @Unique
   private final ArrayList<Rotation> possible = new ArrayList<>();
   @Shadow
   @Final
   private class_315 field_3902;

   @Shadow
   private static float method_40218(boolean positive, boolean negative) {
      return 0.0F;
   }

   @Inject(
      method = {"method_3129()V"},
      at = {@At("TAIL")},
      allow = 1
   )
   private void injectMovementInputEvent(CallbackInfo ci) {
      this.field_54155 = new class_10185(
         this.field_3902.field_1894.method_1434(),
         this.field_3902.field_1881.method_1434(),
         this.field_3902.field_1913.method_1434(),
         this.field_3902.field_1849.method_1434(),
         this.field_3902.field_1903.method_1434(),
         this.field_3902.field_1832.method_1434(),
         this.field_3902.field_1867.method_1434()
      );
      this.field_3905 = method_40218(this.field_54155.comp_3159(), this.field_54155.comp_3160());
      this.field_3907 = method_40218(this.field_54155.comp_3161(), this.field_54155.comp_3162());
      MovementInputEvent event = Southside.INSTANCE
         .getEventBus()
         .post(
            new MovementInputEvent(
               this.field_54155.comp_3159(),
               this.field_54155.comp_3160(),
               this.field_54155.comp_3161(),
               this.field_54155.comp_3162(),
               this.field_54155.comp_3164(),
               this.field_54155.comp_3163(),
               this.field_54155.comp_3165(),
               false
            )
         );

      RotationUtils.setSilentFixSkipped(event.isSkipSilent());
      this.field_54155 = new class_10185(event.isForward(), event.isBack(), event.isLeft(), event.isRight(), event.isJump(), event.isSneak(), event.isSprint());
      this.field_3905 = method_40218(this.field_54155.comp_3159(), this.field_54155.comp_3160());
      this.field_3907 = method_40218(this.field_54155.comp_3161(), this.field_54155.comp_3162());
      this.fixStrafeMovement(event);
      this.field_3905 = method_40218(this.field_54155.comp_3159(), this.field_54155.comp_3160());
      this.field_3907 = method_40218(this.field_54155.comp_3161(), this.field_54155.comp_3162());
      if (MovementUtils.consumeInputHold()) {
         this.field_54155 = new class_10185(
            false, false, false, false, this.field_54155.comp_3163(), this.field_54155.comp_3164(), this.field_54155.comp_3165()
         );
         this.field_3905 = method_40218(this.field_54155.comp_3159(), this.field_54155.comp_3160());
         this.field_3907 = method_40218(this.field_54155.comp_3161(), this.field_54155.comp_3162());
      }
      Southside.INSTANCE.getEventBus().post(new PostMoveInput());
   }

   @Unique
   private void fixStrafeMovement(MovementInputEvent event) {
      if (!event.isSkipSilent()) {
         class_746 player = class_310.method_1551().field_1724;
         if (player != null) {
            if (!RotationUtils.isStrictFix()) {
               if (RotationUtils.isStrafeFix()) {
                  if (RotationUtils.getRotation() != null) {
                     Rotation rotation = RotationUtils.getRotationSafely();
                     float forward = this.field_3905;
                     float strafe = this.field_3907;
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

                     this.field_54155 = new class_10185(up, down, left, right, event.isJump(), event.isSneak(), event.isSprint());
                  }
               }
            }
         }
      }
   }
}
