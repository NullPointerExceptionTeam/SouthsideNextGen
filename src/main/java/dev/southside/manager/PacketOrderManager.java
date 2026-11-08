package dev.southside.manager;

import lombok.Generated;

public final class PacketOrderManager {
   public static boolean rightClicking;
   public static boolean swap;

   public static void resetAllState() {
      rightClicking = false;
      swap = false;
   }

   @Generated
   private PacketOrderManager() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
