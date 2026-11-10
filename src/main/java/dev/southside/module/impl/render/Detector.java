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
import net.minecraft.util.Formatting;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.registry.entry.RegistryEntry;

public class Detector extends Module {
   private static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
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
   private static final Map<RegistryEntry<StatusEffect>, String> EFFECT_SHORT_NAMES = Map.of(
      StatusEffects.SPEED,
      "Spd",
      StatusEffects.STRENGTH,
      "Str",
      StatusEffects.RESISTANCE,
      "Res",
      StatusEffects.REGENERATION,
      "Reg",
      StatusEffects.FIRE_RESISTANCE,
      "FRes",
      StatusEffects.ABSORPTION,
      "Abs",
      StatusEffects.INVISIBILITY,
      "Inv",
      StatusEffects.JUMP_BOOST,
      "Jump",
      StatusEffects.SLOWNESS,
      "Slow",
      StatusEffects.WEAKNESS,
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
      if (mc.player != null && mc.world != null) {
         NanoVGHelper helper = NanoVGHelper.INSTANCE;
         helper.setupAndDraw(
            true,
            vg -> {
               for (PlayerEntity target : mc.world.getPlayers()) {
                  if (this.shouldRender(target) && !(mc.player.distanceTo(target) > this.renderRange.getValue().floatValue())) {
                     Vec2f anchor = this.getFeetScreenPos(target, event.getRenderTick());
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
                           float baseX = anchor.x - boxWidth / 2.0F;
                           float baseY = anchor.y + offsetYValue - boxHeight;
                           float scaleFactor = (float)(
                              this.customScale.getValue()
                                 ? this.scale.getDouble()
                                 : (this.distanceScale.getValue() ? this.distanceScale(target, event.getRenderTick()) : 1.0)
                           );
                           NanoUtil.scaleStart(anchor.x, anchor.y, scaleFactor);
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

   private boolean shouldRender(PlayerEntity player) {
      if (player == null || player == mc.player) {
         return false;
      } else {
         return player.isAlive() && !player.isSpectator() ? !this.respectTargets.getValue() || Targets.check(player) : false;
      }
   }

   private Vec2f getFeetScreenPos(PlayerEntity entity, float partialTicks) {
      Vec3d pos = entity.getLerpedPos(partialTicks).add(0.0, 0.05, 0.0);
      return ProjectionUtil.worldScreenPos(pos);
   }

   private List<String> buildLines(PlayerEntity player) {
      List<String> info = new ArrayList<>();
      List<String> godTags = this.buildGodItemTags(player);
      if (!godTags.isEmpty()) {
         info.add(String.join(" ", godTags));
      }

      if (this.showDistance.getValue()) {
         info.add(Formatting.GRAY + "Dist " + Formatting.WHITE + String.format("%.1fm", mc.player.distanceTo(player)));
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

   private List<String> buildGodItemTags(PlayerEntity player) {
      List<String> tags = new ArrayList<>();
      HackerDetector hackerDetector = Southside.get().getModuleManager().get(HackerDetector.class);
      if (hackerDetector.isEnabled() && hackerDetector.isHacker(player)) {
         tags.add(Formatting.DARK_RED + "[Hacker]" + Formatting.RESET);
      }

      if (!GodItemDetector.shouldShowTag(GodItemDetector.TagTarget.DETECTOR)) {
         return tags;
      } else {
         if (GodItemDetector.isGodAxe(player)) {
            tags.add(Formatting.GOLD + "[GodAxe]" + Formatting.RESET);
         }

         if (GodItemDetector.isMace(player)) {
            tags.add(Formatting.YELLOW + "[Mace]" + Formatting.RESET);
         }

         if (GodItemDetector.isEndCrystal(player)) {
            tags.add(Formatting.DARK_PURPLE + "[Crystal]" + Formatting.RESET);
         }

         if (GodItemDetector.isEGapple(player)) {
            tags.add(Formatting.LIGHT_PURPLE + "[GApple]" + Formatting.RESET);
         }

         return tags;
      }
   }

   private String formatProtection(PlayerEntity player) {
      int maxProt = 0;
      boolean hasArmor = false;

      for (EquipmentSlot slot : ARMOR_SLOTS) {
         ItemStack stack = player.getEquippedStack(slot);
         if (!stack.isEmpty()) {
            hasArmor = true;
            int level = EnchantmentUtils.getLevel(Enchantments.PROTECTION, stack);
            if (level > maxProt) {
               maxProt = level;
            }
         }
      }

      if (!hasArmor) {
         return Formatting.GRAY + "Prot " + Formatting.RED + "None";
      } else {
         Formatting color = maxProt >= 4 ? Formatting.RED : (maxProt >= 2 ? Formatting.YELLOW : Formatting.GREEN);
         return Formatting.GRAY + "Prot " + color + maxProt;
      }
   }

   private String formatSharpness(PlayerEntity player) {
      ItemStack mainHand = player.getMainHandStack();
      if (mainHand.isEmpty()) {
         return null;
      } else {
         int level = EnchantmentUtils.getLevel(Enchantments.SHARPNESS, mainHand);
         if (level <= 0) {
            return null;
         } else {
            Formatting color = level >= 5 ? Formatting.RED : (level >= 3 ? Formatting.YELLOW : Formatting.WHITE);
            return Formatting.GRAY + "Sharp " + color + level;
         }
      }
   }

   private String formatPotions(PlayerEntity player) {
      Collection<StatusEffectInstance> effects = player.getStatusEffects();
      if (effects.isEmpty()) {
         return null;
      } else {
         List<String> parts = new ArrayList<>();

         for (StatusEffectInstance effect : effects) {
            String shortName = EFFECT_SHORT_NAMES.getOrDefault(effect.getEffectType(), null);
            if (shortName == null) {
               String full = ((StatusEffect)effect.getEffectType().value()).getName().getString();
               shortName = full.length() > 4 ? full.substring(0, 3) : full;
            }

            int amp = effect.getAmplifier() + 1;
            String tag = amp > 1 ? shortName + amp : shortName;
            parts.add(tag);
         }

         return Formatting.AQUA + String.join(" ", parts);
      }
   }

   private float getMaxWidth(NanoVGHelper helper, long vg, List<String> lines, float fontSize) {
      float width = 0.0F;

      for (String line : lines) {
         width = Math.max(width, helper.getTextWidth(vg, line, fontSize, FONT));
      }

      return width;
   }

   private double distanceScale(PlayerEntity entity, float partialTicks) {
      double dist = mc.gameRenderer.getCamera().getPos().distanceTo(entity.getLerpedPos(partialTicks));
      return MathHelper.clamp(1.0 - dist * 0.01, 0.5, 2.147483647E9);
   }

   private String slotLetter(EquipmentSlot slot) {
      return switch (slot) {
         case HEAD -> "H";
         case CHEST -> "C";
         case LEGS -> "L";
         case FEET -> "B";
         default -> "?";
      };
   }
}
