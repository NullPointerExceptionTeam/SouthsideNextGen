package dev.southside.gui.hud;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import dev.southside.module.impl.render.InventoryHUDModule;
import net.minecraft.class_1661;
import net.minecraft.class_1799;
import net.minecraft.class_332;
import net.minecraft.class_4587;

public class InventoryHud extends Hud {
   public final Switch showBackground = new Switch("Background", true);
   public final Slider bgAlpha = new Slider("Background Alpha", 120, 0, 255, 1);
   public final Switch showTitle = new Switch("Show Title", true);
   public final Switch bindToTab = new Switch("Bind to Tab", false);
   private static final int SLOT_SIZE = 16;
   private static final int SLOT_GAP = 2;
   private static final int PADDING = 4;
   private static final int TITLE_HEIGHT = 12;
   private static final int COLS = 9;
   private static final int ROWS = 3;

   @Override
   protected void draw(class_332 context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
      if (mc.field_1724 != null) {
         if (!example && this.bindToTab != null && this.bindToTab.getValue()) {
            if (!mc.field_1690.field_1907.method_1434()) {
               return;
            }

            int contentWidth = 168;
            int titleH = this.showTitle != null && this.showTitle.getValue() ? 12 : 0;
            int contentHeight = 8 + titleH + 54 - 2;
            int scaledW = (int)(contentWidth * scale);
            int scaledH = (int)(contentHeight * scale);
            int screenW = mc.method_22683().method_4486();
            int screenH = mc.method_22683().method_4502();
            x = (screenW - scaledW) / 2.0F;
            y = screenH / 2.0F + 10.0F;
         }

         this.drawInventory(context, x, y, scale);
      }
   }

   private void drawInventory(class_332 context, float x, float y, float scale) {
      class_1661 inv = mc.field_1724.method_31548();
      boolean isSimple = InventoryHUDModule.style.isMode("Simple");
      int titleH = !isSimple && this.showTitle != null && this.showTitle.getValue() ? 12 : 0;
      int contentWidth = 168;
      int contentHeight = 8 + titleH + 54 - 2;
      class_4587 mat = context.method_51448();
      mat.method_22903();
      mat.method_46416(x, y, 0.0F);
      mat.method_22905(scale, scale, 1.0F);
      if (isSimple) {
         context.method_25294(0, 0, contentWidth, contentHeight, -1275068416);
      } else {
         if (this.showBackground != null && this.showBackground.getValue()) {
            int alpha = this.bgAlpha != null ? this.bgAlpha.getValue().intValue() : 120;
            int bgColor = alpha << 24 | 1315860;
            context.method_25294(0, 0, contentWidth, contentHeight, bgColor);
            if (this.showTitle != null && this.showTitle.getValue()) {
               int titleBgAlpha = Math.min(alpha + 30, 255);
               int titleBg = titleBgAlpha << 24 | 1973790;
               context.method_25294(0, 0, contentWidth, 12, titleBg);
            }
         }

         if (this.showTitle != null && this.showTitle.getValue()) {
            context.method_51433(mc.field_1772, "Inventory", 4, 2, -1, false);
         }
      }

      float contentX = 4.0F;
      float contentY = 4 + titleH;

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            int slotIndex = 9 + row * 9 + col;
            class_1799 stack = inv.method_5438(slotIndex);
            int slotX = (int)(contentX + col * 18);
            int slotY = (int)(contentY + row * 18);
            context.method_25294(slotX, slotY, slotX + 16, slotY + 16, 1073741824);
            if (!stack.method_7960()) {
               context.method_51427(stack, slotX, slotY);
               context.method_51431(mc.field_1772, stack, slotX, slotY);
            }
         }
      }

      mat.method_22909();
   }

   @Override
   protected float getWidth(float scale, boolean example) {
      int contentWidth = 168;
      return contentWidth * scale;
   }

   @Override
   protected float getHeight(float scale, boolean example) {
      boolean isSimple = InventoryHUDModule.style.isMode("Simple");
      int titleH = !isSimple && this.showTitle != null && this.showTitle.getValue() ? 12 : 0;
      int contentHeight = 8 + titleH + 54 - 2;
      return contentHeight * scale;
   }
}
