package cc.polyfrost.oneconfig.platform.impl;

import cc.polyfrost.oneconfig.libs.universal.UMinecraft;
import cc.polyfrost.oneconfig.libs.universal.UScreen;
import cc.polyfrost.oneconfig.platform.GuiPlatform;
import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.random.Random;

public class GuiPlatformImpl implements GuiPlatform {
   @Override
   public Object getCurrentScreen() {
      return UScreen.getCurrentScreen();
   }

   @Override
   public void setCurrentScreen(Object screen) {
      UScreen.displayScreen((Screen)screen);
   }

   @Override
   public boolean isInChat() {
      return this.getCurrentScreen() instanceof ChatScreen;
   }

   @Override
   public boolean isInDebug() {
      return (Boolean)UMinecraft.getSettings().getReducedDebugInfo().getValue();
   }

   @Override
   public void playClickSound() {
      var soundInstance = new AbstractSoundInstance(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, SoundCategory.PLAYERS, Random.create(114514L)) {};
      UMinecraft.getMinecraft().getSoundManager().play(soundInstance);
   }
}
