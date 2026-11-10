package dev.southside.utils.player;

import com.mojang.authlib.GameProfile;
import dev.southside.MinecraftAccessor;
import java.util.UUID;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity.RemovalReason;

public class FakePlayerEntity extends OtherClientPlayerEntity {
   public boolean doNotPush;
   public boolean hideWhenInsideCamera;

   public FakePlayerEntity(PlayerEntity player, String name, float health, boolean copyInv) {
      super(MinecraftAccessor.mc.world, new GameProfile(UUID.randomUUID(), name));
      this.copyPositionAndRotation(player);
      this.prevYaw = this.getYaw();
      this.prevPitch = this.getPitch();
      this.headYaw = player.headYaw;
      this.prevHeadYaw = this.headYaw;
      this.bodyYaw = player.bodyYaw;
      this.prevBodyYaw = this.bodyYaw;
      Byte playerModel = (Byte)player.getDataTracker().get(PlayerEntity.PLAYER_MODEL_PARTS);
      this.dataTracker.set(PlayerEntity.PLAYER_MODEL_PARTS, playerModel);
      this.getAttributes().setFrom(player.getAttributes());
      this.setPose(player.getPose());
      this.capeX = this.getX();
      this.capeY = this.getY();
      this.capeZ = this.getZ();
      if (health <= 20.0F) {
         this.setHealth(health);
      } else {
         this.setHealth(health);
         this.setAbsorptionAmount(health - 20.0F);
      }

      if (copyInv) {
         this.getInventory().clone(player.getInventory());
      }
   }

   public void spawn() {
      if (MinecraftAccessor.mc.world != null) {
         this.unsetRemoved();
         MinecraftAccessor.mc.world.addEntity(this);
      }
   }

   public void despawn() {
      if (MinecraftAccessor.mc.world != null) {
         MinecraftAccessor.mc.world.removeEntity(this.getId(), RemovalReason.DISCARDED);
         this.setRemoved(RemovalReason.DISCARDED);
      }
   }
}
