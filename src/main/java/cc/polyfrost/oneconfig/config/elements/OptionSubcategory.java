package cc.polyfrost.oneconfig.config.elements;

import cc.polyfrost.oneconfig.gui.OneConfigGui;
import cc.polyfrost.oneconfig.gui.elements.config.ConfigPageButton;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.SearchUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OptionSubcategory {
   private final String name;
   public ArrayList<BasicOption> options = new ArrayList<>();
   public ArrayList<ConfigPageButton> topButtons = new ArrayList<>();
   public ArrayList<ConfigPageButton> bottomButtons = new ArrayList<>();
   private List<BasicOption> filteredOptions = new ArrayList<>();
   private int drawLastY;
   private final String category;

   public OptionSubcategory(String name, String category) {
      this.name = name;
      this.category = category;
   }

   public int draw(long vg, int x, int y, InputHandler inputHandler) {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      String filter = OneConfigGui.INSTANCE == null ? "" : OneConfigGui.INSTANCE.getSearchValue().toLowerCase().trim();
      boolean shouldNotFilter = filter.equals("") || SearchUtils.isSimilar(this.category, filter) || SearchUtils.isSimilar(this.name, filter);
      this.filteredOptions = this.options
         .stream()
         .filter(option -> !option.isHidden() && (shouldNotFilter || SearchUtils.isSimilar(option.name, filter)))
         .collect(Collectors.toList());
      List<ConfigPageButton> filteredTop = this.topButtons
         .stream()
         .filter(
            pagex -> !pagex.isHidden() && (shouldNotFilter || SearchUtils.isSimilar(pagex.name, filter) || SearchUtils.isSimilar(pagex.description, filter))
         )
         .collect(Collectors.toList());
      List<ConfigPageButton> filteredBottom = this.bottomButtons
         .stream()
         .filter(
            pagex -> !pagex.isHidden() && (shouldNotFilter || SearchUtils.isSimilar(pagex.name, filter) || SearchUtils.isSimilar(pagex.description, filter))
         )
         .collect(Collectors.toList());
      if (this.filteredOptions.size() == 0 && filteredTop.size() == 0 && filteredBottom.size() == 0) {
         return 0;
      } else {
         int optionY = y;
         if (!this.name.equals("")) {
            nanoVGHelper.drawText(vg, this.name, x, y + 12, Colors.WHITE_90, 24.0F, Fonts.MEDIUM);
            optionY = y + 36;
         }

         for (ConfigPageButton page : filteredTop) {
            page.draw(vg, x, optionY, inputHandler);
            optionY += page.getHeight() + 16;
         }

         if (this.filteredOptions.size() > 0) {
            int backgroundSize = 16;

            for (int i = 0; i < this.filteredOptions.size(); i++) {
               BasicOption option = this.filteredOptions.get(i);
               if (i + 1 < this.filteredOptions.size()) {
                  BasicOption nextOption = this.filteredOptions.get(i + 1);
                  if (option.size == 1 && nextOption.size == 1) {
                     backgroundSize += Math.max(option.getHeight(), nextOption.getHeight()) + 16;
                     i++;
                     continue;
                  }
               }

               backgroundSize += option.getHeight() + 16;
            }

            nanoVGHelper.drawRoundedRect(vg, x - 16, optionY, 1024.0F, backgroundSize, Colors.GRAY_900, 20.0F);
            optionY += 16;
         }

         this.drawLastY = optionY;
         if (this.filteredOptions.size() > 0) {
            for (int i = 0; i < this.filteredOptions.size(); i++) {
               BasicOption option = this.filteredOptions.get(i);
               option.draw(vg, x, optionY, inputHandler);
               if (i + 1 < this.filteredOptions.size()) {
                  BasicOption nextOption = this.filteredOptions.get(i + 1);
                  if (option.size == 1 && nextOption.size == 1) {
                     nextOption.draw(vg, x + 512, optionY, inputHandler);
                     optionY += Math.max(option.getHeight(), nextOption.getHeight()) + 16;
                     i++;
                     continue;
                  }
               }

               optionY += option.getHeight() + 16;
            }

            optionY += 16;
         }

         for (ConfigPageButton page : filteredBottom) {
            page.draw(vg, x, optionY, inputHandler);
            optionY += page.getHeight() + 16;
         }

         return optionY - y;
      }
   }

   public void drawLast(long vg, int x, InputHandler inputHandler) {
      for (int i = 0; i < this.filteredOptions.size(); i++) {
         BasicOption option = this.filteredOptions.get(i);
         option.drawLast(vg, x, this.drawLastY, inputHandler);
         if (i + 1 < this.filteredOptions.size()) {
            BasicOption nextOption = this.filteredOptions.get(i + 1);
            if (option.size == 1 && nextOption.size == 1) {
               nextOption.drawLast(vg, x + 512, this.drawLastY, inputHandler);
               this.drawLastY = this.drawLastY + Math.max(option.getHeight(), nextOption.getHeight()) + 16;
               i++;
               continue;
            }
         }

         this.drawLastY = this.drawLastY + option.getHeight() + 16;
      }
   }

   public String getName() {
      return this.name;
   }
}
