package cc.polyfrost.oneconfig.platform.impl;

import cc.polyfrost.oneconfig.platform.Platform;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_310;

public class PlatformImpl implements Platform {
   @Override
   public boolean isCallingFromMinecraftThread() {
      return class_310.method_1551().method_18854();
   }

   @Override
   public int getMinecraftVersion() {
      return 12040;
   }

   @Override
   public boolean isDevelopmentEnvironment() {
      return FabricLoader.getInstance().isDevelopmentEnvironment();
   }

   @Override
   public Platform.Loader getLoader() {
      return Platform.Loader.FABRIC;
   }
}
