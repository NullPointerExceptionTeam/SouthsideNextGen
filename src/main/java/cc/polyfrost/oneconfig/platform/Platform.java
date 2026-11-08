package cc.polyfrost.oneconfig.platform;

import cc.polyfrost.oneconfig.platform.impl.GLPlatformImpl;
import cc.polyfrost.oneconfig.platform.impl.GuiPlatformImpl;
import cc.polyfrost.oneconfig.platform.impl.I18nPlatformImpl;
import cc.polyfrost.oneconfig.platform.impl.MousePlatformImpl;
import cc.polyfrost.oneconfig.platform.impl.PlatformImpl;
import cc.polyfrost.oneconfig.platform.impl.ServerPlatformImpl;

public interface Platform {
   static Platform getInstance() {
      return Platform.PlatformHolder.INSTANCE.platform;
   }

   static MousePlatform getMousePlatform() {
      return Platform.PlatformHolder.INSTANCE.mousePlatform;
   }

   static ServerPlatform getServerPlatform() {
      return Platform.PlatformHolder.INSTANCE.serverPlatform;
   }

   static GLPlatform getGLPlatform() {
      return Platform.PlatformHolder.INSTANCE.glPlatform;
   }

   static GuiPlatform getGuiPlatform() {
      return Platform.PlatformHolder.INSTANCE.guiPlatform;
   }

   static I18nPlatform getI18nPlatform() {
      return Platform.PlatformHolder.INSTANCE.i18nPlatform;
   }

   boolean isCallingFromMinecraftThread();

   int getMinecraftVersion();

   boolean isDevelopmentEnvironment();

   Platform.Loader getLoader();

   public static enum Loader {
      FORGE,
      FABRIC;
   }

   public static class PlatformHolder {
      static Platform.PlatformHolder INSTANCE = new Platform.PlatformHolder();
      Platform platform = new PlatformImpl();
      MousePlatform mousePlatform = new MousePlatformImpl();
      ServerPlatform serverPlatform = new ServerPlatformImpl();
      GLPlatform glPlatform = new GLPlatformImpl();
      GuiPlatform guiPlatform = new GuiPlatformImpl();
      I18nPlatform i18nPlatform = new I18nPlatformImpl();

      private PlatformHolder() {
      }
   }
}
