package cc.polyfrost.oneconfig.platform;

public interface GuiPlatform {
   Object getCurrentScreen();

   void setCurrentScreen(Object var1);

   boolean isInChat();

   boolean isInDebug();

   void playClickSound();
}
