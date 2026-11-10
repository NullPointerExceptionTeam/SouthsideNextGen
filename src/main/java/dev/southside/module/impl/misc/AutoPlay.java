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
import net.minecraft.util.Hand;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.client.option.Perspective;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;

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
      if (mc.world != null) {
         if (event.getOrigin() == TransferOrigin.RECEIVE && event.packet instanceof TitleS2CPacket packet) {
            Text text = packet.text();
            if (text.getString().contains("胜利")) {
               if (this.autoScreenShot.getValue()) {
                  if (this.auto3rdPerson.getValue()) {
                     mc.execute(() -> mc.options.setPerspective(Perspective.THIRD_PERSON_FRONT));
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
      if (mc.player != null) {
         if (this.ScreenshotScheduled && this.ScreenshotTimer.hasTimePassed(500L)) {
            mc.execute(() -> {
               Framebuffer fb = mc.getFramebuffer();
               String filename = "[AutoScreenshot]" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + ".jpg";
               ScreenshotRecorder.saveScreenshot(mc.runDirectory, filename, fb, message -> {
                  mc.inGameHud.getChatHud().addMessage(message);
                  if (this.screenCopy.getValue()) {
                     this.copyScreenshotToClipboard(new File(mc.runDirectory, "screenshots/" + filename));
                  }
               });
               if (this.auto3rdPerson.getValue()) {
                  mc.options.setPerspective(Perspective.FIRST_PERSON);
               }
            });
            this.ScreenshotScheduled = false;
         }

         if (this.autoQuit.value && this.waitingNextGame && this.nextGameTimer.hasTimePassed(this.nextGameDelay.getValue().longValue() * 1000L)) {
            int playAgainSlot = this.findPlayAgainSlot();
            if (playAgainSlot != -1) {
               int previousSlot = mc.player.getInventory().selectedSlot;
               mc.player.getInventory().selectedSlot = playAgainSlot;
               mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
               mc.player.getInventory().selectedSlot = previousSlot;
               Notification.addNotification("Used item to join next game", "AutoPlay", Notification.NotificationType.WARN);
            }

            this.waitingNextGame = false;
            this.nextGameTimer.reset();
         }
      }
   }

   private int findPlayAgainSlot() {
      if (mc.player == null) {
         return -1;
      } else {
         for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getName().getString().contains("再来一局")) {
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
