package dev.southside.utils.misc;

import dev.southside.MinecraftAccessor;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import javax.imageio.ImageIO;
import net.minecraft.class_2960;
import net.minecraft.class_3298;

public class TrayUtils implements MinecraftAccessor {
   public static boolean checkTrayAvailable() {
      return SystemTray.getSystemTray() != null;
   }

   public static TrayIcon newTrayIcon(class_2960 id, String name) {
      TrayIcon trayIcon = null;

      try {
         Image image1 = ImageIO.read(((class_3298)mc.method_1478().method_14486(id).orElse(null)).method_14482());
         trayIcon = new TrayIcon(image1, name);
         trayIcon.setImageAutoSize(true);
      } catch (Exception var4) {
      }

      return trayIcon;
   }

   public static void addTrayIcon(SystemTray systemTray, TrayIcon trayIcon) {
      try {
         systemTray.add(trayIcon);
      } catch (Exception var3) {
      }
   }
}
