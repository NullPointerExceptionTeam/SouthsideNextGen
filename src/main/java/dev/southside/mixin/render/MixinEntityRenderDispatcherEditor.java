package dev.southside.mixin.render;

import dev.southside.module.impl.world.EntityEditor;
import java.util.Map;
import net.minecraft.client.render.entity.state.CamelEntityRenderState;
import net.minecraft.client.render.entity.state.CreeperEntityRenderState;
import net.minecraft.client.render.entity.state.DonkeyEntityRenderState;
import net.minecraft.client.render.entity.state.IronGolemEntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PigEntityRenderState;
import net.minecraft.client.render.entity.state.SheepEntityRenderState;
import net.minecraft.client.render.entity.state.SkeletonEntityRenderState;
import net.minecraft.client.render.entity.state.WolfEntityRenderState;
import net.minecraft.client.render.entity.state.ZombieEntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.EntityAttachmentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({EntityRenderDispatcher.class})
public abstract class MixinEntityRenderDispatcherEditor {
   @Shadow
   private Map<EntityType<?>, EntityRenderer<?, ?>> renderers;
   @Shadow
   public Camera camera;

   @Shadow
   public abstract <T extends Entity> EntityRenderer<? super T, ?> getRenderer(T var1);

   @Inject(
      method = {"render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private <E extends Entity> void onRenderEntity(
      E entity, double x, double y, double z, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci
   ) {
      if (EntityEditor.shouldReplace(entity)) {
         EntityType<?> targetType = EntityEditor.getTargetEntityType();
         EntityRenderer<?, ?> animalRenderer = this.renderers.get(targetType);
         if (animalRenderer != null) {
            try {
               this.southside$renderAsAnimal(entity, x, y, z, tickDelta, matrices, vertexConsumers, light, animalRenderer);
               ci.cancel();
            } catch (Exception var16) {
            }
         }
      }
   }

   @Unique
   private void southside$renderAsAnimal(
      Entity entity, double x, double y, double z, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, EntityRenderer animalRenderer
   ) {
      LivingEntityRenderState state = this.southside$createAnimalState();
      state.x = MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX());
      state.y = MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY());
      state.z = MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ());
      state.invisible = entity.isInvisible();
      state.age = entity.age + tickDelta;
      state.width = entity.getWidth();
      state.height = entity.getHeight();
      state.standingEyeHeight = entity.getStandingEyeHeight();
      state.squaredDistanceToCamera = entity.squaredDistanceTo(MinecraftClient.getInstance().gameRenderer.getCamera().getPos());
      state.sneaking = entity.isSneaky();
      state.onFire = entity.doesRenderOnFire();
      if (entity instanceof LivingEntity living) {
         state.bodyYaw = MathHelper.lerpAngleDegrees(tickDelta, living.prevBodyYaw, living.bodyYaw);
         state.yawDegrees = MathHelper.lerpAngleDegrees(tickDelta, living.prevYaw, living.getYaw());
         state.pitch = MathHelper.lerp(tickDelta, living.prevPitch, living.getPitch());
         state.baseScale = living.getScale();
         state.ageScale = 1.0F;
         state.baby = living.isBaby();
         state.hurt = living.hurtTime > 0;
         state.deathTime = living.deathTime;
         state.limbFrequency = living.limbAnimator.getSpeed(tickDelta);
         state.limbAmplitudeMultiplier = living.limbAnimator.getPos(tickDelta);
         state.touchingWater = living.isTouchingWater();
      } else {
         state.bodyYaw = MathHelper.lerpAngleDegrees(tickDelta, entity.prevYaw, entity.getYaw());
         state.yawDegrees = state.bodyYaw;
         state.pitch = MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch());
         state.baseScale = 1.0F;
         state.ageScale = 1.0F;
      }

      if (entity instanceof PlayerEntity player) {
         String name = player.getGameProfile() != null ? player.getGameProfile().getName() : player.getDisplayName().getString();
         state.displayName = Text.literal(name);
      } else {
         state.displayName = entity.getType().getName();
      }

      Vec3d namePos = entity.getAttachments().getPointNullable(EntityAttachmentType.NAME_TAG, 0, entity.getLerpedYaw(tickDelta));
      state.nameLabelPos = namePos;
      matrices.push();

      try {
         Vec3d posOffset = animalRenderer.getPositionOffset(state);
         double dx = x + posOffset.getX();
         double dy = y + posOffset.getY();
         double dz = z + posOffset.getZ();
         matrices.translate(dx, dy, dz);
         animalRenderer.render(state, matrices, vertexConsumers, light);
         matrices.translate(-posOffset.getX(), -posOffset.getY(), -posOffset.getZ());
      } finally {
         matrices.pop();
      }
   }

   @Unique
   private LivingEntityRenderState southside$createAnimalState() {
      int modelIndex = EntityEditor.getModelIndex();

      return (LivingEntityRenderState)(switch (modelIndex) {
         case 1 -> new SheepEntityRenderState();
         case 2 -> new WolfEntityRenderState();
         case 3 -> new CamelEntityRenderState();
         case 4 -> new DonkeyEntityRenderState();
         case 5 -> new CreeperEntityRenderState();
         case 6 -> new SkeletonEntityRenderState();
         case 7 -> new ZombieEntityRenderState();
         case 8 -> new LivingEntityRenderState();
         case 9 -> new IronGolemEntityRenderState();
         default -> new PigEntityRenderState();
      });
   }
}
