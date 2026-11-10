package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.renderer.scissor.Scissor;
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper;
import dev.southside.Southside;
import dev.southside.event.events.Render2DEvent;
import dev.southside.manager.FriendManager;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.client.Targets;
import dev.southside.module.impl.misc.AntiBot;
import dev.southside.module.impl.misc.GodItemDetector;
import dev.southside.module.impl.misc.HackerDetector;
import dev.southside.module.impl.misc.MurderFinder;
import dev.southside.module.impl.misc.Teams;
import dev.southside.nanovg.NanoUtil;
import dev.southside.orbit.EventHandler;
import dev.southside.render.ProjectionUtil;
import java.awt.Color;
import net.minecraft.util.Formatting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.MathHelper;

public class NameTag extends Module {
   private static NameTag INSTANCE;
   private final Dropdown style = new Dropdown("Style", new String[]{"Classic", "Summer", "Retro", "Simple"}, "Choose nametag style", 2, 0);
   private final Switch distanceScale = new Switch("Distance Scale", false);
   private final Switch showGodItem = new Switch("Show GodItem", true);
   private final Switch showMurder = new Switch("Show Murder", true);
   public final Switch hideVanilla = new Switch("Hide Vanilla Name", true);
   public final Switch hideMe = new Switch("Hide Me", true);
   private final Switch customScale = new Switch("Custom Scale", false);
   private final Switch glow = new Switch("Background Glow", false);
   private final Slider scale = new Slider("Scale", 1.0F, 0.1F, 10.0F, 0.1F);

   public NameTag() {
      super(Category.Render, "NameTags", "视奸。。。");
      INSTANCE = this;
   }

   @Override
   public void initPostRunnable() {
      super.initPostRunnable();
      this.hideIf(this.customScale.getLabel(), this.distanceScale::getValue);
      this.hideIf(this.scale.getLabel(), () -> !this.customScale.getValue() || this.distanceScale.getValue());
   }

   @EventHandler
   public final void onRender2DEvent(Render2DEvent event) {
      for (Entity entity : mc.world.getEntities()) {
         if (Targets.check(entity) && entity instanceof LivingEntity living) {
            this.drawNameTag(living, event.getRenderTick());
         }
      }
   }

   private void drawNameTag(LivingEntity entity, float partialTicks) {
      if ((entity != mc.player || !this.hideMe.getValue()) && (entity != mc.player || !mc.options.getPerspective().isFirstPerson())) {
         boolean friend = false;
         String name = entity.getDisplayName().getString();
         boolean team;
         if (entity instanceof PlayerEntity player) {
            if (player.getGameProfile() != null) {
               name = player.getGameProfile().getName();
               FriendManager friendManager = Southside.get().getFriendManager();
               if (friendManager != null) {
                  friend = friendManager.isFriend(name);
               }
            }

            team = Targets.check(entity) && Teams.isTeam(entity);
         } else {
            team = false;
         }

         boolean bot = AntiBot.isBot(entity);
         boolean godAxe = GodItemDetector.isGodAxe(entity);
         boolean mace = GodItemDetector.isMace(entity);
         boolean hacker = Southside.get().getModuleManager().get(HackerDetector.class).isHacker(entity);
         boolean endCrystal = GodItemDetector.isEndCrystal(entity);
         boolean enchantedGapple = GodItemDetector.isEGapple(entity);
         Vec2f pos = ProjectionUtil.entityScreenPos(entity, partialTicks);
         if (pos != null) {
            if (this.showGodItem.getValue() && GodItemDetector.shouldShowTag(GodItemDetector.TagTarget.NAMETAG)) {
               if (godAxe) {
                  name = Formatting.GOLD + "[GodAxe] " + Formatting.RESET + name;
               }

               if (mace) {
                  name = Formatting.YELLOW + "[Mace] " + Formatting.RESET + name;
               }

               if (endCrystal) {
                  name = Formatting.DARK_PURPLE + "[Crystal] " + Formatting.RESET + name;
               }

               if (enchantedGapple) {
                  name = Formatting.LIGHT_PURPLE + "[GApple] " + Formatting.RESET + name;
               }
            }

            if (this.showMurder.getValue() && MurderFinder.isMurder(entity)) {
               name = Formatting.DARK_RED + "[Murder] " + Formatting.RESET + name;
            }

            if (team) {
               name = Formatting.GREEN + "[Team] " + Formatting.RESET + name;
            }

            if (friend) {
               name = Formatting.GREEN + "[Friend] " + Formatting.RESET + name;
            }

            if (bot) {
               name = Formatting.RED + "[Bot] " + Formatting.RESET + name;
            }

            if (hacker) {
               name = Formatting.DARK_RED + "[Hacker] " + Formatting.RESET + name;
            }

            NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
            ScissorHelper scissorHelper = ScissorHelper.INSTANCE;
            String finalName = name;
            nanoVGHelper.setupAndDraw(
               true,
               vg -> {
                  float scaleFactor = (float)(
                     this.customScale.getValue() ? this.scale.getDouble() : (this.distanceScale.getValue() ? this.distanceScale(entity, partialTicks) : 1.0)
                  );
                  NanoUtil.scaleStart(pos.x, pos.y, scaleFactor);
                  nanoVGHelper.translate(vg, pos.x, pos.y);
                  if (this.style.getMode().equals("Classic")) {
                     float extraWidth = 197.5F;
                     float h1 = nanoVGHelper.getTextHeight(vg, 12.0F, Fonts.REGULAR);
                     float right = extraWidth / 2.0F;
                     float left = -extraWidth / 2.0F;
                     float top = -62.5F;
                     float bottom = -10.0F;
                     int alpha = 191;
                     float rad = 7.5F;
                     float rate = MathHelper.clamp(entity.getHealth() / entity.getMaxHealth(), 0.0F, 1.0F);
                     if (this.glow.getValue()) {
                        nanoVGHelper.drawDropShadow(vg, left, top, right - left, bottom - top, 10.0F, 0.0F, rad, new Color(34, 35, 38, alpha));
                     }

                     nanoVGHelper.drawRoundedRect(vg, left, top, right - left, bottom - top, new Color(34, 35, 38, alpha).getRGB(), rad);
                     Scissor scissor = scissorHelper.scissor(vg, left, bottom - 4.0F, (right - left) * rate, 4.0F);
                     nanoVGHelper.drawRoundedRectVaried(vg, left, top, right - left, bottom - top, Color.WHITE.getRGB(), 0.0F, 0.0F, rad, rad);
                     scissorHelper.resetScissor(vg, scissor);
                     float size = 30.0F;
                     float yy = (52.5F - size) / 2.0F + h1 / 2.0F;
                     int fontColor = new Color(255, 255, 255, 255).getRGB();
                     nanoVGHelper.drawTextWithFormatting(vg, finalName, left + 9.0F, top + yy, fontColor, 13.0F, Fonts.SEMIBOLD);
                     nanoVGHelper.drawTextWithFormatting(
                        vg,
                        String.format("Health: %.1f / %.1f", entity.getHealth(), entity.getMaxHealth()),
                        left + 9.0F,
                        top + yy + h1 + 4.5F,
                        fontColor,
                        8.0F,
                        Fonts.REGULAR
                     );
                  } else if (this.style.getMode().equals("Summer")) {
                     double dist = mc.gameRenderer.getCamera().getPos().distanceTo(entity.getLerpedPos(partialTicks));
                     String hpText = " " + (int)entity.getHealth() + "HP";
                     String distText = " " + String.format("%.1f", dist) + "m";
                     float textSize = 13.0F;
                     float fontHeight = nanoVGHelper.getTextHeight(vg, textSize, Fonts.SEMIBOLD);
                     float nameWidth = nanoVGHelper.getTextWidth(vg, finalName, textSize, Fonts.SEMIBOLD);
                     float hpWidth = nanoVGHelper.getTextWidth(vg, hpText, textSize, Fonts.SEMIBOLD);
                     float distWidth = nanoVGHelper.getTextWidth(vg, distText, textSize, Fonts.SEMIBOLD);
                     float totalWidth = nameWidth + hpWidth + distWidth;
                     float padding = 10.0F;
                     float bgWidth = totalWidth + padding * 2.0F;
                     float bgHeight = fontHeight + padding * 2.0F;
                     float left = -bgWidth / 2.0F;
                     float top = -bgHeight / 2.0F;
                     float right = bgWidth / 2.0F;
                     float bottom = bgHeight / 2.0F;
                     int bgAlpha = 76;
                     float rad = 7.5F;
                     if (this.glow.getValue()) {
                        nanoVGHelper.drawDropShadow(vg, left, top, bgWidth, bgHeight, 10.0F, 0.0F, rad, new Color(0, 0, 0, bgAlpha));
                     }

                     nanoVGHelper.drawRoundedRect(vg, left, top, bgWidth, bgHeight, new Color(0, 0, 0, bgAlpha).getRGB(), rad);
                     float textY = top + padding + fontHeight / 2.0F;
                     float textX = left + padding;
                     nanoVGHelper.drawTextWithFormatting(vg, finalName, textX, textY, new Color(255, 255, 255).getRGB(), textSize, Fonts.SEMIBOLD);
                     textX += nameWidth;
                     nanoVGHelper.drawTextWithFormatting(vg, hpText, textX, textY, new Color(255, 255, 255).getRGB(), textSize, Fonts.SEMIBOLD);
                     textX += hpWidth;
                     nanoVGHelper.drawTextWithFormatting(vg, distText, textX, textY, new Color(128, 128, 128).getRGB(), textSize, Fonts.SEMIBOLD);
                  } else if (this.style.getMode().equals("Retro")) {
                     float extraWidth = Math.max(
                           nanoVGHelper.getTextWidth(vg, finalName, 18.0F, Fonts.SEMIBOLD),
                           nanoVGHelper.getTextWidth(vg, String.format("Health: %.1f / %.1f", entity.getHealth(), entity.getMaxHealth()), 12.0F, Fonts.REGULAR)
                        )
                        + 18.0F;
                     float h1 = nanoVGHelper.getTextHeight(vg, 12.0F, Fonts.REGULAR);
                     float right = extraWidth / 2.0F;
                     float left = -extraWidth / 2.0F;
                     float top = -62.5F;
                     float bottom = -10.0F;
                     int alpha = 191;
                     float rad = 7.5F;
                     float rate = MathHelper.clamp(entity.getHealth() / entity.getMaxHealth(), 0.0F, 1.0F);
                     if (this.glow.getValue()) {
                        nanoVGHelper.drawDropShadow(vg, left, top, right - left, bottom - top, 10.0F, 0.0F, rad, new Color(34, 35, 38, alpha));
                     }

                     float size = 30.0F;
                     float yy = (60.0F - size) / 2.0F + h1 / 2.0F;
                     int fontColor = new Color(255, 255, 255, 255).getRGB();
                     int fontRevertColor = new Color(0, 0, 0, 255).getRGB();
                     Scissor scissor = scissorHelper.scissor(vg, left + (right - left) * rate, top, right - left - (right - left) * rate, bottom - top);
                     nanoVGHelper.drawRoundedRect(vg, left, top, right - left, bottom - top, new Color(34, 35, 38, alpha).getRGB(), rad);
                     nanoVGHelper.drawTextWithFormatting(vg, finalName, left + 9.0F, top + yy, fontColor, 18.0F, Fonts.SEMIBOLD);
                     nanoVGHelper.drawTextWithFormatting(
                        vg,
                        String.format("Health: %.1f / %.1f", entity.getHealth(), entity.getMaxHealth()),
                        left + 9.0F,
                        top + yy + h1 + 4.5F,
                        fontColor,
                        12.0F,
                        Fonts.REGULAR
                     );
                     scissorHelper.resetScissor(vg, scissor);
                     Scissor scissor2 = scissorHelper.scissor(vg, left, top, (right - left) * rate, bottom - top);
                     nanoVGHelper.drawRoundedRect(vg, left, top, right - left, bottom - top, new Color(255, 255, 255, 127).getRGB(), rad);
                     nanoVGHelper.drawTextWithFormatting(vg, finalName, left + 9.0F, top + yy, fontRevertColor, 18.0F, Fonts.SEMIBOLD);
                     nanoVGHelper.drawTextWithFormatting(
                        vg,
                        String.format("Health: %.1f / %.1f", entity.getHealth(), entity.getMaxHealth()),
                        left + 9.0F,
                        top + yy + h1 + 4.5F,
                        fontRevertColor,
                        12.0F,
                        Fonts.REGULAR
                     );
                     scissorHelper.resetScissor(vg, scissor2);
                  } else if (this.style.getMode().equals("Simple")) {
                     double dist = mc.gameRenderer.getCamera().getPos().distanceTo(entity.getLerpedPos(partialTicks));
                     String hpText = " " + (int)entity.getHealth() + "HP";
                     String distText = " " + String.format("%.1f", dist) + "m";
                     float textSize = 13.0F;
                     float fontHeight = nanoVGHelper.getTextHeight(vg, textSize, Fonts.SEMIBOLD);
                     float nameWidth = nanoVGHelper.getTextWidth(vg, finalName, textSize, Fonts.SEMIBOLD);
                     float hpWidth = nanoVGHelper.getTextWidth(vg, hpText, textSize, Fonts.SEMIBOLD);
                     float distWidth = nanoVGHelper.getTextWidth(vg, distText, textSize, Fonts.SEMIBOLD);
                     float totalWidth = nameWidth + hpWidth + distWidth;
                     float padding = 10.0F;
                     float bgWidth = totalWidth + padding * 2.0F;
                     float bgHeight = fontHeight + padding * 2.0F;
                     float left = -bgWidth / 2.0F;
                     float top = -bgHeight / 2.0F;
                     nanoVGHelper.drawRect(vg, left, top, bgWidth, bgHeight, new Color(0, 0, 0, 180).getRGB());
                     float textY = top + padding + fontHeight / 2.0F;
                     float textX = left + padding;
                     nanoVGHelper.drawTextWithFormatting(vg, finalName, textX, textY, Color.WHITE.getRGB(), textSize, Fonts.SEMIBOLD);
                     textX += nameWidth;
                     nanoVGHelper.drawTextWithFormatting(vg, hpText, textX, textY, Color.WHITE.getRGB(), textSize, Fonts.SEMIBOLD);
                     textX += hpWidth;
                     nanoVGHelper.drawTextWithFormatting(vg, distText, textX, textY, new Color(180, 180, 180).getRGB(), textSize, Fonts.SEMIBOLD);
                     float rate = MathHelper.clamp(entity.getHealth() / entity.getMaxHealth(), 0.0F, 1.0F);
                     float barH = 2.0F;
                     nanoVGHelper.drawRect(vg, left, top + bgHeight, bgWidth, barH, new Color(255, 255, 255, 30).getRGB());
                     nanoVGHelper.drawRect(vg, left, top + bgHeight, bgWidth * rate, barH, new Color(255, 255, 255, 220).getRGB());
                  }

                  nanoVGHelper.translate(vg, -pos.x, -pos.y);
                  NanoUtil.scaleEnd();
               }
            );
         }
      }
   }

   private double distanceScale(Entity ent, float tickDelta) {
      double dist = mc.gameRenderer.getCamera().getPos().distanceTo(ent.getLerpedPos(tickDelta));
      return MathHelper.clamp(1.0 - dist * 0.01, 0.5, 2.147483647E9);
   }
}
