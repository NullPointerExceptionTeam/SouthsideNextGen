package dev.southside.compat;

import net.minecraft.class_437;

public class IASCompat {
   public static class_437 accountScreen(class_437 parent) {
      try {
         Class<?> accountScreenClass = Class.forName("ru.vidtu.ias.screen.AccountScreen");
         return (class_437)accountScreenClass.getConstructor(class_437.class).newInstance(parent);
      } catch (Exception var2) {
         return null;
      }
   }
}
