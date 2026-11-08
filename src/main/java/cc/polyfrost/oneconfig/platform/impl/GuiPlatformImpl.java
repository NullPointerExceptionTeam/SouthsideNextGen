package cc.polyfrost.oneconfig.platform.impl;

import cc.polyfrost.oneconfig.libs.universal.UMinecraft;
import cc.polyfrost.oneconfig.libs.universal.UScreen;
import cc.polyfrost.oneconfig.platform.GuiPlatform;
import net.minecraft.class_1102;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import net.minecraft.class_408;
import net.minecraft.class_437;
import net.minecraft.class_5819;

public class GuiPlatformImpl implements GuiPlatform {
   @Override
   public Object getCurrentScreen() {
      return UScreen.getCurrentScreen();
   }

   @Override
   public void setCurrentScreen(Object screen) {
      UScreen.displayScreen((class_437)screen);
   }

   @Override
   public boolean isInChat() {
      return this.getCurrentScreen() instanceof class_408;
   }

   @Override
   public boolean isInDebug() {
      return (Boolean)UMinecraft.getSettings().method_42442().method_41753();
   }

   @Override
   public void playClickSound() {
      var soundInstance = new class_1102(class_3417.field_14791, class_3419.field_15248, class_5819.method_43049(114514L)) {};
      UMinecraft.getMinecraft().method_1483().method_4873(soundInstance);
   }
}
