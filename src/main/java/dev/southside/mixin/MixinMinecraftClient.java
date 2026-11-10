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
import net.minecraft.client.util.Window;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.tutorial.TutorialManager;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.item.PotionItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.hit.HitResult;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.toast.TutorialToast;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.realms.gui.screen.RealmsGenericErrorScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerWarningScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.RunArgs;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.client.render.RenderTickCounter.Dynamic;
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

@Mixin({MinecraftClient.class})
public abstract class MixinMinecraftClient implements IMinecraftClient {
   @Shadow
   public Screen currentScreen;
   @Final
   @Shadow
   private static Logger LOGGER;
   @Shadow
   public int attackCooldown;
   @Shadow
   public int itemUseCooldown;
   @Shadow
   @Final
   private Window window;
   @Shadow
   Thread thread;
   @Shadow
   private boolean disconnecting;
   @Shadow
   public boolean skipGameRender;
   @Final
   @Shadow
   private SoundManager soundManager;
   @Shadow
   @Nullable
   public ClientPlayerEntity player;
   @Shadow
   @Nullable
   public HitResult crosshairTarget;
   @Shadow
   @Final
   public GameOptions options;
   @Shadow
   @Final
   public Dynamic renderTickCounter;
   @Shadow
   @Final
   public GameRenderer gameRenderer;
   @Shadow
   @Final
   public WorldRenderer worldRenderer;
   @Shadow
   @Final
   public InGameHud inGameHud;
   @Shadow
   @Final
   private static Text SOCIAL_INTERACTIONS_NOT_AVAILABLE;
   @Shadow
   @Final
   private NarratorManager narratorManager;
   @Shadow
   @Nullable
   private TutorialToast socialInteractionsToast;
   @Shadow
   @Final
   private TutorialManager tutorialManager;
   @Shadow
   @Nullable
   public ClientPlayerInteractionManager interactionManager;
   @Shadow
   @Nullable
   private Overlay overlay;
   @Shadow
   @Final
   public Mouse mouse;
   @Shadow
   @Nullable
   public ClientWorld world;
   @Unique
   private AtomicBoolean currentIsInGame;
   @Unique
   private AtomicBoolean isLoaded;
   @Unique
   private AtomicInteger startTick;

   @Shadow
   public abstract Window getWindow();

   @Shadow
   protected abstract boolean doAttack();

   @Shadow
   @Nullable
   public abstract Entity getCameraEntity();

   @Shadow
   public abstract boolean isConnectedToLocalServer();

   @Shadow
   @Nullable
   public abstract ClientPlayNetworkHandler getNetworkHandler();

   @Shadow
   protected abstract void openChatScreen(String var1);

   @Shadow
   protected abstract void doItemPick();

   @Shadow
   protected abstract void handleBlockBreaking(boolean var1);

   @Shadow
   public void setScreen(@Nullable Screen screen) {
   }

   @Inject(
      method = {"<init>(Lnet/minecraft/client/RunArgs;)V"},
      at = {@At("TAIL")}
   )
   public void PostInit(RunArgs args, CallbackInfo ci) {
      Southside.INSTANCE.onInitialize();
   }

   @Inject(
      method = {"tick()V"},
      at = {@At("HEAD")}
   )
   public void preTick(CallbackInfo ci) {
      MinecraftTickTimerUtils.setMultiplier(1.0F);
      Southside.INSTANCE.getEventBus().call(new TickEvent(Stage.PRE));
      PingSpoofManager.release();
      ServerPacketManager.onTick();
   }

   @Inject(
      method = {"tick()V"},
      at = {@At("TAIL")}
   )
   public void postTick(CallbackInfo ci) {
      Southside.INSTANCE.getEventBus().call(new TickEvent(Stage.POST));
      PacketOrderManager.resetAllState();
   }

   @Inject(
      method = {"tick()V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/GameRenderer;updateCrosshairTarget(F)V",
         shift = Shift.BEFORE
      )}
   )
   public void hookRotation(CallbackInfo ci) {
      ClientRayTraceUtil.updateEyePos();
      RotationUtils.choose();
   }

   @Inject(
      method = {"tick()V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/MinecraftClient;handleInputEvents()V",
         shift = Shift.BEFORE
      )}
   )
   public void hookRotationApplied(CallbackInfo ci) {
      ServerPacketManager.postTick();
      Southside.INSTANCE.getEventBus().call(new RotationAppliedEvent());
   }

   @Inject(
      at = {@At("HEAD")},
      method = {"stop()V"}
   )
   private void stop(CallbackInfo info) {
      try {
         Southside.INSTANCE.closeClient();
      } catch (Exception var3) {
         var3.printStackTrace();
      }
   }

   @Inject(
      method = {"doItemUse()V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void doItemUse(CallbackInfo ci) {
      ci.cancel();
      FuckMixin.doItemUse();
   }

   @Inject(
      method = {"setWorld(Lnet/minecraft/client/world/ClientWorld;)V"},
      at = {@At("HEAD")}
   )
   public void setWorld(ClientWorld world, CallbackInfo ci) {
      Southside.INSTANCE.getEventBus().call(new WorldEvent(world));
      PlayerUtils.serverSprinting = false;
      PingSpoofManager.packets.clear();
      ServerPacketManager.stop();
   }

   @Inject(
      method = {"handleInputEvents()V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z",
         shift = Shift.AFTER,
         ordinal = 0
      )}
   )
   private void handleInputEvents(CallbackInfo ci) {
      if (!this.player.hasVehicle() && this.crosshairTarget != null && Southside.get().getModuleManager().isEnabled(OldHitting.class)) {
         UseAction useAction = this.player.getActiveItem().getUseAction();
         if ((
               this.player.getActiveItem().getItem() instanceof SwordItem
                  || useAction.equals(UseAction.EAT)
                  || useAction.equals(UseAction.BOW)
                  || this.player.getActiveItem().getItem() instanceof PotionItem
            )
            && this.options.attackKey.isPressed()
            && this.crosshairTarget.getType() == Type.BLOCK) {
            this.player.swingHand(Hand.MAIN_HAND, false);
         }
      }
   }

   @Redirect(
      method = {"tick()V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/MinecraftClient;handleInputEvents()V"
      )
   )
   private void ss$redirectHandleInputEvents(MinecraftClient self) {
   }

   @Inject(
      method = {"handleInputEvents()V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z",
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
      method = {"setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void setScreen(Screen screen, CallbackInfo ci) {
      Screen currentScreen = this.currentScreen;
      if (screen == null
         && !(currentScreen instanceof CreateWorldScreen)
         && !(currentScreen instanceof MultiplayerScreen)
         && !(currentScreen instanceof MultiplayerWarningScreen)
         && !(currentScreen instanceof RealmsMainScreen)
         && !(currentScreen instanceof OptionsScreen)
         && !(currentScreen instanceof RealmsGenericErrorScreen)) {
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
      method = {"setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"},
      at = @At(
         value = "FIELD",
         target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;",
         shift = Shift.AFTER
      ),
      ordinal = 0,
      argsOnly = true
   )
   private Screen modifyScreen(Screen screen) {
      return (Screen)(screen instanceof TitleScreen && Southside.get().getModuleManager().get(ClientSettings.class).mode.getMode().equals("Southside")
         ? new SouthsideMenuScreen()
         : screen);
   }

   @Shadow
   public void updateWindowTitle() {
   }

   @Inject(
      method = {"render(Z)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gl/Framebuffer;draw(II)V",
         shift = Shift.BEFORE
      )}
   )
   public void renderFramebufferBefore(CallbackInfo ci) {
      Southside.INSTANCE.getEventBus().call(new FramebufferDrawEvent(Stage.PRE));
   }

   @Inject(
      method = {"render(Z)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gl/Framebuffer;draw(II)V",
         shift = Shift.AFTER
      )}
   )
   public void renderFramebufferAfter(CallbackInfo ci) {
      Southside.INSTANCE.getEventBus().call(new FramebufferDrawEvent(Stage.POST));
   }

   @Inject(
      method = {"render(Z)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/GameRenderer;render(Lnet/minecraft/client/render/RenderTickCounter;Z)V",
         shift = Shift.BEFORE
      )}
   )
   public void renderGameBefore(CallbackInfo ci) {
      Southside.INSTANCE.getEventBus().call(new GameRendererDrawEvent(Stage.PRE, this.renderTickCounter.getTickDelta(false)));
   }

   @Inject(
      method = {"render(Z)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/GameRenderer;render(Lnet/minecraft/client/render/RenderTickCounter;Z)V",
         shift = Shift.AFTER
      )}
   )
   public void renderGameAfter(CallbackInfo ci) {
      Southside.INSTANCE.getEventBus().call(new GameRendererDrawEvent(Stage.POST, this.renderTickCounter.getTickDelta(false)));
   }

   @Inject(
      method = {"tick()V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/sound/MusicTracker;tick()V"
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
      method = {"render(Z)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/MinecraftClient;runTasks()V",
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
      this.attackCooldown = i;
   }

   @Override
   public void leftClick() {
      this.doAttack();
   }
}
