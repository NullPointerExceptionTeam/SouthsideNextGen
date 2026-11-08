package dev.southside.utils;

import lombok.Generated;

public class MinecraftTickTimerUtils {
   private static float multiplier = 1.0F;

   public static void setMultiplier(float multiplier) {
      MinecraftTickTimerUtils.multiplier = multiplier;
   }

   @Generated
   public static float getMultiplier() {
      return multiplier;
   }
}
