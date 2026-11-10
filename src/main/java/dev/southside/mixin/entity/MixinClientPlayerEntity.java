package dev.southside.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.events.MotionEvent;
import dev.southside.event.events.MoveEvent;
import dev.southside.event.events.PlayerSlowdownEvent;
import dev.southside.event.events.PlayerUpdateEvent;
import dev.southside.event.events.SprintPacketSendEvent;
import dev.southside.module.impl.misc.Disabler;
import dev.southside.utils.network.PacketUtils;
import dev.southside.utils.player.PlayerUtils;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPlayerEntity.class})
public abstract class MixinClientPlayerEntity extends PlayerEntity {
   @Shadow
   public int ticksSinceLastPositionPacketSent;
   @Shadow
   public double lastX;
   @Shadow
   public double lastBaseY;
   @Shadow
   public double lastZ;
   @Shadow
   public float lastYaw;
   @Shadow
   public float lastPitch;
   @Shadow
   public boolean lastOnGround;
   @Shadow
   public boolean lastSprinting;
   @Shadow
   public boolean lastSneaking;
   @Shadow
   private boolean autoJumpEnabled = true;
   @Shadow
   @Final
   public ClientPlayNetworkHandler networkHandler;
   @Shadow
   @Final
   protected MinecraftClient client;
   @Shadow
   private boolean lastHorizontalCollision;

   public MixinClientPlayerEntity(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
      super(world, pos, yaw, gameProfile);
   }

   @Shadow
   public abstract boolean isSneaking();

   @Shadow
   protected abstract boolean isCamera();

   @Inject(
      method = {"tick()V"},
      at = {@At("HEAD")}
   )
   public void tick(CallbackInfo callbackInfo) {
   }

   @Inject(
      method = {"tick()V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V",
         shift = Shift.BEFORE,
         ordinal = 0
      )},
      cancellable = true
   )
   private void hookTickEvent(CallbackInfo ci) {
      PlayerUtils.ticksSinceTeleport++;
      PlayerUpdateEvent event = new PlayerUpdateEvent();
      Southside.INSTANCE.getEventBus().call(event);
      MinecraftClient.getInstance().handleInputEvents();
      if (PlayerUtils.playerStuckTicks > 0) {
         PlayerUtils.playerStuckTicks--;
         ci.cancel();
      }
   }

   @Overwrite
   private void sendMovementPackets() {
      if (!this.isOnGround()) {
         PlayerUtils.offGroundTicks++;
         PlayerUtils.onGroundTicks = 0;
      } else {
         PlayerUtils.offGroundTicks = 0;
         PlayerUtils.onGroundTicks++;
         PlayerUtils.lastOnGroundTicks = PlayerUtils.onGroundTicks;
      }

      this.sendSprintingPacket();
      MotionEvent event = new MotionEvent(
         this.getX(),
         this.getY(),
         this.getZ(),
         this.getYaw(),
         this.getPitch(),
         this.isOnGround(),
         this.horizontalCollision,
         Stage.PRE
      );
      Southside.INSTANCE.getEventBus().call(event);
      float yaw = Disabler.normalizeYawDiff(event.getYaw(), this.lastYaw);
      if (this.isCamera()) {
         double d = event.getX() - this.lastX;
         double e = event.getY() - this.lastBaseY;
         double f = event.getZ() - this.lastZ;
         double g = yaw - this.lastYaw;
         double h = event.getPitch() - this.lastPitch;
         PlayerUtils.lastPitchDiff = Math.abs(h);
         this.ticksSinceLastPositionPacketSent++;
         boolean bl = MathHelper.squaredMagnitude(d, e, f) > MathHelper.square(2.0E-4) || this.ticksSinceLastPositionPacketSent >= 20;
         boolean bl2 = g != 0.0 || h != 0.0;
         if (bl && bl2) {
            this.networkHandler
               .sendPacket(new Full(event.getX(), event.getY(), event.getZ(), yaw, event.getPitch(), event.isOnGround(), event.isHorizontalCollision()));
         } else if (bl) {
            this.networkHandler.sendPacket(new PositionAndOnGround(event.getX(), event.getY(), event.getZ(), event.isOnGround(), event.isHorizontalCollision()));
         } else if (bl2) {
            this.networkHandler.sendPacket(new LookAndOnGround(yaw, event.getPitch(), event.isOnGround(), event.isHorizontalCollision()));
         } else if (this.lastOnGround != event.isOnGround() || this.lastHorizontalCollision != event.isHorizontalCollision()) {
            this.networkHandler.sendPacket(new OnGroundOnly(event.isOnGround(), event.isHorizontalCollision()));
         } else if (event.isShouldSendExtraC03()) {
            this.networkHandler.sendPacket(new OnGroundOnly(event.isOnGround(), event.isHorizontalCollision()));
         } else {
            PacketUtils.callPacket(new OnGroundOnly(event.isOnGround(), event.isHorizontalCollision()));
         }

         Disabler.processPackets();
         if (bl) {
            this.lastX = event.getX();
            this.lastBaseY = event.getY();
            this.lastZ = event.getZ();
            this.ticksSinceLastPositionPacketSent = 0;
         }

         if (bl2) {
            this.lastYaw = yaw;
            this.lastPitch = event.getPitch();
         }

         this.lastOnGround = event.isOnGround();
         this.lastHorizontalCollision = event.isHorizontalCollision();
         this.autoJumpEnabled = (Boolean)this.client.options.getAutoJump().getValue();
         event.setState(Stage.POST);
         Southside.INSTANCE.getEventBus().call(event);
      }
   }

   @ModifyExpressionValue(
      method = {"tickMovement()V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"
      )}
   )
   private boolean redirectUsingItem(boolean isUsingItem) {
      PlayerSlowdownEvent event = new PlayerSlowdownEvent();
      Southside.INSTANCE.getEventBus().call(event);
      return event.isCancelled() ? false : isUsingItem;
   }

   @Inject(
      method = {"move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"
      )},
      cancellable = true
   )
   public void onMoveHook(MovementType movementType, Vec3d movement, CallbackInfo ci) {
      MoveEvent event = new MoveEvent(movement.x, movement.y, movement.z);
      Southside.INSTANCE.getEventBus().call(event);
      if (event.isCancelled()) {
         super.move(movementType, new Vec3d(event.getX(), event.getY(), event.getZ()));
         ci.cancel();
      }
   }

   @Overwrite
   private void sendSprintingPacket() {
      SprintPacketSendEvent event = new SprintPacketSendEvent(this.isSprinting());
      Southside.INSTANCE.getEventBus().call(event);
      if (!event.isCancelled()) {
         boolean bl = event.isSprinting();
         if (bl != this.lastSprinting) {
            Mode mode = bl ? Mode.START_SPRINTING : Mode.STOP_SPRINTING;
            this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, mode));
            this.lastSprinting = bl;
         }
      }
   }
}
