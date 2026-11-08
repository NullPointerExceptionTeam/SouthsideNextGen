package dev.southside.event.events;

import dev.southside.event.CancellableEvent;
import lombok.Generated;

public class MovementInputEvent extends CancellableEvent {
   private boolean forward;
   private boolean back;
   private boolean left;
   private boolean right;
   private boolean sneak;
   private boolean jump;
   private boolean sprint;
   private boolean skipSilent;

   @Generated
   public MovementInputEvent(boolean forward, boolean back, boolean left, boolean right, boolean sneak, boolean jump, boolean sprint, boolean skipSilent) {
      this.forward = forward;
      this.back = back;
      this.left = left;
      this.right = right;
      this.sneak = sneak;
      this.jump = jump;
      this.sprint = sprint;
      this.skipSilent = skipSilent;
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
   public boolean isSprint() {
      return this.sprint;
   }

   @Generated
   public boolean isSkipSilent() {
      return this.skipSilent;
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

   @Generated
   public void setSprint(boolean sprint) {
      this.sprint = sprint;
   }

   @Generated
   public void setSkipSilent(boolean skipSilent) {
      this.skipSilent = skipSilent;
   }
}
