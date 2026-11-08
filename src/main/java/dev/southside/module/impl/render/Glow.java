package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.options.impl.Color;
import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.misc.GodItemDetector;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1429;
import net.minecraft.class_1542;
import net.minecraft.class_1569;
import net.minecraft.class_1657;
import net.minecraft.class_1738;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_268;
import net.minecraft.class_270;
import net.minecraft.class_3489;
import net.minecraft.class_640;
import net.minecraft.class_9282;
import org.jetbrains.annotations.Nullable;

public class Glow extends Module {
   private static final int DEFAULT_COLOR = -1;
   private static final String GOD_AXE_TAG = "一刀999!";
   private final Dropdown mode = new Dropdown("Mode", "Teams", "Teams", "Bedwars", "Custom");
   private final Color teamsTeammateColor = new Color("Teammate Color", new OneColor(0, 255, 140));
   private final Color teamsEnemyColor = new Color("Enemy Color", new OneColor(255, 70, 70));
   private final Switch bedwarsUseArmorColor = new Switch("Use Armor Color", true);
   private final Color bedwarsFallbackColor = new Color("Fallback Color", new OneColor(255, 255, 255));
   private final Color customPlayerColor = new Color("Player Color", new OneColor(255, 255, 255));
   private final Color customAnimalColor = new Color("Animal Color", new OneColor(0, 255, 0));
   private final Color customMonsterColor = new Color("Monster Color", new OneColor(255, 0, 0));
   private final Color customItemColor = new Color("Item Color", new OneColor(255, 215, 0));
   private final Switch itemGlow = new Switch("Items Glow", false);
   private final Switch godItemGlow = new Switch("God Items", true);
   private final Switch bedwarsItemGlow = new Switch("Bedwars Items", false);

   public Glow() {
      super(Category.Render, "Glow", "实体描边发光");
   }

   @Override
   public void initPostRunnable() {
      super.initPostRunnable();
      this.addListener(this.mode.getLabel(), () -> this.setSuffix(this.mode.getMode()));
      this.hideIf(this.teamsTeammateColor.getLabel(), () -> !this.isTeamsMode());
      this.hideIf(this.teamsEnemyColor.getLabel(), () -> !this.isTeamsMode());
      this.hideIf(this.bedwarsUseArmorColor.getLabel(), () -> !this.isBedwarsMode());
      this.hideIf(this.bedwarsFallbackColor.getLabel(), () -> !this.isBedwarsMode());
      this.hideIf(this.customPlayerColor.getLabel(), () -> !this.isCustomMode());
      this.hideIf(this.customAnimalColor.getLabel(), () -> !this.isCustomMode());
      this.hideIf(this.customMonsterColor.getLabel(), () -> !this.isCustomMode());
      this.hideIf(this.customItemColor.getLabel(), () -> !this.isCustomMode());
      this.hideIf(this.godItemGlow.getLabel(), () -> !this.itemGlow.getValue());
      this.hideIf(this.bedwarsItemGlow.getLabel(), () -> !this.itemGlow.getValue());
      this.setSuffix(this.mode.getMode());
   }

   public Glow.Mode getMode() {
      if (this.mode.isMode("Bedwars")) {
         return Glow.Mode.BEDWARS;
      } else {
         return this.mode.isMode("Custom") ? Glow.Mode.CUSTOM : Glow.Mode.TEAMS;
      }
   }

   public boolean isTeamsMode() {
      return this.isEnabled() && this.getMode() == Glow.Mode.TEAMS;
   }

   public boolean isBedwarsMode() {
      return this.isEnabled() && this.getMode() == Glow.Mode.BEDWARS;
   }

   public boolean isCustomMode() {
      return this.isEnabled() && this.getMode() == Glow.Mode.CUSTOM;
   }

   public boolean shouldGlow(class_1297 entity) {
      if (!this.isEnabled() || entity == null) {
         return false;
      } else if (mc.field_1724 == null || entity == mc.field_1724) {
         return false;
      } else if (entity instanceof class_1657 player) {
         return player.method_5805();
      } else if (this.itemGlow.getValue() && entity instanceof class_1542 itemEntity) {
         return this.resolveItemColor(itemEntity) != null;
      } else {
         return !this.isCustomMode() || !(entity instanceof class_1429) && !(entity instanceof class_1569) ? false : ((class_1309)entity).method_5805();
      }
   }

   public int resolveOutlineColor(class_1297 entity) {
      if (entity == null) {
         return -1;
      } else if (entity instanceof class_1657 && GodItemDetector.hasTotemPopped(entity)) {
         return -65536;
      } else {
         if (entity instanceof class_1657 player) {
            java.awt.Color color = this.getPlayerColor(player);
            if (color != null) {
               return color.getRGB();
            }
         }

         if (this.itemGlow.getValue() && entity instanceof class_1542 itemEntity) {
            java.awt.Color color = this.resolveItemColor(itemEntity);
            if (color != null) {
               return color.getRGB();
            }
         }

         if (this.isCustomMode()) {
            if (entity instanceof class_1429) {
               return this.customAnimalColor.getValue().toJavaColor().getRGB();
            }

            if (entity instanceof class_1569) {
               return this.customMonsterColor.getValue().toJavaColor().getRGB();
            }
         }

         return this.fallbackColor(entity);
      }
   }

   @Nullable
   private java.awt.Color getPlayerColor(class_1657 player) {
      if (!player.method_5805()) {
         return null;
      } else {
         java.awt.Color godColor = this.getGodItemColor(player);
         if (godColor != null) {
            return godColor;
         } else {
            return switch (this.getMode()) {
               case TEAMS -> this.isTeamMate(player) ? this.teamsTeammateColor.getValue().toJavaColor() : this.teamsEnemyColor.getValue().toJavaColor();
               case BEDWARS -> this.bedwarsUseArmorColor.getValue() ? this.getArmorColor(player) : this.bedwarsFallbackColor.getValue().toJavaColor();
               case CUSTOM -> this.customPlayerColor.getValue().toJavaColor();
            };
         }
      }
   }

   private boolean isTeamMate(class_1657 player) {
      if (mc.field_1724 == null) {
         return false;
      } else {
         class_268 self = this.getListedTeam(mc.field_1724);
         class_268 other = this.getListedTeam(player);
         if (self != null && other != null) {
            if (self.method_1197().equals(other.method_1197())) {
               return true;
            }

            Integer selfColor = self.method_1202() != null ? self.method_1202().method_532() : null;
            Integer otherColor = other.method_1202() != null ? other.method_1202().method_532() : null;
            if (selfColor != null && selfColor.equals(otherColor)) {
               return true;
            }
         }

         return false;
      }
   }

   @Nullable
   private class_268 getListedTeam(class_1657 player) {
      if (mc.method_1562() == null) {
         return null;
      } else {
         class_640 entry = mc.method_1562().method_2871(player.method_5667());
         return entry != null ? entry.method_2955() : null;
      }
   }

   private int fallbackColor(class_1297 entity) {
      class_270 team = entity.method_5781();
      return team != null && team.method_1202().method_532() != null ? 0xFF000000 | team.method_1202().method_532() : -1;
   }

   @Nullable
   private java.awt.Color getArmorColor(class_1657 player) {
      for (int slot = 3; slot >= 2; slot--) {
         class_1799 stack = player.method_31548().method_7372(slot);
         if (!stack.method_7960() && stack.method_7909() instanceof class_1738) {
            int rgb = stack.method_31573(class_3489.field_48803) ? class_9282.method_57470(stack, 0) : 0;
            if (rgb != 0) {
               return new java.awt.Color(rgb | 0xFF000000, true);
            }
         }
      }

      return this.bedwarsFallbackColor.getValue().toJavaColor();
   }

   @Nullable
   private java.awt.Color resolveItemColor(class_1542 itemEntity) {
      if (!this.itemGlow.getValue()) {
         return null;
      } else {
         class_1799 stack = itemEntity.method_6983();
         if (stack.method_7960()) {
            return null;
         } else {
            if (this.godItemGlow.getValue()) {
               java.awt.Color godColor = this.getGodItemColor(stack);
               if (godColor != null) {
                  return godColor;
               }
            }

            if (this.bedwarsItemGlow.getValue()) {
               if (stack.method_31574(class_1802.field_8477)) {
                  return new java.awt.Color(120, 220, 255);
               }

               if (stack.method_31574(class_1802.field_8687)) {
                  return new java.awt.Color(0, 210, 80);
               }
            }

            return this.isCustomMode() ? this.customItemColor.getValue().toJavaColor() : null;
         }
      }
   }

   @Nullable
   private java.awt.Color getGodItemColor(class_1309 entity) {
      if (this.itemGlow.getValue() && this.godItemGlow.getValue()) {
         java.awt.Color main = this.getGodItemColor(entity.method_6047());
         return main != null ? main : this.getGodItemColor(entity.method_6079());
      } else {
         return null;
      }
   }

   @Nullable
   private java.awt.Color getGodItemColor(class_1799 stack) {
      if (stack.method_7960()) {
         return null;
      } else if (stack.method_31574(class_1802.field_8301)) {
         return new java.awt.Color(170, 60, 255);
      } else if (stack.method_31574(class_1802.field_8825) && stack.method_57353().method_57837() && stack.method_57353().toString().contains("一刀999!")) {
         return new java.awt.Color(255, 215, 0);
      } else if (stack.method_31574(class_1802.field_8288)) {
         return new java.awt.Color(255, 150, 60);
      } else {
         return stack.method_31574(class_1802.field_8777) ? new java.awt.Color(90, 255, 120) : null;
      }
   }

   public static enum Mode {
      TEAMS,
      BEDWARS,
      CUSTOM;
   }
}
