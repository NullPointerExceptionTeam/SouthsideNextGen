package dev.southside.utils.misc;

import net.minecraft.class_310;

public class ClientUtils {
   public static boolean updatingEntity = false;

   public static boolean nullCheck() {
      return class_310.method_1551() == null || class_310.method_1551().field_1724 == null || class_310.method_1551().field_1687 == null;
   }
}
