package cc.polyfrost.oneconfig.gui.pages;

import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.gui.OneConfigGui;
import cc.polyfrost.oneconfig.gui.elements.ModCard;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.internal.config.OneConfigConfig;
import cc.polyfrost.oneconfig.internal.config.core.ConfigCore;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.SearchUtils;
import dev.southside.module.Category;
import java.util.ArrayList;

public class ModsPage extends Page {
   private final ArrayList<ModCard> modCards = new ArrayList<>();
   private int size;
   private int selected;

   public ModsPage(int x) {
      super(getModType(x).name());
      this.reloadMods();
      this.selected = x;
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler inputHandler) {
      String filter = OneConfigGui.INSTANCE == null ? "" : OneConfigGui.INSTANCE.getSearchValue().toLowerCase().trim();
      int iX = x + 16;
      int iY = y + 16;

      for (ModCard modCard : new ArrayList<>(this.modCards)) {
         if (filter.isEmpty() && this.inSelection(modCard) || !filter.isEmpty() && SearchUtils.isSimilar(modCard.getModData().name, filter)) {
            if (iY + 135 >= y - this.scroll && iY <= y + 728 - this.scroll) {
               modCard.draw(vg, iX, iY, inputHandler);
            }

            iX += 260;
            if (iX > x + 796) {
               iX = x + 16;
               iY += 135;
            }
         }
      }

      this.size = iY - y + 135;
      if (iX == x + 16 && iY == y + 72) {
         NanoVGHelper.INSTANCE.drawText(vg, "Looks like there is nothing here. Try another category?", x + 16, y + 72, Colors.WHITE_60, 14.0F, Fonts.MEDIUM);
      }
   }

   @Override
   public int drawStatic(long vg, int x, int y, InputHandler inputHandler) {
      int iXCat = x + 16;
      boolean selected = false;
      return 0;
   }

   private boolean inSelection(ModCard modCard) {
      return this.selected == 0 && modCard.getModData().modType == Category.Client
         || this.selected == 1 && modCard.getModData().modType == Category.Combat
         || this.selected == 2 && modCard.getModData().modType == Category.Misc
         || this.selected == 3 && modCard.getModData().modType == Category.Movement
         || this.selected == 4 && modCard.getModData().modType == Category.Player
         || this.selected == 5 && modCard.getModData().modType == Category.Render
         || this.selected == 6 && modCard.getModData().modType == Category.World;
   }

   private static Category getModType(int x) {
      return switch (x) {
         case 1 -> Category.Combat;
         case 2 -> Category.Misc;
         case 3 -> Category.Movement;
         case 4 -> Category.Player;
         case 5 -> Category.Render;
         case 6 -> Category.World;
         default -> Category.Client;
      };
   }

   public void reloadMods() {
      this.modCards.clear();

      for (Mod modData : ConfigCore.mods) {
         ModCard modCard = new ModCard(
            modData, modData.config == null || modData.config.isEnabled(), false, OneConfigConfig.favoriteMods.contains(modData.name), this
         );
         modData.config.registerToggleListener(modCard::setToggled);
         this.modCards.add(modCard);
      }
   }

   @Override
   public int getMaxScrollHeight() {
      return this.size;
   }

   @Override
   public boolean isBase() {
      return true;
   }
}
