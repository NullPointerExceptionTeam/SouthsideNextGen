package dev.southside.utils.misc;

import dev.southside.MinecraftAccessor;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import javax.imageio.ImageIO;
import net.minecraft.util.Identifier;
import net.minecraft.resource.Resource;

public class TrayUtils implements MinecraftAccessor {
   public static boolean checkTrayAvailable() {
      return SystemTray.getSystemTray() != null;
   }

   public static TrayIcon newTrayIcon(Identifier id, String name) {
      TrayIcon trayIcon = null;

      try {
         Image image1 = ImageIO.read(((Resource)mc.getResourceManager().getResource(id).orElse(null)).getInputStream());
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
