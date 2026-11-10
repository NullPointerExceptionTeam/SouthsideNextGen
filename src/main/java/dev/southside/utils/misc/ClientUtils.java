package dev.southside.utils.misc;

import net.minecraft.client.MinecraftClient;

public class ClientUtils {
   public static boolean updatingEntity = false;

   public static boolean nullCheck() {
      return MinecraftClient.getInstance() == null || MinecraftClient.getInstance().player == null || MinecraftClient.getInstance().world == null;
   }
}
