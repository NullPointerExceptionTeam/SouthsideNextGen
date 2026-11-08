package dev.southside.mixin;

import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.events.FramebufferDrawEvent;
import dev.southside.event.events.GameRendererDrawEvent;
import dev.southside.event.events.PacketProcessEvent;
import dev.southside.event.events.PlaceEvent;
import dev.southside.event.events.RotationAppliedEvent;
import dev.southside.event.events.SetScreenEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.event.events.WorldEvent;
import dev.southside.gui.SouthsideMenuScreen;
import dev.southside.manager.PacketOrderManager;
import dev.southside.manager.PingSpoofManager;
import dev.southside.manager.ServerPacketManager;
import dev.southside.mixininterfaces.IMinecraftClient;
import dev.southside.module.impl.client.ClientSettings;
import dev.southside.module.impl.render.OldHitting;
import dev.southside.utils.FuckMixin;
import dev.southside.utils.MinecraftTickTimerUtils;
import dev.southside.utils.player.PlayerUtils;
import dev.southside.utils.raytrace.ClientRayTraceUtil;
import dev.southside.utils.rotation.RotationUtils;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.class_1041;
import net.minecraft.class_1144;
import net.minecraft.class_1156;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1812;
import net.minecraft.class_1829;
import net.minecraft.class_1839;
import net.minecraft.class_239;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_312;
import net.minecraft.class_315;
import net.minecraft.class_329;
import net.minecraft.class_333;
import net.minecraft.class_372;
import net.minecraft.class_4071;
import net.minecraft.class_429;
import net.minecraft.class_4325;
import net.minecraft.class_437;
import net.minecraft.class_4394;
import net.minecraft.class_442;
import net.minecraft.class_4749;
import net.minecraft.class_500;
import net.minecraft.class_525;
import net.minecraft.class_542;
import net.minecraft.class_634;
import net.minecraft.class_636;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_757;
import net.minecraft.class_761;
import net.minecraft.class_239.class_240;
import net.minecraft.class_9779.class_9781;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_310.class})
public abstract class MixinMinecraftClient implements IMinecraftClient {
   @Shadow
   public class_437 field_1755;
   @Final
   @Shadow
   private static Logger field_1762;
   @Shadow
   public int field_1771;
   @Shadow
   public int field_1752;
   @Shadow
   @Final
   private class_1041 field_1704;
   @Shadow
   Thread field_1696;
   @Shadow
   private boolean field_46551;
   @Shadow
   public boolean field_1743;
   @Final
   @Shadow
   private class_1144 field_1727;
   @Shadow
   @Nullable
   public class_746 field_1724;
   @Shadow
   @Nullable
   public class_239 field_1765;
   @Shadow
   @Final
   public class_315 field_1690;
   @Shadow
   @Final
   public class_9781 field_52750;
   @Shadow
   @Final
   public class_757 field_1773;
   @Shadow
   @Final
   public class_761 field_1769;
   @Shadow
   @Final
   public class_329 field_1705;
   @Shadow
   @Final
   private static class_2561 field_26841;
   @Shadow
   @Final
   private class_333 field_39769;
   @Shadow
   @Nullable
   private class_372 field_26843;
   @Shadow
   @Final
   private class_1156 field_1758;
   @Shadow
   @Nullable
   public class_636 field_1761;
   @Shadow
   @Nullable
   private class_4071 field_18175;
   @Shadow
   @Final
   public class_312 field_1729;
   @Shadow
   @Nullable
   public class_638 field_1687;
   @Unique
   private AtomicBoolean currentIsInGame;
   @Unique
   private AtomicBoolean isLoaded;
   @Unique
   private AtomicInteger startTick;

   @Shadow
   public abstract class_1041 method_22683();

   @Shadow
   protected abstract boolean method_1536();

   @Shadow
   @Nullable
   public abstract class_1297 method_1560();

   @Shadow
   public abstract boolean method_47392();

   @Shadow
   @Nullable
   public abstract class_634 method_1562();

   @Shadow
   protected abstract void method_29041(String var1);

   @Shadow
   protected abstract void method_1511();

   @Shadow
   protected abstract void method_1590(boolean var1);

   @Shadow
   public void method_1507(@Nullable class_437 screen) {
   }

   @Inject(
      method = {"<init>(Lnet/minecraft/class_542;)V"},
      at = {@At("TAIL")}
   )
   public void PostInit(class_542 args, CallbackInfo ci) {
      Southside.INSTANCE.onInitialize();
   }

   @Inject(
      method = {"method_1574()V"},
      at = {@At("HEAD")}
   )
   public void preTick(CallbackInfo ci) {
      MinecraftTickTimerUtils.setMultiplier(1.0F);
      Southside.INSTANCE.getEventBus().call(new TickEvent(Stage.PRE));
      PingSpoofManager.release();
      ServerPacketManager.onTick();
   }

   @Inject(
      method = {"method_1574()V"},
      at = {@At("TAIL")}
   )
   public void postTick(CallbackInfo ci) {
      Southside.INSTANCE.getEventBus().call(new TickEvent(Stage.POST));
      PacketOrderManager.resetAllState();
   }

   @Inject(
      method = {"method_1574()V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_757;method_3190(F)V",
         shift = Shift.BEFORE
      )}
   )
   public void hookRotation(CallbackInfo ci) {
      ClientRayTraceUtil.updateEyePos();
      RotationUtils.choose();
   }

   @Inject(
      method = {"method_1574()V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_310;method_1508()V",
         shift = Shift.BEFORE
      )}
   )
   public void hookRotationApplied(CallbackInfo ci) {
      ServerPacketManager.postTick();
      Southside.INSTANCE.getEventBus().call(new RotationAppliedEvent());
   }

   @Inject(
      at = {@At("HEAD")},
      method = {"method_1490()V"}
   )
   private void stop(CallbackInfo info) {
      try {
         Southside.INSTANCE.closeClient();
      } catch (Exception var3) {
         var3.printStackTrace();
      }
   }

   @Inject(
      method = {"method_1583()V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void doItemUse(CallbackInfo ci) {
      ci.cancel();
      FuckMixin.doItemUse();
   }

   @Inject(
      method = {"method_18097(Lnet/minecraft/class_638;)V"},
      at = {@At("HEAD")}
   )
   public void setWorld(class_638 world, CallbackInfo ci) {
      Southside.INSTANCE.getEventBus().call(new WorldEvent(world));
      PlayerUtils.serverSprinting = false;
      PingSpoofManager.packets.clear();
      ServerPacketManager.stop();
   }

   @Inject(
      method = {"method_1508()V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_746;method_6115()Z",
         shift = Shift.AFTER,
         ordinal = 0
      )}
   )
   private void handleInputEvents(CallbackInfo ci) {
      if (!this.field_1724.method_5765() && this.field_1765 != null && Southside.get().getModuleManager().isEnabled(OldHitting.class)) {
         class_1839 useAction = this.field_1724.method_6030().method_7976();
         if ((
               this.field_1724.method_6030().method_7909() instanceof class_1829
                  || useAction.equals(class_1839.field_8950)
                  || useAction.equals(class_1839.field_8953)
                  || this.field_1724.method_6030().method_7909() instanceof class_1812
            )
            && this.field_1690.field_1886.method_1434()
            && this.field_1765.method_17783() == class_240.field_1332) {
            this.field_1724.method_23667(class_1268.field_5808, false);
         }
      }
   }

   @Redirect(
      method = {"method_1574()V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_310;method_1508()V"
      )
   )
   private void ss$redirectHandleInputEvents(class_310 self) {
   }

   @Inject(
      method = {"method_1508()V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_746;method_6115()Z",
         ordinal = 0,
         shift = Shift.BEFORE
      )},
      cancellable = true
   )
   private void clickEvent(CallbackInfo ci) {
      if (Southside.get() != null && Southside.get().getEventBus() != null) {
         PlaceEvent event = new PlaceEvent();
         Southside.get().getEventBus().call(event);
         if (event.isCancelled()) {
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"method_1507(Lnet/minecraft/class_437;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void setScreen(class_437 screen, CallbackInfo ci) {
      class_437 currentScreen = this.field_1755;
      if (screen == null
         && !(currentScreen instanceof class_525)
         && !(currentScreen instanceof class_500)
         && !(currentScreen instanceof class_4749)
         && !(currentScreen instanceof class_4325)
         && !(currentScreen instanceof class_429)
         && !(currentScreen instanceof class_4394)) {
         if (this.currentIsInGame == null) {
            this.currentIsInGame = new AtomicBoolean(true);
         }

         if (!this.currentIsInGame.get()) {
            this.currentIsInGame.set(true);
         }
      }

      if (Southside.INSTANCE.getEventBus().call(new SetScreenEvent(screen)).isCancelled()) {
         ci.cancel();
      }
   }

   @ModifyVariable(
      method = {"method_1507(Lnet/minecraft/class_437;)V"},
      at = @At(
         value = "FIELD",
         target = "Lnet/minecraft/class_310;field_1755:Lnet/minecraft/class_437;",
         shift = Shift.AFTER
      ),
      ordinal = 0,
      argsOnly = true
   )
   private class_437 modifyScreen(class_437 screen) {
      return (class_437)(screen instanceof class_442 && Southside.get().getModuleManager().get(ClientSettings.class).mode.getMode().equals("Southside")
         ? new SouthsideMenuScreen()
         : screen);
   }

   @Shadow
   public void method_24288() {
   }

   @Inject(
      method = {"method_1523(Z)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_276;method_1237(II)V",
         shift = Shift.BEFORE
      )}
   )
   public void renderFramebufferBefore(CallbackInfo ci) {
      Southside.INSTANCE.getEventBus().call(new FramebufferDrawEvent(Stage.PRE));
   }

   @Inject(
      method = {"method_1523(Z)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_276;method_1237(II)V",
         shift = Shift.AFTER
      )}
   )
   public void renderFramebufferAfter(CallbackInfo ci) {
      Southside.INSTANCE.getEventBus().call(new FramebufferDrawEvent(Stage.POST));
   }

   @Inject(
      method = {"method_1523(Z)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_757;method_3192(Lnet/minecraft/class_9779;Z)V",
         shift = Shift.BEFORE
      )}
   )
   public void renderGameBefore(CallbackInfo ci) {
      Southside.INSTANCE.getEventBus().call(new GameRendererDrawEvent(Stage.PRE, this.field_52750.method_60637(false)));
   }

   @Inject(
      method = {"method_1523(Z)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_757;method_3192(Lnet/minecraft/class_9779;Z)V",
         shift = Shift.AFTER
      )}
   )
   public void renderGameAfter(CallbackInfo ci) {
      Southside.INSTANCE.getEventBus().call(new GameRendererDrawEvent(Stage.POST, this.field_52750.method_60637(false)));
   }

   @Inject(
      method = {"method_1574()V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1142;method_18669()V"
      )}
   )
   public void tick(CallbackInfo ci) {
      if (this.isLoaded == null) {
         this.isLoaded = new AtomicBoolean(false);
      }

      if (this.startTick == null) {
         this.startTick = new AtomicInteger(0);
      }

      if (!this.isLoaded.get()) {
         this.isLoaded.set(true);
      }

      if (this.startTick.get() <= 50) {
         this.startTick.getAndIncrement();
      }
   }

   @Inject(
      method = {"method_1523(Z)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_310;method_5383()V",
         shift = Shift.BEFORE
      )}
   )
   private void hookPacketProcess(CallbackInfo callbackInfo) {
      Southside.INSTANCE.getEventBus().call(new PacketProcessEvent());
   }

   @Override
   public boolean getCurrentIsInGame() {
      return this.currentIsInGame.get();
   }

   @Override
   public void setCurrentIsInGame(boolean currentIsInGame) {
      this.currentIsInGame.set(currentIsInGame);
   }

   @Override
   public boolean getIsLoaded() {
      return this.isLoaded.get();
   }

   @Override
   public int getStartTick() {
      return this.startTick.get();
   }

   @Override
   public void setAttackCooldown(int i) {
      this.field_1771 = i;
   }

   @Override
   public void leftClick() {
      this.method_1536();
   }
}
