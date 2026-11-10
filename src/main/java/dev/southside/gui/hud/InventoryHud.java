package dev.southside.gui.hud;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import dev.southside.module.impl.render.InventoryHUDModule;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

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
   protected void draw(DrawContext context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
      if (mc.player != null) {
         if (!example && this.bindToTab != null && this.bindToTab.getValue()) {
            if (!mc.options.playerListKey.isPressed()) {
               return;
            }

            int contentWidth = 168;
            int titleH = this.showTitle != null && this.showTitle.getValue() ? 12 : 0;
            int contentHeight = 8 + titleH + 54 - 2;
            int scaledW = (int)(contentWidth * scale);
            int scaledH = (int)(contentHeight * scale);
            int screenW = mc.getWindow().getScaledWidth();
            int screenH = mc.getWindow().getScaledHeight();
            x = (screenW - scaledW) / 2.0F;
            y = screenH / 2.0F + 10.0F;
         }

         this.drawInventory(context, x, y, scale);
      }
   }

   private void drawInventory(DrawContext context, float x, float y, float scale) {
      PlayerInventory inv = mc.player.getInventory();
      boolean isSimple = InventoryHUDModule.style.isMode("Simple");
      int titleH = !isSimple && this.showTitle != null && this.showTitle.getValue() ? 12 : 0;
      int contentWidth = 168;
      int contentHeight = 8 + titleH + 54 - 2;
      MatrixStack mat = context.getMatrices();
      mat.push();
      mat.translate(x, y, 0.0F);
      mat.scale(scale, scale, 1.0F);
      if (isSimple) {
         context.fill(0, 0, contentWidth, contentHeight, -1275068416);
      } else {
         if (this.showBackground != null && this.showBackground.getValue()) {
            int alpha = this.bgAlpha != null ? this.bgAlpha.getValue().intValue() : 120;
            int bgColor = alpha << 24 | 1315860;
            context.fill(0, 0, contentWidth, contentHeight, bgColor);
            if (this.showTitle != null && this.showTitle.getValue()) {
               int titleBgAlpha = Math.min(alpha + 30, 255);
               int titleBg = titleBgAlpha << 24 | 1973790;
               context.fill(0, 0, contentWidth, 12, titleBg);
            }
         }

         if (this.showTitle != null && this.showTitle.getValue()) {
            context.drawText(mc.textRenderer, "Inventory", 4, 2, -1, false);
         }
      }

      float contentX = 4.0F;
      float contentY = 4 + titleH;

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            int slotIndex = 9 + row * 9 + col;
            ItemStack stack = inv.getStack(slotIndex);
            int slotX = (int)(contentX + col * 18);
            int slotY = (int)(contentY + row * 18);
            context.fill(slotX, slotY, slotX + 16, slotY + 16, 1073741824);
            if (!stack.isEmpty()) {
               context.drawItem(stack, slotX, slotY);
               context.drawStackOverlay(mc.textRenderer, stack, slotX, slotY);
            }
         }
      }

      mat.pop();
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
