package cc.polyfrost.oneconfig.internal.gui.impl;

import cc.polyfrost.oneconfig.internal.config.Preferences;
import cc.polyfrost.oneconfig.internal.gui.BlurHandler;
import cc.polyfrost.oneconfig.libs.universal.UMinecraft;
import cc.polyfrost.oneconfig.utils.gui.BlurScreen;
import dev.southside.event.Stage;
import dev.southside.event.events.GameRendererDrawEvent;
import dev.southside.event.events.SetScreenEvent;
import dev.southside.mixin.accessors.IGameRenderer;
import dev.southside.orbit.EventHandler;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.DefaultFramebufferSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BlurHandlerImpl implements BlurHandler {
   private static final Identifier BLUR_SHADER = Identifier.of("southside:blur");
   private static final float FADE_SPEED_BASE_FPS = 60.0F;
   private static final float MAX_ANIMATION_DELTA_SECONDS = 0.25F;
   private static final float MIN_RADIUS_THRESHOLD = 0.01F;
   private static final float MIN_FADE_STEP = 0.005F;
   private static final float MIN_SAMPLE_RATE = 1.0F;
   private static final float RADIUS_APPLY_EPSILON = 0.01F;
   private static final float SAMPLE_RATE_APPLY_EPSILON = 0.01F;
   private static final long NANOS_PER_SECOND = 1000000000L;
   private static final long ERROR_LOG_COOLDOWN_MS = 3000L;
   private final Logger logger = LogManager.getLogger("OneConfig - Blur");
   private boolean shouldBlur;
   private float animatedRadius;
   private float lastAppliedEffectiveRadius = -1.0F;
   private float lastAppliedSampleRate = -1.0F;
   private PostEffectProcessor cachedProcessor;
   private long lastAnimationStepNanos = Long.MIN_VALUE;
   private long nextErrorLogAt;

   @EventHandler
   private void onGuiChange(SetScreenEvent event) {
      this.reloadBlur(event.screen);
   }

   @EventHandler
   private void onRenderTick(GameRendererDrawEvent event) {
      if (event.stage == Stage.PRE) {
         long nowNanos = System.nanoTime();
         if (UMinecraft.getWorld() == null) {
            this.shouldBlur = false;
            this.animatedRadius = 0.0F;
            this.lastAppliedEffectiveRadius = -1.0F;
            this.lastAppliedSampleRate = -1.0F;
            this.cachedProcessor = null;
            this.lastAnimationStepNanos = Long.MIN_VALUE;
            this.disableBlurShader("world not ready");
         } else if (this.shouldBlur || !(this.animatedRadius <= 0.01F) || this.isOwnBlurShaderActive()) {
            Preferences preferences = Preferences.getInstance();
            float maxBlurRadius = Math.max(0.0F, preferences.blurMaxRadius.getValue().floatValue());
            float fadeSpeedPerSecond = Math.max(0.005F, preferences.blurFadeStep.getValue().floatValue()) * 60.0F;
            float sampleRate = Math.max(1.0F, preferences.blurSamplingScale.getValue().floatValue());
            float targetRadius = this.shouldBlur ? maxBlurRadius : 0.0F;
            float deltaSeconds = this.computeAnimationDeltaSeconds(nowNanos);
            float maxAnimationStep = fadeSpeedPerSecond * deltaSeconds;
            this.animatedRadius = this.moveTowards(this.animatedRadius, targetRadius, maxAnimationStep);
            if (this.animatedRadius <= 0.01F) {
               this.animatedRadius = 0.0F;
               if (!this.shouldBlur) {
                  this.lastAppliedEffectiveRadius = -1.0F;
                  this.lastAppliedSampleRate = -1.0F;
                  this.cachedProcessor = null;
                  this.disableBlurShader("fade finished");
               }
            } else {
               label49:
               if (this.isOwnBlurShaderActive()) {
                  float effectiveRadius = Math.max(this.animatedRadius, 0.01F);
                  boolean radiusUnchanged = Math.abs(effectiveRadius - this.lastAppliedEffectiveRadius) < 0.01F;
                  boolean sampleRateUnchanged = Math.abs(sampleRate - this.lastAppliedSampleRate) < 0.01F;
                  if (!radiusUnchanged || !sampleRateUnchanged) {
                     this.updateBlurUniforms(effectiveRadius, sampleRate);
                  }
               } else if (!this.isAnyShaderActive()) {
                  try {
                     ((IGameRenderer)UMinecraft.getMinecraft().gameRenderer).invokeLoadPostProcessor(BLUR_SHADER);
                     this.cachedProcessor = null;
                     this.lastAppliedEffectiveRadius = -1.0F;
                     this.lastAppliedSampleRate = -1.0F;
                     break label49;
                  } catch (Exception var14) {
                     this.logErrorThrottled("Failed to re-load blur shader during fade update", var14);
                  }
               }
            }
         }
      }
   }

   @Override
   public void reloadBlur(Object gui) {
      if (UMinecraft.getWorld() == null) {
         this.shouldBlur = false;
         this.animatedRadius = 0.0F;
         this.lastAppliedEffectiveRadius = -1.0F;
         this.lastAppliedSampleRate = -1.0F;
         this.cachedProcessor = null;
         this.lastAnimationStepNanos = Long.MIN_VALUE;
         this.disableBlurShader("world not ready");
      } else {
         this.lastAppliedEffectiveRadius = -1.0F;
         this.lastAppliedSampleRate = -1.0F;
         boolean blurEnabled = Preferences.getInstance().enableBlur.getValue();
         this.shouldBlur = blurEnabled && gui instanceof BlurScreen blurScreen && blurScreen.hasBackgroundBlur();
         if (this.shouldBlur) {
            if (!this.isAnyShaderActive()) {
               try {
                  this.logger.info("Attempting to load blur shader: {}", BLUR_SHADER);
                  ((IGameRenderer)UMinecraft.getMinecraft().gameRenderer).invokeLoadPostProcessor(BLUR_SHADER);
                  this.cachedProcessor = null;
                  this.lastAppliedEffectiveRadius = -1.0F;
                  this.lastAppliedSampleRate = -1.0F;
                  this.lastAnimationStepNanos = Long.MIN_VALUE;
                  if (this.isOwnBlurShaderActive()) {
                     this.logger.info("Blur shader loaded successfully");
                  } else {
                     this.logger.warn("Blur shader failed to load - no active OneConfig blur detected");
                  }
               } catch (Exception var4) {
                  this.logErrorThrottled("Failed to load blur shader: " + BLUR_SHADER, var4);
                  this.disableBlurShader("failed to load");
               }
            }
         } else {
            if (!blurEnabled) {
               this.logger.info("Blur preference disabled, fading out");
            }
         }
      }
   }

   private boolean isAnyShaderActive() {
      return UMinecraft.getMinecraft().gameRenderer.getPostProcessorId() != null;
   }

   private boolean isOwnBlurShaderActive() {
      Identifier active = UMinecraft.getMinecraft().gameRenderer.getPostProcessorId();
      return active != null && active.equals(BLUR_SHADER);
   }

   private void disableBlurShader(String reason) {
      if (this.isOwnBlurShaderActive()) {
         try {
            ((IGameRenderer)UMinecraft.getMinecraft().gameRenderer).invokeClearPostProcessor();
            this.cachedProcessor = null;
            this.lastAppliedEffectiveRadius = -1.0F;
            this.lastAppliedSampleRate = -1.0F;
            this.lastAnimationStepNanos = Long.MIN_VALUE;
            this.logger.info("Blur shader disabled ({})", reason);
         } catch (Exception var3) {
            this.logErrorThrottled("Error disabling blur shader (" + reason + ")", var3);
         }
      }
   }

   private float computeAnimationDeltaSeconds(long nowNanos) {
      if (this.lastAnimationStepNanos == Long.MIN_VALUE) {
         this.lastAnimationStepNanos = nowNanos;
         return 0.0F;
      } else {
         long deltaNanos = nowNanos - this.lastAnimationStepNanos;
         this.lastAnimationStepNanos = nowNanos;
         return deltaNanos <= 0L ? 0.0F : Math.min(0.25F, (float)deltaNanos / 1.0E9F);
      }
   }

   private float moveTowards(float current, float target, float maxDelta) {
      if (maxDelta <= 0.0F || current == target) {
         return current;
      } else {
         return current < target ? Math.min(target, current + maxDelta) : Math.max(target, current - maxDelta);
      }
   }

   private void updateBlurUniforms(float radius, float sampleRate) {
      if (this.isOwnBlurShaderActive()) {
         try {
            PostEffectProcessor processor = this.cachedProcessor;
            if (processor == null) {
               processor = UMinecraft.getMinecraft().getShaderLoader().loadPostEffect(BLUR_SHADER, DefaultFramebufferSet.MAIN_ONLY);
               this.cachedProcessor = processor;
            }

            if (processor != null) {
               processor.setUniforms("Radius", radius);
               processor.setUniforms("SampleRate", sampleRate);
               processor.setUniforms("DualSampleEnabled", 0.0F);
               this.lastAppliedEffectiveRadius = radius;
               this.lastAppliedSampleRate = sampleRate;
            }
         } catch (Exception var4) {
            this.cachedProcessor = null;
            this.logErrorThrottled("Failed to update blur uniforms: radius=" + radius + ", sampleRate=" + sampleRate, var4);
         }
      }
   }

   private void logErrorThrottled(String message, Exception e) {
      long now = System.currentTimeMillis();
      if (now >= this.nextErrorLogAt) {
         this.logger.error(message, e);
         this.nextErrorLogAt = now + 3000L;
      }
   }
}
