package cc.polyfrost.oneconfig.gui.elements;

import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.gui.OneConfigGui;
import cc.polyfrost.oneconfig.gui.animations.ColorAnimation;
import cc.polyfrost.oneconfig.gui.pages.ModConfigPage;
import cc.polyfrost.oneconfig.gui.pages.ModsPage;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.internal.assets.SVGs;
import cc.polyfrost.oneconfig.internal.config.OneConfigConfig;
import cc.polyfrost.oneconfig.internal.config.compatibility.forge.ForgeCompat;
import cc.polyfrost.oneconfig.internal.config.core.ConfigCore;
import cc.polyfrost.oneconfig.platform.Platform;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.renderer.scissor.Scissor;
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import cc.polyfrost.oneconfig.utils.color.ColorUtils;
import org.jetbrains.annotations.NotNull;

public class ModCard extends BasicElement {
   private final Mod modData;
   private final BasicButton favoriteButton = new BasicButton(32, 32, SVGs.HEART_OUTLINE, 2, ColorPalette.TERTIARY);
   private final ColorAnimation colorFrame = new ColorAnimation(ColorPalette.SECONDARY);
   private final ColorAnimation colorToggle;
   private boolean active;
   private boolean disabled;
   private boolean favorite;
   private boolean isHoveredMain = false;
   private final ModsPage page;

   public ModCard(@NotNull Mod mod, boolean active, boolean disabled, boolean favorite, ModsPage page) {
      super(244, 119, false);
      this.modData = mod;
      this.active = active;
      this.toggled = active;
      this.colorToggle = new ColorAnimation(active ? ColorPalette.PRIMARY : ColorPalette.SECONDARY);
      this.disabled = disabled;
      this.favorite = favorite;
      this.favoriteButton.setToggled(favorite);
      this.toggled = active;
      this.page = page;
   }

   @Override
   public void draw(long vg, float x, float y, InputHandler inputHandler) {
      super.update(x, y, inputHandler);
      ScissorHelper scissorHelper = ScissorHelper.INSTANCE;
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      String cleanName = this.modData.name.replaceAll("§.", "");
      Scissor scissor = scissorHelper.scissor(vg, x, y, this.width, this.height);
      this.isHoveredMain = inputHandler.isAreaHovered(x, y, this.width, 87.0F);
      boolean isHoveredSecondary = inputHandler.isAreaHovered(x, y + 87.0F, this.width - 32, 32.0F) && !this.disabled;
      if (this.disabled) {
         nanoVGHelper.setAlpha(vg, 0.5F);
      }

      nanoVGHelper.drawRoundedRectVaried(
         vg,
         x,
         y,
         this.width,
         87.0F,
         this.colorFrame.getColor(this.isHoveredMain, this.isHoveredMain && Platform.getMousePlatform().isButtonDown(0)),
         12.0F,
         12.0F,
         0.0F,
         0.0F
      );
      nanoVGHelper.drawRoundedRectVaried(
         vg,
         x,
         y + 87.0F,
         this.width,
         32.0F,
         this.colorToggle.getColor(isHoveredSecondary, isHoveredSecondary && Platform.getMousePlatform().isButtonDown(0)),
         0.0F,
         0.0F,
         12.0F,
         12.0F
      );
      nanoVGHelper.drawLine(vg, x, y + 86.0F, x + this.width, y + 86.0F, 2.0F, Colors.GRAY_300);
      this.drawIcon(nanoVGHelper, vg, x, y, cleanName);
      this.favoriteButton.draw(vg, x + 212.0F, y + 87.0F, inputHandler);
      this.favorite = this.favoriteButton.isToggled();
      if (this.favoriteButton.isClicked()) {
         if (this.favorite) {
            OneConfigConfig.favoriteMods.add(this.modData.name);
         } else {
            OneConfigConfig.favoriteMods.remove(this.modData.name);
         }

         ConfigCore.sortMods();
         this.page.reloadMods();
      }

      Scissor scissor2 = scissorHelper.scissor(vg, x, y + 87.0F, this.width - 32, 32.0F);
      nanoVGHelper.drawText(
         vg, cleanName, x + 12.0F, y + 103.0F, ColorUtils.setAlpha(Colors.WHITE, (int)(this.colorToggle.getAlpha() * 255.0F)), 14.0F, Fonts.MEDIUM
      );
      scissorHelper.resetScissor(vg, scissor2);
      if (this.favorite) {
         this.favoriteButton.setLeftIcon(SVGs.HEART_FILL);
      } else {
         this.favoriteButton.setLeftIcon(SVGs.HEART_OUTLINE);
      }

      if (this.clicked && this.isHoveredMain && !this.active) {
         this.toggled = false;
      }

      if (this.clicked && this.favoriteButton.hovered) {
         this.toggled = false;
      }

      if (this.clicked && !isHoveredSecondary && this.active) {
         this.toggled = true;
      }

      if (!this.active & this.disabled) {
         this.toggled = false;
      }

      if (this.active != this.toggled) {
         this.active = this.toggled;
         if (this.modData.config.canToggle) {
            this.colorToggle.setPalette(this.active ? ColorPalette.PRIMARY : ColorPalette.SECONDARY);
            this.modData.config.setEnable(this.active);
            this.modData.config.save();
         } else {
            this.toggled = this.active = true;
         }
      }

      nanoVGHelper.setAlpha(vg, 1.0F);
      scissorHelper.resetScissor(vg, scissor);
   }

   protected void drawIcon(NanoVGHelper nanoVGHelper, long vg, float x, float y, String cleanName) {
      int width = this.modData.iconWidth == -1 ? 56 : this.modData.iconWidth;
      int height = this.modData.iconHeight == -1 ? width : this.modData.iconHeight;
      if (this.modData.modIcon != null) {
         if (this.modData.modIcon.toLowerCase().endsWith(".svg")) {
            nanoVGHelper.drawSvg(vg, this.modData.modIcon, x + 122.0F - width / 2.0F, y + 43.0F - height / 2.0F, (float)width, (float)height);
         } else {
            nanoVGHelper.drawRoundImage(vg, this.modData.modIcon, x + 122.0F - width / 2.0F, y + 43.0F - height / 2.0F, (float)width, (float)height, 12.0F);
         }
      } else {
         nanoVGHelper.drawText(
            vg,
            cleanName,
            x + Math.max(0.0F, 244.0F - nanoVGHelper.getTextWidth(vg, cleanName, 16.0F, Fonts.MINECRAFT_BOLD)) / 2.0F,
            y + 44.0F,
            ColorUtils.setAlpha(Colors.WHITE, (int)(this.colorFrame.getAlpha() * 255.0F)),
            16.0F,
            Fonts.MINECRAFT_BOLD
         );
      }
   }

   @Override
   public void onClick() {
      if (this.isHoveredMain) {
         if (this.modData instanceof ForgeCompat.ForgeCompatMod) {
            Runnable runnable = ForgeCompat.compatMods.get(this.modData);
            if (runnable != null) {
               runnable.run();
               return;
            }
         }

         OneConfigGui.INSTANCE.openPage(new ModConfigPage(this.modData.defaultPage));
      }
   }

   public Mod getModData() {
      return this.modData;
   }

   @Override
   public boolean isDisabled() {
      return this.disabled;
   }

   public void setDisabled(boolean disabled) {
      this.disabled = disabled;
   }

   public boolean isActive() {
      return this.active;
   }

   public boolean isFavorite() {
      return this.favorite;
   }
}
