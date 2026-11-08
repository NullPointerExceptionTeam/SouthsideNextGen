package cc.polyfrost.oneconfig.gui.pages;

import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.config.elements.OptionCategory;
import cc.polyfrost.oneconfig.config.elements.OptionPage;
import cc.polyfrost.oneconfig.config.elements.OptionSubcategory;
import cc.polyfrost.oneconfig.gui.OneConfigGui;
import cc.polyfrost.oneconfig.gui.elements.BasicButton;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.SearchUtils;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class ModConfigPage extends Page {
   private final OptionPage page;
   private final List<BasicButton> categories = new ArrayList<>();
   private String selectedCategory;
   private int totalSize = 724;
   private final boolean base;
   private int categoryY = 0;
   private final ArrayList<OptionSubcategory> empty = new ArrayList<>();

   public ModConfigPage(OptionPage page, boolean base) {
      super(page.name);
      this.page = page;
      this.base = base;
      if (page.categories.size() != 0) {
         Iterator var3 = page.categories.keySet().iterator();
         if (var3.hasNext()) {
            String category = (String)var3.next();
            this.selectedCategory = category;
         }

         if (page.categories.size() >= 2) {
            for (String category : page.categories.keySet()) {
               BasicButton button = new BasicButton(0, 32, category, 2, ColorPalette.SECONDARY);
               button.setClickAction(() -> this.switchCategory(category));
               button.setToggleable(true);
               if (category.equals(this.selectedCategory)) {
                  button.setToggled(true);
               }

               this.categories.add(button);
            }
         }
      }
   }

   public ModConfigPage(OptionPage page) {
      this(page, false);
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler inputHandler) {
      if (this.page.categories.size() != 0) {
         ScissorHelper scissorHelper = ScissorHelper.INSTANCE;
         int optionY = y + (this.page.categories.size() == 1 ? 16 : 64) + this.categoryY;
         List<OptionSubcategory> subcategories = this.getSubcategories();

         for (OptionSubcategory subCategory : subcategories) {
            optionY += subCategory.draw(vg, x + 30, optionY, inputHandler);
         }

         scissorHelper.save();
         scissorHelper.clearScissors(vg);

         for (OptionSubcategory subCategory : subcategories) {
            subCategory.drawLast(vg, x + 30, inputHandler);
         }

         scissorHelper.restore(vg);
         this.totalSize = optionY - y;
      }
   }

   @Override
   public int drawStatic(long vg, int x, int y, InputHandler inputHandler) {
      this.categoryY = 0;
      if (this.categories.size() <= 1) {
         return 0;
      } else {
         int buttonX = x + 16;
         int returned = 0;
         boolean searching = !OneConfigGui.INSTANCE.getSearchValue().trim().isEmpty();

         for (BasicButton button : this.categories) {
            if (button.getWidth() == 0) {
               button.setWidth((int)(Math.ceil(NanoVGHelper.INSTANCE.getTextWidth(vg, button.getText(), 12.0F, Fonts.MEDIUM) / 8.0F) * 8.0 + 16.0));
            }

            if (buttonX + button.getWidth() >= x + 1024 - 16) {
               buttonX = x + 16;
               returned += 48;
               this.categoryY += 48;
            }

            if (searching) {
               boolean similar = SearchUtils.isSimilar(button.getText(), OneConfigGui.INSTANCE.getSearchValue());
               boolean selected = button.isToggled();
               button.setToggled(similar);
               button.draw(vg, buttonX, y + 16 + returned, inputHandler);
               buttonX += button.getWidth() + 16;
               button.setToggled(selected);
            } else {
               button.draw(vg, buttonX, y + 16 + returned, inputHandler);
               buttonX += button.getWidth() + 16;
            }
         }

         return 60 + returned;
      }
   }

   @Override
   public void finishUpAndClose() {
      this.page.mod.config.save();
      OneConfigGui.INSTANCE.closeColorSelector();

      for (OptionSubcategory subCategory : this.getSubcategories()) {
         for (BasicOption option : subCategory.options) {
            option.finishUpAndClose();
         }
      }
   }

   @Override
   public void keyTyped(int keyCode, int scanCode, int modifiers) {
      if (!this.page.categories.isEmpty()) {
         for (OptionSubcategory subCategory : this.getSubcategories()) {
            for (BasicOption option : subCategory.options) {
               option.keyTyped(keyCode, scanCode, modifiers);
            }
         }
      }
   }

   @Override
   public void charTyped(char c, int modifiers) {
      if (!this.page.categories.isEmpty()) {
         for (OptionSubcategory subCategory : this.getSubcategories()) {
            for (BasicOption option : subCategory.options) {
               option.charTyped(c, modifiers);
            }
         }
      }
   }

   @Override
   public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
      if (!this.page.categories.isEmpty()) {
         for (OptionSubcategory subCategory : this.getSubcategories()) {
            for (BasicOption option : subCategory.options) {
               option.mouseClicked(mouseX, mouseY, mouseButton);
            }
         }
      }
   }

   @Override
   public void mouseReleased(double mouseX, double mouseY, int state) {
      if (!this.page.categories.isEmpty()) {
         for (OptionSubcategory subCategory : this.getSubcategories()) {
            for (BasicOption option : subCategory.options) {
               option.mouseReleased(mouseX, mouseY, state);
            }
         }
      }
   }

   private List<OptionSubcategory> getSubcategories() {
      if (OneConfigGui.INSTANCE.getSearchValue().isEmpty()) {
         return this.page.categories.isEmpty() ? this.empty : this.page.categories.get(this.selectedCategory).subcategories;
      } else {
         List<OptionSubcategory> subcategories = new ArrayList<>();

         for (Entry<String, OptionCategory> category : this.page.categories.entrySet()) {
            if (SearchUtils.isSimilar(category.getKey(), OneConfigGui.INSTANCE.getSearchValue())) {
               subcategories.addAll(category.getValue().subcategories);
            } else {
               for (OptionSubcategory subcategory : category.getValue().subcategories) {
                  if (SearchUtils.isSimilar(subcategory.getName(), OneConfigGui.INSTANCE.getSearchValue())) {
                     subcategories.add(subcategory);
                  } else {
                     for (BasicOption option : subcategory.options) {
                        if (SearchUtils.isSimilar(option.name, OneConfigGui.INSTANCE.getSearchValue())) {
                           subcategories.add(subcategory);
                           break;
                        }
                     }
                  }
               }
            }
         }

         return subcategories;
      }
   }

   public void switchCategory(String newCategory) {
      if (this.page.categories.containsKey(newCategory)) {
         this.selectedCategory = newCategory;

         for (BasicButton button : this.categories) {
            if (!button.getText().equals(newCategory)) {
               button.setToggled(false);
               this.scrollTarget = 0.0F;
               this.scrollAnimation = null;
            }
         }

         OneConfigGui.INSTANCE.closeColorSelector();
      }
   }

   @Override
   public int getMaxScrollHeight() {
      return this.totalSize;
   }

   @Override
   public boolean isBase() {
      return this.base;
   }

   public OptionPage getPage() {
      return this.page;
   }

   public String getSelectedCategory() {
      return this.selectedCategory;
   }
}
