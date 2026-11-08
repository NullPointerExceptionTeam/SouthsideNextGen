package dev.southside.module.impl.misc;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.PlayerUpdateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.client.Notification;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.misc.MSTimer;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;
import net.minecraft.class_1268;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_276;
import net.minecraft.class_318;
import net.minecraft.class_5498;
import net.minecraft.class_5904;

public class AutoPlay extends Module {
   public final Switch autoScreenShot = new Switch("AutoScreenShot", true);
   public final Switch screenCopy = new Switch("ScreenCopy", false);
   public final Switch autoQuit = new Switch("AutoJoin", true);
   public final Switch auto3rdPerson = new Switch("Auto3rdPerson", true);
   public Slider nextGameDelay = new Slider("NextGameDelay", 5, 0, 20, 1);
   private boolean waitingNextGame = false;
   private final MSTimer nextGameTimer = new MSTimer();
   private final MSTimer ScreenshotTimer = new MSTimer();
   private boolean ScreenshotScheduled = false;

   public AutoPlay() {
      super(Category.Misc, "AutoPlay", true);
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (mc.field_1687 != null) {
         if (event.getOrigin() == TransferOrigin.RECEIVE && event.packet instanceof class_5904 packet) {
            class_2561 text = packet.comp_2281();
            if (text.getString().contains("胜利")) {
               if (this.autoScreenShot.getValue()) {
                  if (this.auto3rdPerson.getValue()) {
                     mc.execute(() -> mc.field_1690.method_31043(class_5498.field_26666));
                  }

                  this.ScreenshotScheduled = true;
                  this.ScreenshotTimer.reset();
               }

               if (this.autoQuit.getValue()) {
                  Notification.addNotificationKeepTime(
                     "Will join next game after " + this.nextGameDelay.getValue() + "s",
                     "AutoPlay",
                     Notification.NotificationType.WARN,
                     this.nextGameDelay.getValue().doubleValue()
                  );
                  this.waitingNextGame = true;
                  this.nextGameTimer.reset();
               }
            } else if (text.getString().contains("观察者") && this.autoQuit.getValue()) {
               Notification.addNotificationKeepTime(
                  "Will join next game after " + this.nextGameDelay.getValue() + "s",
                  "AutoPlay",
                  Notification.NotificationType.WARN,
                  this.nextGameDelay.getValue().doubleValue()
               );
               this.waitingNextGame = true;
               this.nextGameTimer.reset();
            }
         }
      }
   }

   @EventHandler
   public void onUpdate(PlayerUpdateEvent event) {
      if (mc.field_1724 != null) {
         if (this.ScreenshotScheduled && this.ScreenshotTimer.hasTimePassed(500L)) {
            mc.execute(() -> {
               class_276 fb = mc.method_1522();
               String filename = "[AutoScreenshot]" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + ".jpg";
               class_318.method_22690(mc.field_1697, filename, fb, message -> {
                  mc.field_1705.method_1743().method_1812(message);
                  if (this.screenCopy.getValue()) {
                     this.copyScreenshotToClipboard(new File(mc.field_1697, "screenshots/" + filename));
                  }
               });
               if (this.auto3rdPerson.getValue()) {
                  mc.field_1690.method_31043(class_5498.field_26664);
               }
            });
            this.ScreenshotScheduled = false;
         }

         if (this.autoQuit.value && this.waitingNextGame && this.nextGameTimer.hasTimePassed(this.nextGameDelay.getValue().longValue() * 1000L)) {
            int playAgainSlot = this.findPlayAgainSlot();
            if (playAgainSlot != -1) {
               int previousSlot = mc.field_1724.method_31548().field_7545;
               mc.field_1724.method_31548().field_7545 = playAgainSlot;
               mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
               mc.field_1724.method_31548().field_7545 = previousSlot;
               Notification.addNotification("Used item to join next game", "AutoPlay", Notification.NotificationType.WARN);
            }

            this.waitingNextGame = false;
            this.nextGameTimer.reset();
         }
      }
   }

   private int findPlayAgainSlot() {
      if (mc.field_1724 == null) {
         return -1;
      } else {
         for (int i = 0; i < 9; i++) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (!stack.method_7960() && stack.method_7964().getString().contains("再来一局")) {
               return i;
            }
         }

         return -1;
      }
   }

   private void copyScreenshotToClipboard(File file) {
      EventQueue.invokeLater(() -> {
         try {
            BufferedImage image = ImageIO.read(file);
            if (image != null) {
               AutoPlay.TransferableImage transferable = new AutoPlay.TransferableImage(image);
               Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, null);
               Notification.addNotification("Screenshot copied to clipboard", "AutoPlay", Notification.NotificationType.INFO);
            }
         } catch (Exception var3) {
            Notification.addNotification("Failed to copy screenshot", "AutoPlay", Notification.NotificationType.WARN);
         }
      });
   }

   private record TransferableImage(BufferedImage image) implements Transferable {
      @Override
      public DataFlavor[] getTransferDataFlavors() {
         return new DataFlavor[]{DataFlavor.imageFlavor};
      }

      @Override
      public boolean isDataFlavorSupported(DataFlavor flavor) {
         return DataFlavor.imageFlavor.equals(flavor);
      }

      @Override
      public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
         if (!this.isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
         } else {
            return this.image;
         }
      }
   }
}
