package dev.southside.gui.hud;

import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.renderer.scissor.Scissor;
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper;
import dev.southside.MinecraftAccessor;
import dev.southside.module.impl.render.PotionEffects;
import dev.southside.utils.misc.BezierUtil;
import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.registry.entry.RegistryEntry;

public class PotionHud extends Hud {
   private final HashMap<String, PotionHud.PotionEffectComponent> potionEffects = new HashMap<>();
   private final BezierUtil height = new BezierUtil(4.0F, 200.0F);

   public PotionHud(float x, float y, int positionAlignment, float scale) {
      super(x, y, positionAlignment, scale);
   }

   @Override
   protected void draw(DrawContext context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
      Collection<StatusEffectInstance> effects = mc.player.getStatusEffects();
      if (!(this.height.get() < 1.0F) || example || !effects.isEmpty()) {
         ScissorHelper scissorHelper = ScissorHelper.INSTANCE;
         NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
         nanoVGHelper.setupAndDraw(true, vg -> {
            float potionY = 0.0F;
            float totalWidth = 150.0F * scale;
            Scissor scissor = scissorHelper.scissor(vg, x, y, totalWidth, this.height.get());
            if (!example) {
               int size = effects.size();

               for (int i = 0; i < size; i++) {
                  StatusEffectInstance e = effects.stream().toList().get(i);
                  String name = ((StatusEffect)e.getEffectType().value()).getName().getString();
                  if (!this.potionEffects.containsKey(name)) {
                     this.potionEffects.put(name, new PotionHud.PotionEffectComponent(e, e.getDuration()));
                  }

                  PotionHud.PotionEffectComponent componentxx = this.potionEffects.get(name);
                  componentxx.effect = e;
                  componentxx.y.update(potionY);
                  componentxx.draw(vg, x, y, scale);
                  potionY += (PotionEffects.style.isMode("Simple") ? 46.0F : 37.5F) * scale;
               }

               for (PotionHud.PotionEffectComponent componentx : this.potionEffects.values()) {
                  if (!effects.contains(componentx.effect)) {
                     componentx.y.update(-75.0F * scale);
                     componentx.draw(vg, x, y, scale);
                  }
               }

               if (potionY > 0.0F) {
                  this.height.update(potionY);
               } else {
                  this.height.update(0.0F);
               }
            }

            scissorHelper.resetScissor(vg, scissor);
         });
         if (!example) {
            context.enableScissor((int)x, (int)y, (int)(x + 150.0F * scale), (int)(y + this.height.get()));

            for (PotionHud.PotionEffectComponent component : this.potionEffects.values()) {
               component.drawIcon(context, x, y, scale);
            }

            context.disableScissor();
         }
      }
   }

   @Override
   protected float getWidth(float scale, boolean example) {
      return 150.0F * scale;
   }

   @Override
   protected float getHeight(float scale, boolean example) {
      return this.height == null ? 37.5F * scale : this.height.target;
   }

   public static class PotionEffectComponent {
      private final BezierUtil y = new BezierUtil(3.0F, 0.0F);
      private static final float height = 30.0F;
      public int maxDuration;
      public StatusEffectInstance effect;

      public PotionEffectComponent(StatusEffectInstance effect, int dur) {
         this.effect = effect;
         this.maxDuration = dur;
      }

      public void drawIcon(DrawContext context, float x, float yOffset, float scale) {
         if (!(this.y.get() < -30.0F)) {
            float y = this.y.get() + yOffset;
            StatusEffectSpriteManager statusEffectSpriteManager = MinecraftAccessor.mc.getStatusEffectSpriteManager();
            RegistryEntry<StatusEffect> statusEffect = this.effect.getEffectType();
            Sprite sprite = statusEffectSpriteManager.getSprite(statusEffect);
            MatrixStack matrix = context.getMatrices();
            matrix.push();
            matrix.translate(x, y, 0.0F);
            matrix.scale(scale * 1.25F, scale * 1.25F, scale * 1.25F);
            context.drawSpriteStretched(RenderLayer::getGuiTextured, sprite, 5, 5, 18, 18);
            matrix.pop();
         }
      }

      public void draw(long vg, float x, float yOffset, float scale) {
         if (!(this.y.get() < -30.0F) && MinecraftAccessor.mc.player != null) {
            float y = this.y.get() + yOffset;
            NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
            String duration = null;
            if (MinecraftAccessor.mc.world != null) {
               duration = StatusEffectUtil.getDurationText(this.effect, 1.0F, MinecraftAccessor.mc.world.getTickManager().getTickRate()).getString();
            }

            if (PotionEffects.style.isMode("Simple")) {
               this.drawSimple(vg, nanoVGHelper, x, y, scale, duration);
            } else {
               float progressBarWidth = 150.0F * scale * (1.0F * this.effect.getDuration() / this.maxDuration);
               float progressBarHeight = 36.5F * scale;
               if (MinecraftAccessor.mc.player.getStatusEffects().size() == 1) {
                  float rad = 5.0F * scale;
                  ScissorHelper scissorHelper = ScissorHelper.INSTANCE;
                  Scissor scissor = scissorHelper.scissor(vg, x + progressBarWidth, y + scale, 150.0F * scale - progressBarWidth, progressBarHeight);
                  nanoVGHelper.drawRoundedRectVaried(vg, x, y + scale, 150.0F * scale, 36.5F * scale, new Color(0, 0, 0, 77).getRGB(), rad, rad, rad, rad);
                  nanoVGHelper.drawText(
                     vg,
                     this.getStatusEffectDescription(this.effect).getString(),
                     x + 32.5F * scale,
                     y + 18.75F * scale,
                     Color.WHITE.getRGB(),
                     11.25F * scale,
                     Fonts.SEMIBOLD
                  );
                  nanoVGHelper.drawText(
                     vg,
                     duration,
                     x + 147.5F * scale - nanoVGHelper.getTextWidth(vg, duration, 8.75F * scale, Fonts.REGULAR),
                     y + 31.25F * scale,
                     Color.WHITE.getRGB(),
                     8.75F * scale,
                     Fonts.REGULAR
                  );
                  scissorHelper.resetScissor(vg, scissor);
                  Scissor scissor1 = scissorHelper.scissor(vg, x, y + scale, progressBarWidth, progressBarHeight);
                  nanoVGHelper.drawRoundedRectVaried(
                     vg, x, y + scale, 150.0F * scale, 36.5F * scale, new Color(255, 255, 255, 135).getRGB(), rad, rad, rad, rad
                  );
                  nanoVGHelper.drawText(
                     vg,
                     this.getStatusEffectDescription(this.effect).getString(),
                     x + 32.5F * scale,
                     y + 18.75F * scale,
                     Color.BLACK.getRGB(),
                     11.25F * scale,
                     Fonts.SEMIBOLD
                  );
                  nanoVGHelper.drawText(
                     vg,
                     duration,
                     x + 147.5F * scale - nanoVGHelper.getTextWidth(vg, duration, 8.75F * scale, Fonts.REGULAR),
                     y + 31.25F * scale,
                     Color.BLACK.getRGB(),
                     8.75F * scale,
                     Fonts.REGULAR
                  );
                  scissorHelper.resetScissor(vg, scissor1);
               } else if (!(y + progressBarHeight >= yOffset + 35.5F * scale * MinecraftAccessor.mc.player.getStatusEffects().size())
                  && !(y <= yOffset + 35.5F * scale)) {
                  ScissorHelper scissorHelper = ScissorHelper.INSTANCE;
                  Scissor scissor = scissorHelper.scissor(vg, x + progressBarWidth, y + scale, 150.0F * scale - progressBarWidth, progressBarHeight);
                  nanoVGHelper.drawRect(vg, x, y + scale, 150.0F * scale, 36.5F * scale, new Color(0, 0, 0, 77).getRGB());
                  nanoVGHelper.drawText(
                     vg,
                     this.getStatusEffectDescription(this.effect).getString(),
                     x + 32.5F * scale,
                     y + 18.75F * scale,
                     Color.WHITE.getRGB(),
                     11.25F * scale,
                     Fonts.SEMIBOLD
                  );
                  nanoVGHelper.drawText(
                     vg,
                     duration,
                     x + 147.5F * scale - nanoVGHelper.getTextWidth(vg, duration, 8.75F * scale, Fonts.REGULAR),
                     y + 31.25F * scale,
                     Color.WHITE.getRGB(),
                     8.75F * scale,
                     Fonts.REGULAR
                  );
                  scissorHelper.resetScissor(vg, scissor);
                  Scissor scissor1 = scissorHelper.scissor(vg, x, y + scale, progressBarWidth, progressBarHeight);
                  nanoVGHelper.drawRect(vg, x, y + scale, 150.0F * scale, 36.5F * scale, new Color(255, 255, 255, 135).getRGB());
                  nanoVGHelper.drawText(
                     vg,
                     this.getStatusEffectDescription(this.effect).getString(),
                     x + 32.5F * scale,
                     y + 18.75F * scale,
                     Color.BLACK.getRGB(),
                     11.25F * scale,
                     Fonts.SEMIBOLD
                  );
                  nanoVGHelper.drawText(
                     vg,
                     duration,
                     x + 147.5F * scale - nanoVGHelper.getTextWidth(vg, duration, 8.75F * scale, Fonts.REGULAR),
                     y + 31.25F * scale,
                     Color.BLACK.getRGB(),
                     8.75F * scale,
                     Fonts.REGULAR
                  );
                  scissorHelper.resetScissor(vg, scissor1);
               } else {
                  float rad = 5.0F * scale;
                  boolean top = y + progressBarHeight <= yOffset + 35.5F * scale * MinecraftAccessor.mc.player.getStatusEffects().size();
                  ScissorHelper scissorHelper = ScissorHelper.INSTANCE;
                  Scissor scissor = scissorHelper.scissor(vg, x + progressBarWidth, y + scale, 150.0F * scale - progressBarWidth, progressBarHeight);
                  nanoVGHelper.drawRoundedRectVaried(
                     vg,
                     x,
                     y + scale,
                     150.0F * scale,
                     36.5F * scale,
                     new Color(0, 0, 0, 77).getRGB(),
                     top ? rad : 0.0F,
                     top ? rad : 0.0F,
                     top ? 0.0F : rad,
                     top ? 0.0F : rad
                  );
                  nanoVGHelper.drawText(
                     vg,
                     this.getStatusEffectDescription(this.effect).getString(),
                     x + 32.5F * scale,
                     y + 18.75F * scale,
                     Color.WHITE.getRGB(),
                     11.25F * scale,
                     Fonts.SEMIBOLD
                  );
                  nanoVGHelper.drawText(
                     vg,
                     duration,
                     x + 147.5F * scale - nanoVGHelper.getTextWidth(vg, duration, 8.75F * scale, Fonts.REGULAR),
                     y + 31.25F * scale,
                     Color.WHITE.getRGB(),
                     8.75F * scale,
                     Fonts.REGULAR
                  );
                  scissorHelper.resetScissor(vg, scissor);
                  Scissor scissor1 = scissorHelper.scissor(vg, x, y + scale, progressBarWidth, progressBarHeight);
                  nanoVGHelper.drawRoundedRectVaried(
                     vg,
                     x,
                     y + scale,
                     150.0F * scale,
                     36.5F * scale,
                     new Color(255, 255, 255, 135).getRGB(),
                     top ? rad : 0.0F,
                     top ? rad : 0.0F,
                     top ? 0.0F : rad,
                     top ? 0.0F : rad
                  );
                  nanoVGHelper.drawText(
                     vg,
                     this.getStatusEffectDescription(this.effect).getString(),
                     x + 32.5F * scale,
                     y + 18.75F * scale,
                     Color.BLACK.getRGB(),
                     11.25F * scale,
                     Fonts.SEMIBOLD
                  );
                  nanoVGHelper.drawText(
                     vg,
                     duration,
                     x + 147.5F * scale - nanoVGHelper.getTextWidth(vg, duration, 8.75F * scale, Fonts.REGULAR),
                     y + 31.25F * scale,
                     Color.BLACK.getRGB(),
                     8.75F * scale,
                     Fonts.REGULAR
                  );
                  scissorHelper.resetScissor(vg, scissor1);
               }
            }
         }
      }

      private Text getStatusEffectDescription(StatusEffectInstance statusEffect) {
         MutableText mutableText = ((StatusEffect)statusEffect.getEffectType().value()).getName().copy();
         if (statusEffect.getAmplifier() >= 1 && statusEffect.getAmplifier() <= 9) {
            mutableText.append(ScreenTexts.SPACE).append(Text.translatable("enchantment.level." + (statusEffect.getAmplifier() + 1)));
         }

         return mutableText;
      }

      private void drawSimple(long vg, NanoVGHelper helper, float x, float y, float scale, String duration) {
         float w = 150.0F * scale;
         float h = 40.0F * scale;
         helper.drawRect(vg, x, y + scale, w, h, new Color(0, 0, 0, 180).getRGB());
         String name = this.getStatusEffectDescription(this.effect).getString();
         float nameSize = 11.25F * scale;
         float nameHeight = helper.getTextHeight(vg, nameSize, Fonts.SEMIBOLD);
         float centerY = y + scale + h / 2.0F;
         helper.drawText(vg, name, x + 32.5F * scale, centerY, Color.WHITE.getRGB(), nameSize, Fonts.SEMIBOLD);
         if (duration != null) {
            float durSize = 11.25F * scale;
            float durWidth = helper.getTextWidth(vg, duration, durSize, Fonts.SEMIBOLD);
            helper.drawText(vg, duration, x + w - durWidth - 5.0F * scale, centerY, new Color(180, 180, 180).getRGB(), durSize, Fonts.SEMIBOLD);
         }

         float barY = y + scale + h - 2.0F * scale;
         float barH = 2.0F * scale;
         float progress = 1.0F * this.effect.getDuration() / this.maxDuration;
         helper.drawRect(vg, x, barY, w, barH, new Color(255, 255, 255, 30).getRGB());
         if (progress > 0.0F) {
            helper.drawRect(vg, x, barY, w * progress, barH, new Color(255, 255, 255, 220).getRGB());
         }
      }
   }
}
