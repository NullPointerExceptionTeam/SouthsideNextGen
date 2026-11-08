package dev.southside.event.events;

import dev.southside.event.CancellableEvent;
import dev.southside.event.Stage;
import lombok.Generated;

public class MotionEvent extends CancellableEvent {
   private double x;
   private double y;
   private double z;
   private float yaw;
   private float pitch;
   private boolean onGround;
   private boolean horizontalCollision;
   private boolean modified = false;
   private Stage state;
   private boolean shouldSendExtraC03;

   public MotionEvent(double x, double y, double z, float yaw, float pitch, boolean onGround, boolean horizontalCollision, Stage state) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.yaw = yaw;
      this.pitch = pitch;
      this.onGround = onGround;
      this.horizontalCollision = horizontalCollision;
      this.state = state;
      this.shouldSendExtraC03 = false;
   }

   public void setYaw(float yaw) {
      this.yaw = yaw;
      this.modified = true;
   }

   public void setPitch(float pitch) {
      this.pitch = pitch;
   }

   @Generated
   public double getX() {
      return this.x;
   }

   @Generated
   public double getY() {
      return this.y;
   }

   @Generated
   public double getZ() {
      return this.z;
   }

   @Generated
   public float getYaw() {
      return this.yaw;
   }

   @Generated
   public float getPitch() {
      return this.pitch;
   }

   @Generated
   public boolean isOnGround() {
      return this.onGround;
   }

   @Generated
   public boolean isHorizontalCollision() {
      return this.horizontalCollision;
   }

   @Generated
   public boolean isModified() {
      return this.modified;
   }

   @Generated
   public Stage getState() {
      return this.state;
   }

   @Generated
   public boolean isShouldSendExtraC03() {
      return this.shouldSendExtraC03;
   }

   @Generated
   public void setX(double x) {
      this.x = x;
   }

   @Generated
   public void setY(double y) {
      this.y = y;
   }

   @Generated
   public void setZ(double z) {
      this.z = z;
   }

   @Generated
   public void setOnGround(boolean onGround) {
      this.onGround = onGround;
   }

   @Generated
   public void setHorizontalCollision(boolean horizontalCollision) {
      this.horizontalCollision = horizontalCollision;
   }

   @Generated
   public void setModified(boolean modified) {
      this.modified = modified;
   }

   @Generated
   public void setState(Stage state) {
      this.state = state;
   }

   @Generated
   public void setShouldSendExtraC03(boolean shouldSendExtraC03) {
      this.shouldSendExtraC03 = shouldSendExtraC03;
   }
}
