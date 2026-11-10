package dev.southside.utils.player;

import dev.southside.event.events.MovementInputEvent;
import lombok.Generated;
import net.minecraft.client.input.Input;

public class DirectionalInput {
   private boolean forward;
   private boolean back;
   private boolean left;
   private boolean right;
   private boolean sneak;
   private boolean jump;

   public static DirectionalInput from(MovementInputEvent e) {
      return new DirectionalInput(e.isForward(), e.isBack(), e.isLeft(), e.isRight(), e.isSneak(), e.isJump());
   }

   public static DirectionalInput from(Input i) {
      return new DirectionalInput(
         i.playerInput.forward(),
         i.playerInput.backward(),
         i.playerInput.left(),
         i.playerInput.right(),
         i.playerInput.sneak(),
         i.playerInput.jump()
      );
   }

   @Generated
   public DirectionalInput(boolean forward, boolean back, boolean left, boolean right, boolean sneak, boolean jump) {
      this.forward = forward;
      this.back = back;
      this.left = left;
      this.right = right;
      this.sneak = sneak;
      this.jump = jump;
   }

   @Generated
   public boolean isForward() {
      return this.forward;
   }

   @Generated
   public boolean isBack() {
      return this.back;
   }

   @Generated
   public boolean isLeft() {
      return this.left;
   }

   @Generated
   public boolean isRight() {
      return this.right;
   }

   @Generated
   public boolean isSneak() {
      return this.sneak;
   }

   @Generated
   public boolean isJump() {
      return this.jump;
   }

   @Generated
   public void setForward(boolean forward) {
      this.forward = forward;
   }

   @Generated
   public void setBack(boolean back) {
      this.back = back;
   }

   @Generated
   public void setLeft(boolean left) {
      this.left = left;
   }

   @Generated
   public void setRight(boolean right) {
      this.right = right;
   }

   @Generated
   public void setSneak(boolean sneak) {
      this.sneak = sneak;
   }

   @Generated
   public void setJump(boolean jump) {
      this.jump = jump;
   }
}
