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
import net.minecraft.class_1297;
import net.minecraft.class_239;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4599;
import net.minecraft.class_757;
import net.minecraft.class_761;
import net.minecraft.class_7833;
import net.minecraft.class_9779;
import net.minecraft.class_9922;
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
   value = {class_757.class},
   priority = 4514
)
public abstract class MixinGameRenderer {
   @Shadow
   @Final
   class_310 field_4015;
   @Shadow
   private long field_3998;
   @Shadow
   private boolean field_4013;
   @Shadow
   @Final
   private class_4599 field_20948;
   @Shadow
   @Final
   private class_4184 field_18765;
   @Unique
   private Matrix4f ss$pos_matrix;

   @Shadow
   public abstract void method_3182();

   @Shadow
   protected abstract void method_3198(class_4587 var1, float var2);

   @Shadow
   protected abstract void method_3186(class_4587 var1, float var2);

   @Redirect(
      method = {"method_56153(Lnet/minecraft/class_1297;DDF)Lnet/minecraft/class_239;"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1297;method_5745(DFZ)Lnet/minecraft/class_239;"
      )
   )
   private class_239 hookRaycast(class_1297 instance, double maxDistance, float tickDelta, boolean includeFluids) {
      if (instance != this.field_4015.field_1724) {
         return instance.method_5745(maxDistance, tickDelta, includeFluids);
      } else {
         Rotation rotation = RotationUtils.getRotation() != null
            ? RotationUtils.getRotation()
            : new Rotation(instance.method_5705(tickDelta), instance.method_5695(tickDelta));
         return RayCastUtils.raycast(maxDistance, rotation, includeFluids, tickDelta);
      }
   }

   @Redirect(
      method = {"method_3188(Lnet/minecraft/class_9779;)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_761;method_22710(Lnet/minecraft/class_9922;Lnet/minecraft/class_9779;ZLnet/minecraft/class_4184;Lnet/minecraft/class_757;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V"
      )
   )
   private void onHookMatrix(
      class_761 instance,
      class_9922 allocator,
      class_9779 tickCounter,
      boolean renderBlockOutline,
      class_4184 camera,
      class_757 gameRenderer,
      Matrix4f positionMatrix,
      Matrix4f projectionMatrix
   ) {
      this.ss$pos_matrix = positionMatrix;
      instance.method_22710(allocator, tickCounter, renderBlockOutline, camera, gameRenderer, positionMatrix, projectionMatrix);
   }

   @Inject(
      at = {@At(
         value = "FIELD",
         target = "Lnet/minecraft/class_757;field_3992:Z",
         opcode = 180,
         ordinal = 0
      )},
      method = {"method_3188(Lnet/minecraft/class_9779;)V"}
   )
   void render3dHook(class_9779 renderTickCounter, CallbackInfo ci) {
      ProjectionUtil.updateProjection(this.ss$pos_matrix, RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix());
   }

   @Inject(
      method = {"method_3188(Lnet/minecraft/class_9779;)V"},
      at = {@At(
         value = "INVOKE_STRING",
         target = "Lnet/minecraft/class_3695;method_15405(Ljava/lang/String;)V",
         args = {"ldc=hand"}
      )}
   )
   private void onRenderWorld(class_9779 renderTickCounter, CallbackInfo ci) {
      GL.saveRootState();
      class_4587 mat = new class_4587();
      MSAA.use(() -> {
         mat.method_22903();
         Render3D.begin(mat);
         Render3DEvent render3DEvent = new Render3DEvent(this.field_18765, mat, renderTickCounter.method_60637(false));
         Southside.INSTANCE.getEventBus().call(render3DEvent);
         Render3D.end();
         mat.method_22909();
      });
      GL.restoreRootState();
   }

   @Redirect(
      method = {"method_3188(Lnet/minecraft/class_9779;)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_3532;method_16439(FFF)F"
      )
   )
   private float renderWorldHook(float delta, float first, float second) {
      return Southside.get().getModuleManager().get(NoRender.class).isEnabled() && Southside.get().getModuleManager().get(NoRender.class).nausea.getValue()
         ? 0.0F
         : class_3532.method_16439(delta, first, second);
   }

   @Inject(
      method = {"method_3198(Lnet/minecraft/class_4587;F)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void camHurt(class_4587 matrices, float tickDelta, CallbackInfo ci) {
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
      method = {"method_3198(Lnet/minecraft/class_4587;F)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_7833;rotationDegrees(F)Lorg/joml/Quaternionf;"
      )
   )
   private Quaternionf hookHurtTiltStrength(class_7833 axis, float degrees) {
      if (Southside.get() != null && Southside.get().getModuleManager() != null) {
         NoHurtCam noHurtCam = Southside.get().getModuleManager().get(NoHurtCam.class);
         if (noHurtCam != null && noHurtCam.shouldScaleTilt()) {
            return axis.rotationDegrees(degrees * noHurtCam.tiltScale());
         }
      }

      return axis.rotationDegrees(degrees);
   }

   @Inject(
      method = {"method_3186(Lnet/minecraft/class_4587;F)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookBobView(class_4587 matrices, float tickDelta, CallbackInfo ci) {
      if (Southside.get() != null && Southside.get().getModuleManager() != null) {
         NoHurtCam noHurtCam = Southside.get().getModuleManager().get(NoHurtCam.class);
         if (noHurtCam != null && noHurtCam.isEnabled() && noHurtCam.noBob.getValue()) {
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"method_3192(Lnet/minecraft/class_9779;Z)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(I)V",
         shift = Shift.BEFORE,
         ordinal = 1
      )}
   )
   public void render(class_9779 tickCounter, boolean tick, CallbackInfo ci) {
      class_332 drawContext = new class_332(this.field_4015, this.field_20948.method_23000());
      Render2DEvent event = new Render2DEvent(drawContext, tickCounter.method_60637(false));
      Southside.INSTANCE.getEventBus().call(event);
      drawContext.method_51452();
   }
}
