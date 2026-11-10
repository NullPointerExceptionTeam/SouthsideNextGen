package cc.polyfrost.oneconfig.platform.impl;

import cc.polyfrost.oneconfig.libs.universal.UMinecraft;
import cc.polyfrost.oneconfig.platform.ServerPlatform;
import net.minecraft.client.network.ClientPlayerEntity;

public class ServerPlatformImpl implements ServerPlatform {
   @Override
   public boolean inMultiplayer() {
      return UMinecraft.getWorld() != null && !UMinecraft.getMinecraft().isInSingleplayer();
   }

   @Override
   public String getServerBrand() {
      ClientPlayerEntity player = UMinecraft.getMinecraft().player;
      return player == null ? null : player.networkHandler.getBrand();
   }

   @Override
   public boolean doesPlayerExist() {
      return UMinecraft.getMinecraft().player != null;
   }
}
