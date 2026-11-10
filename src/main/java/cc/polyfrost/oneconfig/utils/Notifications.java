package cc.polyfrost.oneconfig.utils;

import cc.polyfrost.oneconfig.gui.OneConfigGui;
import cc.polyfrost.oneconfig.gui.animations.Animation;
import cc.polyfrost.oneconfig.gui.animations.DummyAnimation;
import cc.polyfrost.oneconfig.gui.animations.EaseInOutQuad;
import cc.polyfrost.oneconfig.internal.assets.SVGs;
import cc.polyfrost.oneconfig.internal.config.Preferences;
import cc.polyfrost.oneconfig.internal.utils.Notification;
import cc.polyfrost.oneconfig.libs.universal.UMinecraft;
import cc.polyfrost.oneconfig.libs.universal.UResolution;
import cc.polyfrost.oneconfig.platform.Platform;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.asset.Icon;
import cc.polyfrost.oneconfig.utils.gui.GuiUtils;
import dev.southside.event.Stage;
import dev.southside.event.events.GameRendererDrawEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.orbit.EventHandler;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;

public final class Notifications {
   public static final Notifications INSTANCE = new Notifications();
   private final LinkedHashMap<Notification, Animation> notifications = new LinkedHashMap<>();
   private final float DEFAULT_DURATION = 4000.0F;
   private float deltaTime = 0.0F;
   private Animation dummyAnimation;
   private static final Icon DEFAULT_ICON = new Icon(SVGs.ONECONFIG_HEAD_DARK);
   private static final File crashPatchSkyClientFile = new File("./OneConfig/CrashPatch/SKYCLIENT");
   private static final File oldCrashPatchSkyClientFile = new File("./W-OVERFLOW/CrashPatch/SKYCLIENT");

   private Notifications() {
   }

   public void send(String title, String message, @Nullable Icon icon, float duration, @Nullable Callable<Float> progressbar, @Nullable Runnable action) {
      Notification notification = new Notification(title, message, icon, duration, progressbar, action);
      this.notifications.put(notification, new DummyAnimation(-1.0F));
   }

   public void send(String title, String message, float duration, @Nullable Callable<Float> progressbar, @Nullable Runnable action) {
      this.send(title, message, null, duration, progressbar, action);
   }

   public void send(String title, String message, @Nullable Icon icon, float duration, @Nullable Callable<Float> progressbar) {
      this.send(title, message, icon, duration, progressbar, null);
   }

   public void send(String title, String message, @Nullable Icon icon, float duration, @Nullable Runnable action) {
      this.send(title, message, icon, duration, null, action);
   }

   public void send(String title, String message, float duration, @Nullable Callable<Float> progressbar) {
      this.send(title, message, duration, progressbar, null);
   }

   public void send(String title, String message, float duration, @Nullable Runnable action) {
      this.send(title, message, duration, null, action);
   }

   public void send(String title, String message, @Nullable Icon icon, @Nullable Callable<Float> progressbar) {
      this.send(title, message, icon, 4000.0F, progressbar);
   }

   public void send(String title, String message, @Nullable Icon icon, @Nullable Runnable action) {
      this.send(title, message, icon, 4000.0F, action);
   }

   public void send(String title, String message, @Nullable Callable<Float> progressbar) {
      this.send(title, message, 4000.0F, progressbar);
   }

   public void send(String title, String message, @Nullable Icon icon, float duration) {
      this.send(title, message, icon, duration, (Callable<Float>)null);
   }

   public void send(String title, String message, @Nullable Runnable action) {
      this.send(title, message, 4000.0F, action);
   }

   public void send(String title, String message, float duration) {
      this.send(title, message, duration, (Callable<Float>)null);
   }

   public void send(String title, String message, @Nullable Icon icon) {
      this.send(title, message, icon, (Callable<Float>)null);
   }

   public void send(String title, String message) {
      this.send(title, message, (Callable<Float>)null);
   }

   @EventHandler
   private void onRenderEvent(GameRendererDrawEvent event) {
      if (event.stage == Stage.PRE) {
         this.deltaTime = this.deltaTime + GuiUtils.getDeltaTime();
      } else if (this.notifications.isEmpty()) {
         this.deltaTime = 0.0F;
      } else {
         NanoVGHelper.INSTANCE.setupAndDraw(vg -> {
            float desiredPosition = -16.0F;
            float scale = OneConfigGui.getNotificationScaleFactor();

            for (Entry<Notification, Animation> entry : this.notifications.entrySet()) {
               if (entry.getValue().getEnd() == -1.0F) {
                  entry.setValue(new DummyAnimation(desiredPosition));
               } else if (desiredPosition != entry.getValue().getEnd()) {
                  entry.setValue(new EaseInOutQuad(250, entry.getValue().get(0.0F), desiredPosition, false));
               }

               float height = entry.getKey().draw(vg, UResolution.getWindowHeight() / scale + entry.getValue().get(this.deltaTime), scale, this.deltaTime);
               desiredPosition -= height + 16.0F;
            }

            this.notifications.entrySet().removeIf(entryx -> ((Notification)entryx.getKey()).isFinished());
         });
         this.deltaTime = 0.0F;
      }
   }

   @EventHandler
   private void onTickEvent(TickEvent event) {
      if (Preferences.firstLaunch.getValue()
         && event.getState() == Stage.PRE
         && !(Platform.getGuiPlatform().getCurrentScreen() instanceof OneConfigGui)
         && Platform.getServerPlatform().doesPlayerExist()
         && UMinecraft.getMinecraft().player.age > 20) {
         Preferences.firstLaunch.setValue(false);
         Preferences.getInstance().save();
         this.dummyAnimation = new EaseInOutQuad(4000, 0.0F, 1.0F, false);
         this.send(
            "Welcome to Southside!",
            "Press '" + Preferences.INSTANCE.oneConfigKeyBind.getValue().getDisplay() + "' to open the Click GUI.",
            new Icon(SVGs.SOUTHSIDE),
            -1.0F,
            () -> {
               if (!(Platform.getGuiPlatform().getCurrentScreen() instanceof OneConfigGui) && !Preferences.oneconfigOpened) {
                  return 0.0F;
               } else {
                  Preferences.oneconfigOpened = true;
                  Preferences.getInstance().save();
                  return this.dummyAnimation.get(GuiUtils.getDeltaTime());
               }
            },
            () -> GuiUtils.displayScreen((Screen)OneConfigGui.create())
         );
      }
   }
}
