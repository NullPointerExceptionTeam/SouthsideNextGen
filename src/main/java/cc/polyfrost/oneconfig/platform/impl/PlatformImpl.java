package cc.polyfrost.oneconfig.platform.impl;

import cc.polyfrost.oneconfig.platform.Platform;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

public class PlatformImpl implements Platform {
   @Override
   public boolean isCallingFromMinecraftThread() {
      return MinecraftClient.getInstance().isOnThread();
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
