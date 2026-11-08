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
import net.minecraft.class_124;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_241;
import net.minecraft.class_3532;

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
      for (class_1297 entity : mc.field_1687.method_18112()) {
         if (Targets.check(entity) && entity instanceof class_1309 living) {
            this.drawNameTag(living, event.getRenderTick());
         }
      }
   }

   private void drawNameTag(class_1309 entity, float partialTicks) {
      if ((entity != mc.field_1724 || !this.hideMe.getValue()) && (entity != mc.field_1724 || !mc.field_1690.method_31044().method_31034())) {
         boolean friend = false;
         String name = entity.method_5476().getString();
         boolean team;
         if (entity instanceof class_1657 player) {
            if (player.method_7334() != null) {
               name = player.method_7334().getName();
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
         class_241 pos = ProjectionUtil.entityScreenPos(entity, partialTicks);
         if (pos != null) {
            if (this.showGodItem.getValue() && GodItemDetector.shouldShowTag(GodItemDetector.TagTarget.NAMETAG)) {
               if (godAxe) {
                  name = class_124.field_1065 + "[GodAxe] " + class_124.field_1070 + name;
               }

               if (mace) {
                  name = class_124.field_1054 + "[Mace] " + class_124.field_1070 + name;
               }

               if (endCrystal) {
                  name = class_124.field_1064 + "[Crystal] " + class_124.field_1070 + name;
               }

               if (enchantedGapple) {
                  name = class_124.field_1076 + "[GApple] " + class_124.field_1070 + name;
               }
            }

            if (this.showMurder.getValue() && MurderFinder.isMurder(entity)) {
               name = class_124.field_1079 + "[Murder] " + class_124.field_1070 + name;
            }

            if (team) {
               name = class_124.field_1060 + "[Team] " + class_124.field_1070 + name;
            }

            if (friend) {
               name = class_124.field_1060 + "[Friend] " + class_124.field_1070 + name;
            }

            if (bot) {
               name = class_124.field_1061 + "[Bot] " + class_124.field_1070 + name;
            }

            if (hacker) {
               name = class_124.field_1079 + "[Hacker] " + class_124.field_1070 + name;
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
                  NanoUtil.scaleStart(pos.field_1343, pos.field_1342, scaleFactor);
                  nanoVGHelper.translate(vg, pos.field_1343, pos.field_1342);
                  if (this.style.getMode().equals("Classic")) {
                     float extraWidth = 197.5F;
                     float h1 = nanoVGHelper.getTextHeight(vg, 12.0F, Fonts.REGULAR);
                     float right = extraWidth / 2.0F;
                     float left = -extraWidth / 2.0F;
                     float top = -62.5F;
                     float bottom = -10.0F;
                     int alpha = 191;
                     float rad = 7.5F;
                     float rate = class_3532.method_15363(entity.method_6032() / entity.method_6063(), 0.0F, 1.0F);
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
                        String.format("Health: %.1f / %.1f", entity.method_6032(), entity.method_6063()),
                        left + 9.0F,
                        top + yy + h1 + 4.5F,
                        fontColor,
                        8.0F,
                        Fonts.REGULAR
                     );
                  } else if (this.style.getMode().equals("Summer")) {
                     double dist = mc.field_1773.method_19418().method_19326().method_1022(entity.method_30950(partialTicks));
                     String hpText = " " + (int)entity.method_6032() + "HP";
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
                           nanoVGHelper.getTextWidth(vg, String.format("Health: %.1f / %.1f", entity.method_6032(), entity.method_6063()), 12.0F, Fonts.REGULAR)
                        )
                        + 18.0F;
                     float h1 = nanoVGHelper.getTextHeight(vg, 12.0F, Fonts.REGULAR);
                     float right = extraWidth / 2.0F;
                     float left = -extraWidth / 2.0F;
                     float top = -62.5F;
                     float bottom = -10.0F;
                     int alpha = 191;
                     float rad = 7.5F;
                     float rate = class_3532.method_15363(entity.method_6032() / entity.method_6063(), 0.0F, 1.0F);
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
                        String.format("Health: %.1f / %.1f", entity.method_6032(), entity.method_6063()),
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
                        String.format("Health: %.1f / %.1f", entity.method_6032(), entity.method_6063()),
                        left + 9.0F,
                        top + yy + h1 + 4.5F,
                        fontRevertColor,
                        12.0F,
                        Fonts.REGULAR
                     );
                     scissorHelper.resetScissor(vg, scissor2);
                  } else if (this.style.getMode().equals("Simple")) {
                     double dist = mc.field_1773.method_19418().method_19326().method_1022(entity.method_30950(partialTicks));
                     String hpText = " " + (int)entity.method_6032() + "HP";
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
                     float rate = class_3532.method_15363(entity.method_6032() / entity.method_6063(), 0.0F, 1.0F);
                     float barH = 2.0F;
                     nanoVGHelper.drawRect(vg, left, top + bgHeight, bgWidth, barH, new Color(255, 255, 255, 30).getRGB());
                     nanoVGHelper.drawRect(vg, left, top + bgHeight, bgWidth * rate, barH, new Color(255, 255, 255, 220).getRGB());
                  }

                  nanoVGHelper.translate(vg, -pos.field_1343, -pos.field_1342);
                  NanoUtil.scaleEnd();
               }
            );
         }
      }
   }

   private double distanceScale(class_1297 ent, float tickDelta) {
      double dist = mc.field_1773.method_19418().method_19326().method_1022(ent.method_30950(tickDelta));
      return class_3532.method_15350(1.0 - dist * 0.01, 0.5, 2.147483647E9);
   }
}
