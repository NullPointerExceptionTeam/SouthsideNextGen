package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.options.impl.Color;
import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.misc.GodItemDetector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.component.type.DyedColorComponent;
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

   public boolean shouldGlow(Entity entity) {
      if (!this.isEnabled() || entity == null) {
         return false;
      } else if (mc.player == null || entity == mc.player) {
         return false;
      } else if (entity instanceof PlayerEntity player) {
         return player.isAlive();
      } else if (this.itemGlow.getValue() && entity instanceof ItemEntity itemEntity) {
         return this.resolveItemColor(itemEntity) != null;
      } else {
         return !this.isCustomMode() || !(entity instanceof AnimalEntity) && !(entity instanceof Monster) ? false : ((LivingEntity)entity).isAlive();
      }
   }

   public int resolveOutlineColor(Entity entity) {
      if (entity == null) {
         return -1;
      } else if (entity instanceof PlayerEntity && GodItemDetector.hasTotemPopped(entity)) {
         return -65536;
      } else {
         if (entity instanceof PlayerEntity player) {
            java.awt.Color color = this.getPlayerColor(player);
            if (color != null) {
               return color.getRGB();
            }
         }

         if (this.itemGlow.getValue() && entity instanceof ItemEntity itemEntity) {
            java.awt.Color color = this.resolveItemColor(itemEntity);
            if (color != null) {
               return color.getRGB();
            }
         }

         if (this.isCustomMode()) {
            if (entity instanceof AnimalEntity) {
               return this.customAnimalColor.getValue().toJavaColor().getRGB();
            }

            if (entity instanceof Monster) {
               return this.customMonsterColor.getValue().toJavaColor().getRGB();
            }
         }

         return this.fallbackColor(entity);
      }
   }

   @Nullable
   private java.awt.Color getPlayerColor(PlayerEntity player) {
      if (!player.isAlive()) {
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

   private boolean isTeamMate(PlayerEntity player) {
      if (mc.player == null) {
         return false;
      } else {
         Team self = this.getListedTeam(mc.player);
         Team other = this.getListedTeam(player);
         if (self != null && other != null) {
            if (self.getName().equals(other.getName())) {
               return true;
            }

            Integer selfColor = self.getColor() != null ? self.getColor().getColorValue() : null;
            Integer otherColor = other.getColor() != null ? other.getColor().getColorValue() : null;
            if (selfColor != null && selfColor.equals(otherColor)) {
               return true;
            }
         }

         return false;
      }
   }

   @Nullable
   private Team getListedTeam(PlayerEntity player) {
      if (mc.getNetworkHandler() == null) {
         return null;
      } else {
         PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
         return entry != null ? entry.getScoreboardTeam() : null;
      }
   }

   private int fallbackColor(Entity entity) {
      AbstractTeam team = entity.getScoreboardTeam();
      return team != null && team.getColor().getColorValue() != null ? 0xFF000000 | team.getColor().getColorValue() : -1;
   }

   @Nullable
   private java.awt.Color getArmorColor(PlayerEntity player) {
      for (int slot = 3; slot >= 2; slot--) {
         ItemStack stack = player.getInventory().getArmorStack(slot);
         if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem) {
            int rgb = stack.isIn(ItemTags.DYEABLE) ? DyedColorComponent.getColor(stack, 0) : 0;
            if (rgb != 0) {
               return new java.awt.Color(rgb | 0xFF000000, true);
            }
         }
      }

      return this.bedwarsFallbackColor.getValue().toJavaColor();
   }

   @Nullable
   private java.awt.Color resolveItemColor(ItemEntity itemEntity) {
      if (!this.itemGlow.getValue()) {
         return null;
      } else {
         ItemStack stack = itemEntity.getStack();
         if (stack.isEmpty()) {
            return null;
         } else {
            if (this.godItemGlow.getValue()) {
               java.awt.Color godColor = this.getGodItemColor(stack);
               if (godColor != null) {
                  return godColor;
               }
            }

            if (this.bedwarsItemGlow.getValue()) {
               if (stack.isOf(Items.DIAMOND)) {
                  return new java.awt.Color(120, 220, 255);
               }

               if (stack.isOf(Items.EMERALD)) {
                  return new java.awt.Color(0, 210, 80);
               }
            }

            return this.isCustomMode() ? this.customItemColor.getValue().toJavaColor() : null;
         }
      }
   }

   @Nullable
   private java.awt.Color getGodItemColor(LivingEntity entity) {
      if (this.itemGlow.getValue() && this.godItemGlow.getValue()) {
         java.awt.Color main = this.getGodItemColor(entity.getMainHandStack());
         return main != null ? main : this.getGodItemColor(entity.getOffHandStack());
      } else {
         return null;
      }
   }

   @Nullable
   private java.awt.Color getGodItemColor(ItemStack stack) {
      if (stack.isEmpty()) {
         return null;
      } else if (stack.isOf(Items.END_CRYSTAL)) {
         return new java.awt.Color(170, 60, 255);
      } else if (stack.isOf(Items.GOLDEN_AXE) && stack.getComponents().isEmpty() && stack.getComponents().toString().contains("一刀999!")) {
         return new java.awt.Color(255, 215, 0);
      } else if (stack.isOf(Items.TOTEM_OF_UNDYING)) {
         return new java.awt.Color(255, 150, 60);
      } else {
         return stack.isOf(Items.SLIME_BALL) ? new java.awt.Color(90, 255, 120) : null;
      }
   }

   public static enum Mode {
      TEAMS,
      BEDWARS,
      CUSTOM;
   }
}
