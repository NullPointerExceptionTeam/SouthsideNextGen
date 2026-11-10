package dev.southside.event.events;

import lombok.Generated;
import net.minecraft.entity.LivingEntity;

public class RenderLivingEntityLookEvent {
   private LivingEntity entity;
   private float prevBodyYaw;
   private float prevHeadYaw;
   private float prevPitch;
   private float bodyYaw;
   private float headYaw;
   private float pitch;

   @Generated
   public LivingEntity getEntity() {
      return this.entity;
   }

   @Generated
   public float getPrevBodyYaw() {
      return this.prevBodyYaw;
   }

   @Generated
   public float getPrevHeadYaw() {
      return this.prevHeadYaw;
   }

   @Generated
   public float getPrevPitch() {
      return this.prevPitch;
   }

   @Generated
   public float getBodyYaw() {
      return this.bodyYaw;
   }

   @Generated
   public float getHeadYaw() {
      return this.headYaw;
   }

   @Generated
   public float getPitch() {
      return this.pitch;
   }

   @Generated
   public RenderLivingEntityLookEvent(LivingEntity entity, float prevBodyYaw, float prevHeadYaw, float prevPitch, float bodyYaw, float headYaw, float pitch) {
      this.entity = entity;
      this.prevBodyYaw = prevBodyYaw;
      this.prevHeadYaw = prevHeadYaw;
      this.prevPitch = prevPitch;
      this.bodyYaw = bodyYaw;
      this.headYaw = headYaw;
      this.pitch = pitch;
   }

   @Generated
   public void setEntity(LivingEntity entity) {
      this.entity = entity;
   }

   @Generated
   public void setPrevBodyYaw(float prevBodyYaw) {
      this.prevBodyYaw = prevBodyYaw;
   }

   @Generated
   public void setPrevHeadYaw(float prevHeadYaw) {
      this.prevHeadYaw = prevHeadYaw;
   }

   @Generated
   public void setPrevPitch(float prevPitch) {
      this.prevPitch = prevPitch;
   }

   @Generated
   public void setBodyYaw(float bodyYaw) {
      this.bodyYaw = bodyYaw;
   }

   @Generated
   public void setHeadYaw(float headYaw) {
      this.headYaw = headYaw;
   }

   @Generated
   public void setPitch(float pitch) {
      this.pitch = pitch;
   }
}
