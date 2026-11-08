package cc.polyfrost.oneconfig.utils.gui;

import cc.polyfrost.oneconfig.gui.GuiPause;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cc.polyfrost.oneconfig.libs.universal.UResolution;
import cc.polyfrost.oneconfig.libs.universal.UScreen;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.utils.InputHandler;
import net.minecraft.class_332;
import org.jetbrains.annotations.NotNull;

public abstract class OneUIScreen extends UScreen implements GuiPause, BlurScreen {
   private final boolean useMinecraftScale;
   private final InputHandler inputHandler = new InputHandler();

   public OneUIScreen(boolean useMinecraftScale, boolean restoreGuiOnClose) {
      super(restoreGuiOnClose);
      this.useMinecraftScale = useMinecraftScale;
   }

   public OneUIScreen(boolean useMinecraftScale) {
      this(useMinecraftScale, false);
   }

   public OneUIScreen() {
      this(false, false);
   }

   @Override
   public final void onDrawScreen(@NotNull UMatrixStack matrixStack, class_332 context, int mouseX, int mouseY, float partialTicks) {
      super.onDrawScreen(matrixStack, context, mouseX, mouseY, partialTicks);
      if (this.useMinecraftScale) {
         this.inputHandler.scale(UResolution.getScaleFactor(), UResolution.getScaleFactor());
      }

      NanoVGHelper.INSTANCE.setupAndDraw(this.useMinecraftScale, vg -> this.draw(vg, partialTicks, this.inputHandler));
   }

   public abstract void draw(long var1, float var3, InputHandler var4);

   @Override
   public boolean hasBackgroundBlur() {
      return false;
   }

   @Override
   public boolean doesGuiPauseGame() {
      return false;
   }

   @Override
   public void onMouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      this.inputHandler.pushDWheel(verticalAmount * 40.0);
   }
}
