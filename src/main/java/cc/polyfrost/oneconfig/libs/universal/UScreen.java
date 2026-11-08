package cc.polyfrost.oneconfig.libs.universal;

import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_437;

public abstract class UScreen extends class_437 {
   public static final UScreen.Companion Companion = new UScreen.Companion();
   private final boolean restoreCurrentGuiOnClose;
   private int newGuiScale;
   private String unlocalizedName;
   private int guiScaleToRestore;
   private final class_437 screenToRestore;

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

   public final void method_25426() {
      this.updateGuiScale();
      this.initScreen(this.field_22789, this.field_22790);
   }

   public final void method_25394(class_332 context, int mouseX, int mouseY, float partialTicks) {
      this.onDrawScreenCompat(new UMatrixStack(), context, mouseX, mouseY, partialTicks);
   }

   public final boolean method_25400(char chr, int modifiers) {
      this.onCharTyped(chr, modifiers);
      return true;
   }

   public final boolean method_25404(int keyCode, int scanCode, int modifiers) {
      this.onKeyPressed(keyCode, scanCode, modifiers);
      return true;
   }

   public final boolean method_16803(int keyCode, int scanCode, int modifiers) {
      this.onKeyReleased(keyCode, scanCode, modifiers);
      return true;
   }

   public final boolean method_25402(double mouseX, double mouseY, int button) {
      this.onMouseClicked(mouseX, mouseY, button);
      return true;
   }

   public final boolean method_25406(double mouseX, double mouseY, int button) {
      this.onMouseReleased(mouseX, mouseY, button);
      return true;
   }

   public final boolean method_25403(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      this.onMouseDragged(mouseX, mouseY, button, deltaX, deltaY);
      return true;
   }

   public final boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      this.onMouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
      return true;
   }

   public final void method_25393() {
      this.onTick();
   }

   public final void method_25419() {
      this.onScreenClose();
      super.method_25419();
   }

   public void method_25420(class_332 context, int mouseX, int mouseY, float delta) {
      this.onDrawBackgroundCompat(new UMatrixStack(), context, mouseX, mouseY, delta);
   }

   public final void restorePreviousScreen() {
      Companion.displayScreen(this.screenToRestore);
   }

   public void updateGuiScale() {
   }

   public void initScreen(int width, int height) {
      super.method_25426();
   }

   public void onDrawScreen(UMatrixStack matrixStack, class_332 context, int mouseX, int mouseY, float partialTicks) {
      matrixStack.runWithGlobalState(() -> {
         super.method_25394(context, mouseX, mouseY, partialTicks);
         return null;
      });
   }

   public void onDrawScreen(class_332 context, int mouseX, int mouseY, float partialTicks) {
      this.onDrawScreen(UMatrixStack.Compat.INSTANCE.get(), context, mouseX, mouseY, partialTicks);
   }

   private final void onDrawScreenCompat(UMatrixStack matrixStack, class_332 context, int mouseX, int mouseY, float partialTicks) {
      UMatrixStack.Compat.INSTANCE.runLegacyMethod(matrixStack, () -> {
         this.onDrawScreen(context, mouseX, mouseY, partialTicks);
         return null;
      });
   }

   public void onCharTyped(char c, int modifiers) {
      super.method_25400(c, modifiers);
   }

   public void onKeyPressed(int keyCode, int scanCode, int modifiers) {
      super.method_25404(keyCode, scanCode, modifiers);
   }

   public void onKeyReleased(int keyCode, int scanCode, int modifiers) {
   }

   public void onMouseClicked(double mouseX, double mouseY, int mouseButton) {
      super.method_25402((int)mouseX, (int)mouseY, mouseButton);
   }

   public void onMouseReleased(double mouseX, double mouseY, int state) {
      super.method_25406(mouseX, mouseY, state);
   }

   public void onMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      super.method_25403(mouseX, mouseY, button, deltaX, deltaY);
   }

   public void onMouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
   }

   public void onTick() {
      super.method_25393();
   }

   public void onScreenClose() {
   }

   public boolean method_25421() {
      return false;
   }

   public void onDrawBackground(UMatrixStack matrixStack, class_332 context, int mouseX, int mouseY, float delta) {
      matrixStack.runWithGlobalState(() -> {
         if (this.field_22787.field_1687 == null) {
            this.method_57728(context, delta);
         }

         return null;
      });
   }

   public void onDrawBackground(class_332 context, int mouseX, int mouseY, float delta) {
      this.onDrawBackground(UMatrixStack.Compat.INSTANCE.get(), context, mouseX, mouseY, delta);
   }

   public final void onDrawBackgroundCompat(UMatrixStack matrixStack, class_332 context, int mouseX, int mouseY, float delta) {
      UMatrixStack.Compat.INSTANCE.runLegacyMethod(matrixStack, () -> {
         this.onDrawBackground(context, mouseX, mouseY, delta);
         return null;
      });
   }

   public static class_437 getCurrentScreen() {
      return Companion.getCurrentScreen();
   }

   public static void displayScreen(class_437 screen) {
      Companion.displayScreen(screen);
   }

   public UScreen(boolean restoreCurrentGuiOnClose, int newGuiScale, String unlocalizedName) {
      super(class_2561.method_43470("one_config"));
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

      public final class_437 getCurrentScreen() {
         return UMinecraft.getMinecraft().field_1755;
      }

      public final void displayScreen(class_437 screen) {
         UMinecraft.getMinecraft().method_1507(screen);
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
            return fromNumber(class_3532.method_15340(guiScaleOverride, 0, 4));
         } else {
            int width = UResolution.getViewportWidth();
            int height = UResolution.getScaledHeight();
            int widthScale = class_3532.method_15340(width / step, 1, 4);
            int heightScale = class_3532.method_15340(height / (step / 16 * 9), 1, 4);
            return fromNumber(Math.min(widthScale, heightScale));
         }
      }
   }
}
