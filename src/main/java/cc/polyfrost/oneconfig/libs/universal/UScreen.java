package cc.polyfrost.oneconfig.libs.universal;

import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.gui.screen.Screen;

public abstract class UScreen extends Screen {
   public static final UScreen.Companion Companion = new UScreen.Companion();
   private final boolean restoreCurrentGuiOnClose;
   private int newGuiScale;
   private String unlocalizedName;
   private int guiScaleToRestore;
   private final Screen screenToRestore;

   public final boolean getRestoreCurrentGuiOnClose() {
      return this.restoreCurrentGuiOnClose;
   }

   public int getNewGuiScale() {
      return this.newGuiScale;
   }

   public void setNewGuiScale(int newGuiScale) {
      this.newGuiScale = newGuiScale;
   }

   public String getUnlocalizedName() {
      return this.unlocalizedName;
   }

   public void setUnlocalizedName(String unlocalizedName) {
      this.unlocalizedName = unlocalizedName;
   }

   public final void init() {
      this.updateGuiScale();
      this.initScreen(this.width, this.height);
   }

   public final void render(DrawContext context, int mouseX, int mouseY, float partialTicks) {
      this.onDrawScreenCompat(new UMatrixStack(), context, mouseX, mouseY, partialTicks);
   }

   public final boolean charTyped(char chr, int modifiers) {
      this.onCharTyped(chr, modifiers);
      return true;
   }

   public final boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      this.onKeyPressed(keyCode, scanCode, modifiers);
      return true;
   }

   public final boolean keyReleased(int keyCode, int scanCode, int modifiers) {
      this.onKeyReleased(keyCode, scanCode, modifiers);
      return true;
   }

   public final boolean mouseClicked(double mouseX, double mouseY, int button) {
      this.onMouseClicked(mouseX, mouseY, button);
      return true;
   }

   public final boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.onMouseReleased(mouseX, mouseY, button);
      return true;
   }

   public final boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      this.onMouseDragged(mouseX, mouseY, button, deltaX, deltaY);
      return true;
   }

   public final boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      this.onMouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
      return true;
   }

   public final void tick() {
      this.onTick();
   }

   public final void close() {
      this.onScreenClose();
      super.close();
   }

   public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
      this.onDrawBackgroundCompat(new UMatrixStack(), context, mouseX, mouseY, delta);
   }

   public final void restorePreviousScreen() {
      Companion.displayScreen(this.screenToRestore);
   }

   public void updateGuiScale() {
   }

   public void initScreen(int width, int height) {
      super.init();
   }

   public void onDrawScreen(UMatrixStack matrixStack, DrawContext context, int mouseX, int mouseY, float partialTicks) {
      matrixStack.runWithGlobalState(() -> {
         super.render(context, mouseX, mouseY, partialTicks);
         return null;
      });
   }

   public void onDrawScreen(DrawContext context, int mouseX, int mouseY, float partialTicks) {
      this.onDrawScreen(UMatrixStack.Compat.INSTANCE.get(), context, mouseX, mouseY, partialTicks);
   }

   private final void onDrawScreenCompat(UMatrixStack matrixStack, DrawContext context, int mouseX, int mouseY, float partialTicks) {
      UMatrixStack.Compat.INSTANCE.runLegacyMethod(matrixStack, () -> {
         this.onDrawScreen(context, mouseX, mouseY, partialTicks);
         return null;
      });
   }

   public void onCharTyped(char c, int modifiers) {
      super.charTyped(c, modifiers);
   }

   public void onKeyPressed(int keyCode, int scanCode, int modifiers) {
      super.keyPressed(keyCode, scanCode, modifiers);
   }

   public void onKeyReleased(int keyCode, int scanCode, int modifiers) {
   }

   public void onMouseClicked(double mouseX, double mouseY, int mouseButton) {
      super.mouseClicked((int)mouseX, (int)mouseY, mouseButton);
   }

   public void onMouseReleased(double mouseX, double mouseY, int state) {
      super.mouseReleased(mouseX, mouseY, state);
   }

   public void onMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
   }

   public void onMouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
   }

   public void onTick() {
      super.tick();
   }

   public void onScreenClose() {
   }

   public boolean shouldPause() {
      return false;
   }

   public void onDrawBackground(UMatrixStack matrixStack, DrawContext context, int mouseX, int mouseY, float delta) {
      matrixStack.runWithGlobalState(() -> {
         if (this.client.world == null) {
            this.renderPanoramaBackground(context, delta);
         }

         return null;
      });
   }

   public void onDrawBackground(DrawContext context, int mouseX, int mouseY, float delta) {
      this.onDrawBackground(UMatrixStack.Compat.INSTANCE.get(), context, mouseX, mouseY, delta);
   }

   public final void onDrawBackgroundCompat(UMatrixStack matrixStack, DrawContext context, int mouseX, int mouseY, float delta) {
      UMatrixStack.Compat.INSTANCE.runLegacyMethod(matrixStack, () -> {
         this.onDrawBackground(context, mouseX, mouseY, delta);
         return null;
      });
   }

   public static Screen getCurrentScreen() {
      return Companion.getCurrentScreen();
   }

   public static void displayScreen(Screen screen) {
      Companion.displayScreen(screen);
   }

   public UScreen(boolean restoreCurrentGuiOnClose, int newGuiScale, String unlocalizedName) {
      super(Text.literal("one_config"));
      this.restoreCurrentGuiOnClose = restoreCurrentGuiOnClose;
      this.newGuiScale = newGuiScale;
      this.unlocalizedName = unlocalizedName;
      this.guiScaleToRestore = -1;
      this.screenToRestore = this.restoreCurrentGuiOnClose ? Companion.getCurrentScreen() : null;
   }

   public UScreen(boolean restoreCurrentGuiOnClose, int newGuiScale) {
      this(restoreCurrentGuiOnClose, newGuiScale, null);
   }

   public UScreen(boolean restoreCurrentGuiOnClose, UScreen.GuiScale newGuiScale) {
      this(restoreCurrentGuiOnClose, newGuiScale.ordinal());
   }

   public UScreen(boolean restoreCurrentGuiOnClose) {
      this(restoreCurrentGuiOnClose, 0, null);
   }

   public UScreen() {
      this(false, 0, null);
   }

   public static final class Companion {
      private Companion() {
      }

      public final Screen getCurrentScreen() {
         return UMinecraft.getMinecraft().currentScreen;
      }

      public final void displayScreen(Screen screen) {
         UMinecraft.getMinecraft().setScreen(screen);
      }
   }

   public static enum GuiScale {
      Auto,
      Small,
      Medium,
      Large,
      VeryLarge;

      private static final int guiScaleOverride = Integer.parseInt(System.getProperty("essential.guiScaleOverride", "-1"));

      public static UScreen.GuiScale fromNumber(int number) {
         return values()[number];
      }

      public static UScreen.GuiScale scaleForScreenSize() {
         return scaleForScreenSize(650);
      }

      public static UScreen.GuiScale scaleForScreenSize(int step) {
         if (guiScaleOverride != -1) {
            return fromNumber(MathHelper.clamp(guiScaleOverride, 0, 4));
         } else {
            int width = UResolution.getViewportWidth();
            int height = UResolution.getScaledHeight();
            int widthScale = MathHelper.clamp(width / step, 1, 4);
            int heightScale = MathHelper.clamp(height / (step / 16 * 9), 1, 4);
            return fromNumber(Math.min(widthScale, heightScale));
         }
      }
   }
}
