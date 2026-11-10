package dev.southside;

import cc.polyfrost.oneconfig.events.EventManager;
import cc.polyfrost.oneconfig.internal.OneConfig;
import cc.polyfrost.oneconfig.renderer.asset.SVG;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.southside.command.CommandManager;
import dev.southside.config.ConfigManager;
import dev.southside.manager.CPSManager;
import dev.southside.manager.ChestManager;
import dev.southside.manager.FriendManager;
import dev.southside.manager.GPTManager;
import dev.southside.manager.StickerManager;
import dev.southside.module.ModuleManager;
import dev.southside.nanovg.NanoFontLoader;
import dev.southside.nanovg.NanoLoader;
import dev.southside.nel.NeteaseLauncher;
import dev.southside.orbit.EventBus;
import dev.southside.orbit.IEventBus;
import dev.southside.render.ProgressBarOverlay;
import dev.southside.render.RenderBuffers;
import dev.southside.render.pipeline.Pipelines;
import dev.southside.utils.gpt.GPTAPIUtils;
import dev.southside.utils.misc.AIWalkController;
import dev.southside.utils.misc.BlinkUtils;
import dev.southside.utils.player.BedwarsCheckUtils;
import dev.southside.utils.player.MovementUtils;
import dev.southside.utils.player.PlayerUtils;
import dev.southside.utils.render.TextReplacementUtils;
import dev.southside.utils.rotation.RotationUtils;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Generated;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Southside implements ClientModInitializer, MinecraftAccessor {
   public static String CLIENT_NAME = "Southside";
   public static SVG LOGO = new SVG("logo.svg");
   public static String CLIENT_VERSION = "Development";
   public static Logger LOGGER = LogManager.getLogger("Southside");
   public static final File FOLDER = new File(MinecraftClient.getInstance().runDirectory, "Southside-nextgen");
   public static Southside INSTANCE;
   public static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();
   private final IEventBus eventBus = new EventBus() {
      {
         this.registerLambdaFactory("", (lookupInMethod, klass) -> (Lookup)lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
      }
   };
   private ModuleManager moduleManager;
   private CommandManager commandManager;
   private ConfigManager configManager;
   private FriendManager friendManager;
   private ProgressBarOverlay progressBarOverlay;
   private CPSManager cpsManager;
   private ChestManager chestManager;
   private StickerManager stickerManager;
   private boolean isVeta = false;
   private final AtomicBoolean closed = new AtomicBoolean(false);

   public static Southside get() {
      return INSTANCE;
   }

   public Southside() {
      INSTANCE = this;
      if (!FOLDER.exists()) {
         FOLDER.mkdirs();
      }
   }

   public void onInitialize() {
      LOGGER.info("Start Initializing");
      NeteaseLauncher.get().killNEL();
      NeteaseLauncher.get().init();
      this.eventBus.subscribe(this);
      this.eventBus.register(BedwarsCheckUtils.INSTANCE);
      this.eventBus.register(MovementUtils.INSTANCE);
      this.eventBus.register(BlinkUtils.INSTANCE);
      this.eventBus.register(AIWalkController.INSTANCE);
      this.eventBus.register(CPSManager.INSTANCE);
      this.eventBus.register(GPTManager.INSTANCE);
      this.progressBarOverlay = new ProgressBarOverlay();
      this.eventBus.register(this.progressBarOverlay);
      Pipelines.init();
      NanoLoader.init();
      NanoFontLoader.registerFonts();
      RenderBuffers.init();
      this.moduleManager = new ModuleManager();
      this.moduleManager.initialize();
      this.configManager = new ConfigManager();
      this.configManager.refreshConfigs();
      this.commandManager = new CommandManager();
      this.commandManager.init();
      this.stickerManager = new StickerManager();
      this.friendManager = new FriendManager();
      this.chestManager = new ChestManager();
      this.eventBus.register(this.chestManager);
      GPTAPIUtils.initialize();
      new RotationUtils();
      new PlayerUtils().init();
      TextReplacementUtils.init();
      OneConfig.INSTANCE.init();
      LOGGER.info("Finish Initializing");
      Runtime.getRuntime().addShutdownHook(new Thread(this::closeClient));
   }

   public void closeClient() {
      if (this.closed.compareAndSet(false, true)) {
         EventManager.INSTANCE.preStop();

         try {
            this.moduleManager.saveConfig();
         } catch (IOException var2) {
            var2.printStackTrace();
         }

         NanoLoader.destroy();
         GPTAPIUtils.shutdown();
         EventManager.INSTANCE.postStop();
         NeteaseLauncher.get().stop();
      }
   }

   public void onInitializeClient() {
      LOGGER.info("3<=8");
   }

   @Generated
   public IEventBus getEventBus() {
      return this.eventBus;
   }

   @Generated
   public ModuleManager getModuleManager() {
      return this.moduleManager;
   }

   @Generated
   public CommandManager getCommandManager() {
      return this.commandManager;
   }

   @Generated
   public ConfigManager getConfigManager() {
      return this.configManager;
   }

   @Generated
   public FriendManager getFriendManager() {
      return this.friendManager;
   }

   @Generated
   public ProgressBarOverlay getProgressBarOverlay() {
      return this.progressBarOverlay;
   }

   @Generated
   public CPSManager getCpsManager() {
      return this.cpsManager;
   }

   @Generated
   public ChestManager getChestManager() {
      return this.chestManager;
   }

   @Generated
   public StickerManager getStickerManager() {
      return this.stickerManager;
   }

   @Generated
   public boolean isVeta() {
      return this.isVeta;
   }

   @Generated
   public AtomicBoolean getClosed() {
      return this.closed;
   }

   @Generated
   public void setVeta(boolean isVeta) {
      this.isVeta = isVeta;
   }
}
