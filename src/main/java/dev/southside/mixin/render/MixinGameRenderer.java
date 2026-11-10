package dev.southside.mixin.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.southside.Southside;
import dev.southside.event.events.Render2DEvent;
import dev.southside.event.events.Render3DEvent;
import dev.southside.module.impl.render.NoHurtCam;
import dev.southside.module.impl.render.NoRender;
import dev.southside.render.GL;
import dev.southside.render.MSAA;
import dev.southside.render.ProjectionUtil;
import dev.southside.render.Render3D;
import dev.southside.utils.raytrace.RayCastUtils;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.ObjectAllocator;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {GameRenderer.class},
   priority = 4514
)
public abstract class MixinGameRenderer {
   @Shadow
   @Final
   MinecraftClient client;
   @Shadow
   private long lastWindowFocusedTime;
   @Shadow
   private boolean postProcessorEnabled;
   @Shadow
   @Final
   private BufferBuilderStorage buffers;
   @Shadow
   @Final
   private Camera camera;
   @Unique
   private Matrix4f ss$pos_matrix;

   @Shadow
   public abstract void tick();

   @Shadow
   protected abstract void tiltViewWhenHurt(MatrixStack var1, float var2);

   @Shadow
   protected abstract void bobView(MatrixStack var1, float var2);

   @Redirect(
      method = {"findCrosshairTarget(Lnet/minecraft/entity/Entity;DDF)Lnet/minecraft/util/hit/HitResult;"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/Entity;raycast(DFZ)Lnet/minecraft/util/hit/HitResult;"
      )
   )
   private HitResult hookRaycast(Entity instance, double maxDistance, float tickDelta, boolean includeFluids) {
      if (instance != this.client.player) {
         return instance.raycast(maxDistance, tickDelta, includeFluids);
      } else {
         Rotation rotation = RotationUtils.getRotation() != null
            ? RotationUtils.getRotation()
            : new Rotation(instance.getYaw(tickDelta), instance.getPitch(tickDelta));
         return RayCastUtils.raycast(maxDistance, rotation, includeFluids, tickDelta);
      }
   }

   @Redirect(
      method = {"renderWorld(Lnet/minecraft/client/render/RenderTickCounter;)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/ObjectAllocator;Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V"
      )
   )
   private void onHookMatrix(
      WorldRenderer instance,
      ObjectAllocator allocator,
      RenderTickCounter tickCounter,
      boolean renderBlockOutline,
      Camera camera,
      GameRenderer gameRenderer,
      Matrix4f positionMatrix,
      Matrix4f projectionMatrix
   ) {
      this.ss$pos_matrix = positionMatrix;
      instance.render(allocator, tickCounter, renderBlockOutline, camera, gameRenderer, positionMatrix, projectionMatrix);
   }

   @Inject(
      at = {@At(
         value = "FIELD",
         target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z",
         opcode = 180,
         ordinal = 0
      )},
      method = {"renderWorld(Lnet/minecraft/client/render/RenderTickCounter;)V"}
   )
   void render3dHook(RenderTickCounter renderTickCounter, CallbackInfo ci) {
      ProjectionUtil.updateProjection(this.ss$pos_matrix, RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix());
   }

   @Inject(
      method = {"renderWorld(Lnet/minecraft/client/render/RenderTickCounter;)V"},
      at = {@At(
         value = "INVOKE_STRING",
         target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
         args = {"ldc=hand"}
      )}
   )
   private void onRenderWorld(RenderTickCounter renderTickCounter, CallbackInfo ci) {
      GL.saveRootState();
      MatrixStack mat = new MatrixStack();
      MSAA.use(() -> {
         mat.push();
         Render3D.begin(mat);
         Render3DEvent render3DEvent = new Render3DEvent(this.camera, mat, renderTickCounter.getTickDelta(false));
         Southside.INSTANCE.getEventBus().call(render3DEvent);
         Render3D.end();
         mat.pop();
      });
      GL.restoreRootState();
   }

   @Redirect(
      method = {"renderWorld(Lnet/minecraft/client/render/RenderTickCounter;)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"
      )
   )
   private float renderWorldHook(float delta, float first, float second) {
      return Southside.get().getModuleManager().get(NoRender.class).isEnabled() && Southside.get().getModuleManager().get(NoRender.class).nausea.getValue()
         ? 0.0F
         : MathHelper.lerp(delta, first, second);
   }

   @Inject(
      method = {"tiltViewWhenHurt(Lnet/minecraft/client/util/math/MatrixStack;F)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void camHurt(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
      if (Southside.get() == null || Southside.get().getModuleManager() == null) {
         return;
      }

      NoRender noRender = Southside.get().getModuleManager().get(NoRender.class);
      if (noRender != null && noRender.isEnabled() && noRender.hurtCam.getValue()) {
         ci.cancel();
         return;
      }

      NoHurtCam noHurtCam = Southside.get().getModuleManager().get(NoHurtCam.class);
      if (noHurtCam != null && noHurtCam.shouldCancelTilt()) {
         ci.cancel();
      }
   }

   @Redirect(
      method = {"tiltViewWhenHurt(Lnet/minecraft/client/util/math/MatrixStack;F)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/util/math/RotationAxis;rotationDegrees(F)Lorg/joml/Quaternionf;"
      )
   )
   private Quaternionf hookHurtTiltStrength(RotationAxis axis, float degrees) {
      if (Southside.get() != null && Southside.get().getModuleManager() != null) {
         NoHurtCam noHurtCam = Southside.get().getModuleManager().get(NoHurtCam.class);
         if (noHurtCam != null && noHurtCam.shouldScaleTilt()) {
            return axis.rotationDegrees(degrees * noHurtCam.tiltScale());
         }
      }

      return axis.rotationDegrees(degrees);
   }

   @Inject(
      method = {"bobView(Lnet/minecraft/client/util/math/MatrixStack;F)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookBobView(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
      if (Southside.get() != null && Southside.get().getModuleManager() != null) {
         NoHurtCam noHurtCam = Southside.get().getModuleManager().get(NoHurtCam.class);
         if (noHurtCam != null && noHurtCam.isEnabled() && noHurtCam.noBob.getValue()) {
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"render(Lnet/minecraft/client/render/RenderTickCounter;Z)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(I)V",
         shift = Shift.BEFORE,
         ordinal = 1
      )}
   )
   public void render(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      DrawContext drawContext = new DrawContext(this.client, this.buffers.getEntityVertexConsumers());
      Render2DEvent event = new Render2DEvent(drawContext, tickCounter.getTickDelta(false));
      Southside.INSTANCE.getEventBus().call(event);
      drawContext.draw();
   }
}
