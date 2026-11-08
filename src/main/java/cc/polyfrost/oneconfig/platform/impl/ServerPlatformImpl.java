package cc.polyfrost.oneconfig.platform.impl;

import cc.polyfrost.oneconfig.libs.universal.UMinecraft;
import cc.polyfrost.oneconfig.platform.ServerPlatform;
import net.minecraft.class_746;

public class ServerPlatformImpl implements ServerPlatform {
   @Override
   public boolean inMultiplayer() {
      return UMinecraft.getWorld() != null && !UMinecraft.getMinecraft().method_1542();
   }

   @Override
   public String getServerBrand() {
      class_746 player = UMinecraft.getMinecraft().field_1724;
      return player == null ? null : player.field_3944.method_52790();
   }

   @Override
   public boolean doesPlayerExist() {
      return UMinecraft.getMinecraft().field_1724 != null;
   }
}
