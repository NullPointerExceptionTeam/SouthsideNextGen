package dev.southside.mixin.hud;

import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import dev.southside.MinecraftAccessor;
import dev.southside.Southside;
import dev.southside.module.impl.render.HUD;
import dev.southside.module.impl.render.NoRender;
import dev.southside.module.impl.render.ScoreBoard;
import dev.southside.utils.render.jello.animation.Animation;
import dev.southside.utils.render.jello.animation.Easing;
import java.awt.Color;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Arm;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Util;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.item.ItemStack;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.math.random.Random;
import net.minecraft.client.render.RenderTickCounter;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({InGameHud.class})
public abstract class MixinInGameHud {
   @Shadow
   @Final
   private static Identifier HOTBAR_ATTACK_INDICATOR_BACKGROUND_TEXTURE;
   @Shadow
   @Final
   private static Identifier HOTBAR_ATTACK_INDICATOR_PROGRESS_TEXTURE;
   @Shadow
   @Final
   private static Identifier EXPERIENCE_BAR_BACKGROUND_TEXTURE;
   @Shadow
   @Final
   private static Identifier EXPERIENCE_BAR_PROGRESS_TEXTURE;
   @Shadow
   @Final
   private static Identifier FOOD_EMPTY_HUNGER_TEXTURE;
   @Shadow
   @Final
   private static Identifier FOOD_HALF_HUNGER_TEXTURE;
   @Shadow
   @Final
   private static Identifier FOOD_FULL_HUNGER_TEXTURE;
   @Shadow
   @Final
   private static Identifier AIR_BURSTING_TEXTURE;
   @Shadow
   @Final
   private static Identifier ARMOR_EMPTY_TEXTURE;
   @Shadow
   @Final
   private static Identifier FOOD_EMPTY_TEXTURE;
   @Shadow
   @Final
   private static Identifier ARMOR_FULL_TEXTURE;
   @Shadow
   @Final
   private static Identifier ARMOR_HALF_TEXTURE;
   @Shadow
   @Final
   private static Identifier FOOD_HALF_TEXTURE;
   @Shadow
   @Final
   private static Identifier FOOD_FULL_TEXTURE;
   @Shadow
   @Final
   private static Identifier AIR_TEXTURE;
   @Shadow
   @Final
   private MinecraftClient client;
   @Shadow
   @Final
   private Random random;
   @Shadow
   private long heartJumpEndTick;
   @Shadow
   private int ticks;
   @Shadow
   private int lastHealthValue;
   @Shadow
   private long lastHealthCheckTime;
   @Shadow
   private int renderHealthValue;
   @Unique
   private Animation animation;

   @Shadow
   public TextRenderer getTextRenderer() {
      return null;
   }

   @Shadow
   private PlayerEntity getCameraPlayer() {
      return null;
   }

   @Shadow
   private int getHeartRows(int heartCount) {
      return 0;
   }

   @Shadow
   private int getHeartCount(@Nullable LivingEntity entity) {
      return 0;
   }

   @Shadow
   private LivingEntity getRiddenEntity() {
      return null;
   }

   @Shadow
   private void renderHealthBar(
      DrawContext context,
      PlayerEntity player,
      int x,
      int y,
      int lines,
      int regeneratingHeartIndex,
      float maxHealth,
      int lastHealth,
      int health,
      int absorption,
      boolean blinking
   ) {
   }

   @Shadow
   protected abstract void renderHotbarItem(DrawContext var1, int var2, int var3, RenderTickCounter var4, PlayerEntity var5, ItemStack var6, int var7);

   @Inject(
      method = {"renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void renderCustomScoreboard(DrawContext context, ScoreboardObjective objective, CallbackInfo ci) {
      if (MinecraftAccessor.mc.world != null && Southside.INSTANCE.getModuleManager().get(ScoreBoard.class).isEnabled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"renderHotbar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void HookRenderHotbar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      HUD hud = Southside.get().getModuleManager().get(HUD.class);
      if (hud.isEnabled() && hud.hotbar.getValue()) {
         this.newHotbar(tickCounter.getTickDelta(false), context, tickCounter);
         ci.cancel();
      }
   }

   @Overwrite
   public void renderExperienceBar(DrawContext context, int x) {
      boolean useCustomHotbar = Southside.get().getModuleManager().get(HUD.class).hotbar.getValue();
      int experience = this.client.player.getNextLevelExperience();
      if (experience > 0) {
         Profilers.get().push("expBar");
         int progressWidth = (int)(this.client.player.experienceProgress * 183.0F);
         int barY = context.getScaledWindowHeight() - 32;
         if (!useCustomHotbar) {
            barY += 3;
         }

         if (useCustomHotbar) {
            int finalBarY = barY;
            int experienceLevel = this.client.player.experienceLevel;
            NanoVGHelper.INSTANCE.setupAndDraw(true, vg -> {
               NanoVGHelper.INSTANCE.drawRoundedRect(vg, x, finalBarY, 182.0F, 4.0F, -13421773, 1.0F);
               if (progressWidth > 0) {
                  NanoVGHelper.INSTANCE.drawRoundedRect(vg, x, finalBarY, progressWidth, 3.0F, -11154347, 1.0F);
               }

               if (experienceLevel > 0) {
                  Profilers.get().push("expLevel");
                  String levelTextx = String.valueOf(experienceLevel);
                  float textWidth = NanoVGHelper.INSTANCE.getTextWidth(vg, levelTextx, 9.0F, Fonts.BOLD);
                  float textXx = x + (182.0F - textWidth) / 2.0F;
                  float textYx = finalBarY - 5;
                  NanoVGHelper.INSTANCE.drawText(vg, levelTextx, textXx - 1.0F, textYx, -16777216, 9.0F, Fonts.BOLD);
                  NanoVGHelper.INSTANCE.drawText(vg, levelTextx, textXx + 1.0F, textYx, -16777216, 9.0F, Fonts.BOLD);
                  NanoVGHelper.INSTANCE.drawText(vg, levelTextx, textXx, textYx - 1.0F, -16777216, 9.0F, Fonts.BOLD);
                  NanoVGHelper.INSTANCE.drawText(vg, levelTextx, textXx, textYx + 1.0F, -16777216, 9.0F, Fonts.BOLD);
                  NanoVGHelper.INSTANCE.drawText(vg, levelTextx, textXx, textYx, -11154347, 9.0F, Fonts.BOLD);
                  Profilers.get().pop();
               }
            });
         } else {
            context.drawGuiTexture(RenderLayer::getGuiTextured, EXPERIENCE_BAR_BACKGROUND_TEXTURE, x, barY, 182, 5);
            if (progressWidth > 0) {
               context.drawGuiTexture(RenderLayer::getGuiTextured, EXPERIENCE_BAR_PROGRESS_TEXTURE, 182, 5, 0, 0, x, barY, progressWidth, 5);
            }

            if (this.client.player.experienceLevel > 0) {
               Profilers.get().push("expLevel");
               String levelText = String.valueOf(this.client.player.experienceLevel);
               int textX = (context.getScaledWindowWidth() - this.getTextRenderer().getWidth(levelText)) / 2;
               int textY = context.getScaledWindowHeight() - 31 - 4;
               context.drawText(this.getTextRenderer(), levelText, textX + 1, textY, 0, false);
               context.drawText(this.getTextRenderer(), levelText, textX - 1, textY, 0, false);
               context.drawText(this.getTextRenderer(), levelText, textX, textY + 1, 0, false);
               context.drawText(this.getTextRenderer(), levelText, textX, textY - 1, 0, false);
               context.drawText(this.getTextRenderer(), levelText, textX, textY, 8453920, false);
               Profilers.get().pop();
            }
         }

         Profilers.get().pop();
      }
   }

   @Overwrite
   private void renderStatusBars(DrawContext context) {
      PlayerEntity playerEntity = this.getCameraPlayer();
      if (playerEntity != null) {
         int i = MathHelper.ceil(playerEntity.getHealth());
         boolean bl = this.heartJumpEndTick > this.ticks && (this.heartJumpEndTick - this.ticks) / 3L % 2L == 1L;
         long l = Util.getMeasuringTimeMs();
         if (i < this.lastHealthValue && playerEntity.timeUntilRegen > 0) {
            this.lastHealthCheckTime = l;
            this.heartJumpEndTick = this.ticks + 20;
         } else if (i > this.lastHealthValue && playerEntity.timeUntilRegen > 0) {
            this.lastHealthCheckTime = l;
            this.heartJumpEndTick = this.ticks + 10;
         }

         if (l - this.lastHealthCheckTime > 1000L) {
            this.lastHealthValue = i;
            this.renderHealthValue = i;
            this.lastHealthCheckTime = l;
         }

         this.lastHealthValue = i;
         int j = this.renderHealthValue;
         this.random.setSeed(this.ticks * 312871);
         HungerManager hungerManager = playerEntity.getHungerManager();
         int k = hungerManager.getFoodLevel();
         int m = context.getScaledWindowWidth() / 2 - 91;
         int n = context.getScaledWindowWidth() / 2 + 91;
         int o = context.getScaledWindowHeight() - 39;
         float f = Math.max((float)playerEntity.getAttributeValue(EntityAttributes.MAX_HEALTH), (float)Math.max(j, i));
         int p = MathHelper.ceil(playerEntity.getAbsorptionAmount());
         int q = MathHelper.ceil((f + p) / 2.0F / 10.0F);
         int r = Math.max(10 - (q - 2), 3);
         int s = o - (q - 1) * r - 10;
         if (Southside.get().getModuleManager().get(HUD.class).hotbar.getValue()) {
            s -= 4;
         }

         int t = o - 10;
         int u = playerEntity.getArmor();
         int v = -1;
         if (playerEntity.hasStatusEffect(StatusEffects.REGENERATION)) {
            v = this.ticks % MathHelper.ceil(f + 5.0F);
         }

         Profilers.get().push("armor");

         for (int w = 0; w < 10; w++) {
            if (u > 0) {
               int x = m + w * 8;
               if (w * 2 + 1 < u) {
                  context.drawGuiTexture(RenderLayer::getGuiTextured, ARMOR_FULL_TEXTURE, x, s, 9, 9);
               }

               if (w * 2 + 1 == u) {
                  context.drawGuiTexture(RenderLayer::getGuiTextured, ARMOR_HALF_TEXTURE, x, s, 9, 9);
               }

               if (w * 2 + 1 > u) {
                  context.drawGuiTexture(RenderLayer::getGuiTextured, ARMOR_EMPTY_TEXTURE, x, s, 9, 9);
               }
            }
         }

         Profilers.get().swap("health");
         if (Southside.get().getModuleManager().get(HUD.class).hotbar.getValue()) {
            o -= 4;
         }

         this.renderHealthBar(context, playerEntity, m, o, r, v, f, i, j, p, bl);
         LivingEntity livingEntity = this.getRiddenEntity();
         int xx = this.getHeartCount(livingEntity);
         if (xx == 0) {
            Profilers.get().swap("food");

            for (int y = 0; y < 10; y++) {
               int z = o;
               Identifier identifier;
               Identifier identifier2;
               Identifier identifier3;
               if (playerEntity.hasStatusEffect(StatusEffects.HUNGER)) {
                  identifier = FOOD_EMPTY_HUNGER_TEXTURE;
                  identifier2 = FOOD_HALF_HUNGER_TEXTURE;
                  identifier3 = FOOD_FULL_HUNGER_TEXTURE;
               } else {
                  identifier = FOOD_EMPTY_TEXTURE;
                  identifier2 = FOOD_HALF_TEXTURE;
                  identifier3 = FOOD_FULL_TEXTURE;
               }

               if (playerEntity.getHungerManager().getSaturationLevel() <= 0.0F && this.ticks % (k * 3 + 1) == 0) {
                  z = o + (this.random.nextInt(3) - 1);
               }

               int aa = n - y * 8 - 9;
               context.drawGuiTexture(RenderLayer::getGuiTextured, identifier, aa, z, 9, 9);
               if (y * 2 + 1 < k) {
                  context.drawGuiTexture(RenderLayer::getGuiTextured, identifier3, aa, z, 9, 9);
               }

               if (y * 2 + 1 == k) {
                  context.drawGuiTexture(RenderLayer::getGuiTextured, identifier2, aa, z, 9, 9);
               }
            }

            t -= 10;
         }

         Profilers.get().swap("air");
         int y = playerEntity.getMaxAir();
         int zx = Math.min(playerEntity.getAir(), y);
         if (playerEntity.isSubmergedIn(FluidTags.WATER) || zx < y) {
            int ab = this.getHeartRows(xx) - 1;
            t -= ab * 10;
            if (Southside.get().getModuleManager().get(HUD.class).hotbar.getValue()) {
               t -= 4;
            }

            int ac = MathHelper.ceil((zx - 2) * 10.0 / y);
            int ad = MathHelper.ceil(zx * 10.0 / y) - ac;

            for (int aax = 0; aax < ac + ad; aax++) {
               if (aax < ac) {
                  context.drawGuiTexture(RenderLayer::getGuiTextured, AIR_TEXTURE, n - aax * 8 - 9, t, 9, 9);
               } else {
                  context.drawGuiTexture(RenderLayer::getGuiTextured, AIR_BURSTING_TEXTURE, n - aax * 8 - 9, t, 9, 9);
               }
            }
         }

         Profilers.get().pop();
      }
   }

   @Unique
   private void newHotbar(float tickDelta, DrawContext context, RenderTickCounter counter) {
      if (this.animation == null) {
         this.animation = new Animation(Easing.EASE_IN_SINE, 150L);
      }

      HUD hud = Southside.get().getModuleManager().get(HUD.class);
      PlayerEntity playerEntity = this.getCameraPlayer();
      if (playerEntity != null) {
         int centerX = context.getScaledWindowWidth() / 2;
         int hotbarBottom = context.getScaledWindowHeight() - 25;
         int slotSize = 22;
         int hotbarWidth = 9 * slotSize;
         int hotbarHeight = 22;
         float cornerRadius = 6.0F;
         int backgroundColor = new Color(34, 35, 38, 140).getRGB();
         NanoVGHelper.INSTANCE
            .setupAndDraw(
               true,
               vg -> NanoVGHelper.INSTANCE
                  .drawRoundedRect(vg, centerX - hotbarWidth / 2 - 1, hotbarBottom - 1, hotbarWidth + 2, hotbarHeight + 2, backgroundColor, cornerRadius)
            );
         int selectedSlot = playerEntity.getInventory().selectedSlot;
         int highlightX = centerX - hotbarWidth / 2 + selectedSlot * slotSize;
         this.animation.animate(highlightX);
         int highlightColor = new Color(255, 255, 255, 255).getRGB();
         NanoVGHelper.INSTANCE
            .setupAndDraw(
               true,
               vg -> NanoVGHelper.INSTANCE
                  .drawHollowRoundRect(
                     vg,
                     hud.hotbarAnimation.getValue() ? (float)this.animation.getValue() : highlightX,
                     hotbarBottom,
                     slotSize,
                     hotbarHeight,
                     highlightColor,
                     cornerRadius - 2.0F,
                     0.6F
                  )
            );

         for (int slot = 0; slot < 9; slot++) {
            int x = centerX - hotbarWidth / 2 + slot * slotSize + 3;
            int y = hotbarBottom + 3;
            ItemStack stack = (ItemStack)playerEntity.getInventory().main.get(slot);
            this.renderHotbarItem(context, x, y, counter, playerEntity, stack, slot + 1);
         }

         ItemStack offhandStack = playerEntity.getOffHandStack();
         if (!offhandStack.isEmpty()) {
            Arm mainArm = playerEntity.getMainArm().getOpposite();
            int offhandX = mainArm == Arm.LEFT ? centerX - hotbarWidth / 2 - 24 : centerX + hotbarWidth / 2 + 4;
            this.renderHotbarItem(context, offhandX, hotbarBottom + 3, counter, playerEntity, offhandStack, 10);
         }

         if (this.client.options.getAttackIndicator().getValue() == AttackIndicator.HOTBAR) {
            float cooldown = playerEntity.getAttackCooldownProgress(0.0F);
            if (cooldown < 1.0F) {
               int indicatorY = hotbarBottom + 2;
               int indicatorX = playerEntity.getMainArm() == Arm.RIGHT ? centerX - hotbarWidth / 2 - 22 : centerX + hotbarWidth / 2 + 6;
               int progressHeight = (int)(cooldown * 19.0F);
               context.drawGuiTexture(RenderLayer::getGuiTextured, HOTBAR_ATTACK_INDICATOR_BACKGROUND_TEXTURE, indicatorX, indicatorY, 18, 18);
               context.drawGuiTexture(
                  RenderLayer::getGuiTextured, HOTBAR_ATTACK_INDICATOR_PROGRESS_TEXTURE, 18, 18, 0, 18 - progressHeight, indicatorX, indicatorY + 18 - progressHeight, 18, progressHeight
               );
            }
         }
      }
   }

   @Inject(
      method = {"renderStatusEffectOverlay(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void hookRenderEffects(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      NoRender noRender = Southside.get().getModuleManager().get(NoRender.class);
      if (noRender.isEnabled() && noRender.vanillaEffects.getValue()) {
         ci.cancel();
      }
   }
}
