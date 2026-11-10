package dev.southside.compat;

import net.minecraft.client.gui.screen.Screen;

public class IASCompat {
   public static Screen accountScreen(Screen parent) {
      try {
         Class<?> accountScreenClass = Class.forName("ru.vidtu.ias.screen.AccountScreen");
         return (Screen)accountScreenClass.getConstructor(Screen.class).newInstance(parent);
      } catch (Exception var2) {
         return null;
      }
   }
}
