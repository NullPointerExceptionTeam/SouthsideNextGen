package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Font;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import dev.southside.Southside;
import dev.southside.event.events.Render2DEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.client.Targets;
import dev.southside.module.impl.misc.GodItemDetector;
import dev.southside.module.impl.misc.HackerDetector;
import dev.southside.nanovg.NanoUtil;
import dev.southside.orbit.EventHandler;
import dev.southside.render.ProjectionUtil;
import dev.southside.utils.player.EnchantmentUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.class_124;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1893;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_6880;

public class Detector extends Module {
   private static final class_1304[] ARMOR_SLOTS = new class_1304[]{class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166};
   private static final Font FONT = Fonts.REGULAR;
   private final Switch distanceScale = new Switch("Distance Scale", false);
   private final Switch customScale = new Switch("Custom Scale", false);
   private final Slider scale = new Slider("Scale", 1.0F, 0.1F, 10.0F, 0.1F);
   private final Switch respectTargets = new Switch("Respect Targets", true);
   private final Switch showDistance = new Switch("Show Distance", true);
   private final Switch showProtection = new Switch("Show Protection", true);
   private final Switch showPotions = new Switch("Show Potions", true);
   private final Slider renderRange = new Slider("Range", 40.0, 5.0, 120.0, 1.0);
   private final Slider offsetY = new Slider("Offset Y", 0.0, -60.0, 120.0, 1.0);
   private static final Map<class_6880<class_1291>, String> EFFECT_SHORT_NAMES = Map.of(
      class_1294.field_5904,
      "Spd",
      class_1294.field_5910,
      "Str",
      class_1294.field_5907,
      "Res",
      class_1294.field_5924,
      "Reg",
      class_1294.field_5918,
      "FRes",
      class_1294.field_5898,
      "Abs",
      class_1294.field_5905,
      "Inv",
      class_1294.field_5913,
      "Jump",
      class_1294.field_5909,
      "Slow",
      class_1294.field_5911,
      "Weak"
   );

   public Detector() {
      super(Category.Render, "Detector", "在玩家脚底显示状态监控");
   }

   @Override
   public void initPostRunnable() {
      super.initPostRunnable();
      this.hideIf(this.customScale.getLabel(), this.distanceScale::getValue);
      this.hideIf(this.scale.getLabel(), () -> !this.customScale.getValue() || this.distanceScale.getValue());
   }

   @EventHandler
   private void onRender2D(Render2DEvent event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         NanoVGHelper helper = NanoVGHelper.INSTANCE;
         helper.setupAndDraw(
            true,
            vg -> {
               for (class_1657 target : mc.field_1687.method_18456()) {
                  if (this.shouldRender(target) && !(mc.field_1724.method_5739(target) > this.renderRange.getValue().floatValue())) {
                     class_241 anchor = this.getFeetScreenPos(target, event.getRenderTick());
                     if (anchor != null) {
                        List<String> lines = this.buildLines(target);
                        if (!lines.isEmpty()) {
                           float fontSize = 11.0F;
                           float textHeight = helper.getTextHeight(vg, fontSize, FONT);
                           float lineHeight = textHeight + 2.0F;
                           float textWidth = this.getMaxWidth(helper, vg, lines, fontSize);
                           float boxWidth = textWidth + 18.0F;
                           float boxHeight = lineHeight * lines.size() + 12.0F;
                           float offsetYValue = this.offsetY.getValue().floatValue();
                           float baseX = anchor.field_1343 - boxWidth / 2.0F;
                           float baseY = anchor.field_1342 + offsetYValue - boxHeight;
                           float scaleFactor = (float)(
                              this.customScale.getValue()
                                 ? this.scale.getDouble()
                                 : (this.distanceScale.getValue() ? this.distanceScale(target, event.getRenderTick()) : 1.0)
                           );
                           NanoUtil.scaleStart(anchor.field_1343, anchor.field_1342, scaleFactor);
                           this.drawBackground(helper, vg, baseX, baseY, boxWidth, boxHeight);
                           float textY = baseY + 8.0F;

                           for (String line : lines) {
                              helper.drawRawTextWithFormatting(vg, line, baseX + 9.0F, textY, Color.WHITE.getRGB(), fontSize, FONT);
                              textY += lineHeight;
                           }

                           NanoUtil.scaleEnd();
                        }
                     }
                  }
               }
            }
         );
      }
   }

   private void drawBackground(NanoVGHelper helper, long vg, float x, float y, float width, float height) {
      int bgAlpha = 76;
      helper.drawRoundedRect(vg, x, y, width, height, new Color(0, 0, 0, bgAlpha).getRGB(), 7.5F);
   }

   private boolean shouldRender(class_1657 player) {
      if (player == null || player == mc.field_1724) {
         return false;
      } else {
         return player.method_5805() && !player.method_7325() ? !this.respectTargets.getValue() || Targets.check(player) : false;
      }
   }

   private class_241 getFeetScreenPos(class_1657 entity, float partialTicks) {
      class_243 pos = entity.method_30950(partialTicks).method_1031(0.0, 0.05, 0.0);
      return ProjectionUtil.worldScreenPos(pos);
   }

   private List<String> buildLines(class_1657 player) {
      List<String> info = new ArrayList<>();
      List<String> godTags = this.buildGodItemTags(player);
      if (!godTags.isEmpty()) {
         info.add(String.join(" ", godTags));
      }

      if (this.showDistance.getValue()) {
         info.add(class_124.field_1080 + "Dist " + class_124.field_1068 + String.format("%.1fm", mc.field_1724.method_5739(player)));
      }

      if (this.showProtection.getValue()) {
         info.add(this.formatProtection(player));
      }

      if (this.showProtection.getValue()) {
         String sharp = this.formatSharpness(player);
         if (sharp != null) {
            info.add(sharp);
         }
      }

      if (this.showPotions.getValue()) {
         String potionLine = this.formatPotions(player);
         if (potionLine != null) {
            info.add(potionLine);
         }
      }

      return info;
   }

   private List<String> buildGodItemTags(class_1657 player) {
      List<String> tags = new ArrayList<>();
      HackerDetector hackerDetector = Southside.get().getModuleManager().get(HackerDetector.class);
      if (hackerDetector.isEnabled() && hackerDetector.isHacker(player)) {
         tags.add(class_124.field_1079 + "[Hacker]" + class_124.field_1070);
      }

      if (!GodItemDetector.shouldShowTag(GodItemDetector.TagTarget.DETECTOR)) {
         return tags;
      } else {
         if (GodItemDetector.isGodAxe(player)) {
            tags.add(class_124.field_1065 + "[GodAxe]" + class_124.field_1070);
         }

         if (GodItemDetector.isMace(player)) {
            tags.add(class_124.field_1054 + "[Mace]" + class_124.field_1070);
         }

         if (GodItemDetector.isEndCrystal(player)) {
            tags.add(class_124.field_1064 + "[Crystal]" + class_124.field_1070);
         }

         if (GodItemDetector.isEGapple(player)) {
            tags.add(class_124.field_1076 + "[GApple]" + class_124.field_1070);
         }

         return tags;
      }
   }

   private String formatProtection(class_1657 player) {
      int maxProt = 0;
      boolean hasArmor = false;

      for (class_1304 slot : ARMOR_SLOTS) {
         class_1799 stack = player.method_6118(slot);
         if (!stack.method_7960()) {
            hasArmor = true;
            int level = EnchantmentUtils.getLevel(class_1893.field_9111, stack);
            if (level > maxProt) {
               maxProt = level;
            }
         }
      }

      if (!hasArmor) {
         return class_124.field_1080 + "Prot " + class_124.field_1061 + "None";
      } else {
         class_124 color = maxProt >= 4 ? class_124.field_1061 : (maxProt >= 2 ? class_124.field_1054 : class_124.field_1060);
         return class_124.field_1080 + "Prot " + color + maxProt;
      }
   }

   private String formatSharpness(class_1657 player) {
      class_1799 mainHand = player.method_6047();
      if (mainHand.method_7960()) {
         return null;
      } else {
         int level = EnchantmentUtils.getLevel(class_1893.field_9118, mainHand);
         if (level <= 0) {
            return null;
         } else {
            class_124 color = level >= 5 ? class_124.field_1061 : (level >= 3 ? class_124.field_1054 : class_124.field_1068);
            return class_124.field_1080 + "Sharp " + color + level;
         }
      }
   }

   private String formatPotions(class_1657 player) {
      Collection<class_1293> effects = player.method_6026();
      if (effects.isEmpty()) {
         return null;
      } else {
         List<String> parts = new ArrayList<>();

         for (class_1293 effect : effects) {
            String shortName = EFFECT_SHORT_NAMES.getOrDefault(effect.method_5579(), null);
            if (shortName == null) {
               String full = ((class_1291)effect.method_5579().comp_349()).method_5560().getString();
               shortName = full.length() > 4 ? full.substring(0, 3) : full;
            }

            int amp = effect.method_5578() + 1;
            String tag = amp > 1 ? shortName + amp : shortName;
            parts.add(tag);
         }

         return class_124.field_1075 + String.join(" ", parts);
      }
   }

   private float getMaxWidth(NanoVGHelper helper, long vg, List<String> lines, float fontSize) {
      float width = 0.0F;

      for (String line : lines) {
         width = Math.max(width, helper.getTextWidth(vg, line, fontSize, FONT));
      }

      return width;
   }

   private double distanceScale(class_1657 entity, float partialTicks) {
      double dist = mc.field_1773.method_19418().method_19326().method_1022(entity.method_30950(partialTicks));
      return class_3532.method_15350(1.0 - dist * 0.01, 0.5, 2.147483647E9);
   }

   private String slotLetter(class_1304 slot) {
      return switch (slot) {
         case field_6169 -> "H";
         case field_6174 -> "C";
         case field_6172 -> "L";
         case field_6166 -> "B";
         default -> "?";
      };
   }
}
