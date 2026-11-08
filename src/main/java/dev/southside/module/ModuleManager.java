package dev.southside.module;

import cc.polyfrost.oneconfig.config.Config;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.southside.Southside;
import dev.southside.gui.hud.ArrayListHud;
import dev.southside.module.impl.client.Account;
import dev.southside.module.impl.client.ClientSettings;
import dev.southside.module.impl.client.MCF;
import dev.southside.module.impl.client.Notification;
import dev.southside.module.impl.client.Renders;
import dev.southside.module.impl.client.TabGUI;
import dev.southside.module.impl.client.Targets;
import dev.southside.module.impl.client.Theme;
import dev.southside.module.impl.client.Watermark;
import dev.southside.module.impl.combat.AimAssist;
import dev.southside.module.impl.combat.AntiFireball;
import dev.southside.module.impl.combat.AutoClicker;
import dev.southside.module.impl.combat.AutoCrystal;
import dev.southside.module.impl.combat.AutoKnockBack;
import dev.southside.module.impl.combat.AutoRod;
import dev.southside.module.impl.combat.BackTrack;
import dev.southside.module.impl.combat.BedAura;
import dev.southside.module.impl.combat.BowAimAssist;
import dev.southside.module.impl.combat.BowSpammer;
import dev.southside.module.impl.combat.Criticals;
import dev.southside.module.impl.combat.GrimFull;
import dev.southside.module.impl.combat.HitBox;
import dev.southside.module.impl.combat.KBController;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.module.impl.combat.SuperKnockBack;
import dev.southside.module.impl.combat.TimerRange;
import dev.southside.module.impl.combat.TriggerBot;
import dev.southside.module.impl.combat.Velocity;
import dev.southside.module.impl.misc.AntiBot;
import dev.southside.module.impl.misc.AntiExploit;
import dev.southside.module.impl.misc.AntiStaff;
import dev.southside.module.impl.misc.AutoPixelParty;
import dev.southside.module.impl.misc.AutoPlay;
import dev.southside.module.impl.misc.AutoReply;
import dev.southside.module.impl.misc.BlockHunter;
import dev.southside.module.impl.misc.BundleDupe;
import dev.southside.module.impl.misc.ChatBypass;
import dev.southside.module.impl.misc.ClientSpoof;
import dev.southside.module.impl.misc.Disabler;
import dev.southside.module.impl.misc.DiscordPresence;
import dev.southside.module.impl.misc.FakeLag;
import dev.southside.module.impl.misc.FuckOffHand;
import dev.southside.module.impl.misc.GodItemDetector;
import dev.southside.module.impl.misc.HackerDetector;
import dev.southside.module.impl.misc.MatchingBypass;
import dev.southside.module.impl.misc.MiniGame;
import dev.southside.module.impl.misc.MurderFinder;
import dev.southside.module.impl.misc.ServerCrasher;
import dev.southside.module.impl.misc.Teams;
import dev.southside.module.impl.movement.FastWeb;
import dev.southside.module.impl.movement.Flight;
import dev.southside.module.impl.movement.GrimFly;
import dev.southside.module.impl.movement.InventoryMove;
import dev.southside.module.impl.movement.KeepSprint;
import dev.southside.module.impl.movement.LongJump;
import dev.southside.module.impl.movement.NoJumpDelay;
import dev.southside.module.impl.movement.NoPush;
import dev.southside.module.impl.movement.NoSlow;
import dev.southside.module.impl.movement.NoSlowTest;
import dev.southside.module.impl.movement.Speed;
import dev.southside.module.impl.movement.Spider;
import dev.southside.module.impl.movement.Sprint;
import dev.southside.module.impl.movement.StrafeFix;
import dev.southside.module.impl.movement.TargetStrafe;
import dev.southside.module.impl.movement.Timer;
import dev.southside.module.impl.player.AntiVoid;
import dev.southside.module.impl.player.AntiWeb;
import dev.southside.module.impl.player.AutoBucket;
import dev.southside.module.impl.player.AutoOffhand;
import dev.southside.module.impl.player.AutoPot;
import dev.southside.module.impl.player.AutoSoup;
import dev.southside.module.impl.player.AutoTool;
import dev.southside.module.impl.player.AutoTotem;
import dev.southside.module.impl.player.AutoTrap;
import dev.southside.module.impl.player.AutoWeb;
import dev.southside.module.impl.player.Blink;
import dev.southside.module.impl.player.BlockIn;
import dev.southside.module.impl.player.Eagle;
import dev.southside.module.impl.player.GhostHand;
import dev.southside.module.impl.player.HealthBypass;
import dev.southside.module.impl.player.InvManagerN;
import dev.southside.module.impl.player.KeyPearl;
import dev.southside.module.impl.player.NoFall;
import dev.southside.module.impl.player.StealerN;
import dev.southside.module.impl.player.Stuck;
import dev.southside.module.impl.render.BedShow;
import dev.southside.module.impl.render.Chatting;
import dev.southside.module.impl.render.ChestESP;
import dev.southside.module.impl.render.Compass;
import dev.southside.module.impl.render.CriticalParticle;
import dev.southside.module.impl.render.CustomFOV;
import dev.southside.module.impl.render.Detector;
import dev.southside.module.impl.render.ESP2D;
import dev.southside.module.impl.render.FakerVSBin;
import dev.southside.module.impl.render.FullBright;
import dev.southside.module.impl.render.Glow;
import dev.southside.module.impl.render.HUD;
import dev.southside.module.impl.render.InventoryHUDModule;
import dev.southside.module.impl.render.IronESP;
import dev.southside.module.impl.render.Item2D;
import dev.southside.module.impl.render.ItemHighlight;
import dev.southside.module.impl.render.ItemHudModule;
import dev.southside.module.impl.render.ItemPhysics;
import dev.southside.module.impl.render.ItemTag;
import dev.southside.module.impl.render.KillEffect;
import dev.southside.module.impl.render.LineBreakFormattor;
import dev.southside.module.impl.render.NameProtect;
import dev.southside.module.impl.render.NameTag;
import dev.southside.module.impl.render.NoHurtCam;
import dev.southside.module.impl.render.NoRender;
import dev.southside.module.impl.render.OldHitting;
import dev.southside.module.impl.render.PlayerAppearance;
import dev.southside.module.impl.render.PotionEffects;
import dev.southside.module.impl.render.ScoreBoard;
import dev.southside.module.impl.render.TNTips;
import dev.southside.module.impl.render.TargetHUD;
import dev.southside.module.impl.render.Trajectories;
import dev.southside.module.impl.render.ViewClip;
import dev.southside.module.impl.render.WorldManager;
import dev.southside.module.impl.world.AntiLiquid;
import dev.southside.module.impl.world.ChestAura;
import dev.southside.module.impl.world.ChestWreck;
import dev.southside.module.impl.world.EntityEditor;
import dev.southside.module.impl.world.FastPlace;
import dev.southside.module.impl.world.LightningTracker;
import dev.southside.module.impl.world.Scaffold;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Generated;
import net.minecraft.class_124;

public class ModuleManager {
   public final List<Module> modules = new ArrayList<>();
   public static final File CONFIG = Southside.FOLDER.toPath().resolve("module.json").toFile();

   public void initialize() {
      Southside.INSTANCE.getEventBus().register(this);
      this.add(new dev.southside.module.impl.client.ArrayList());
      this.add(new ClientSettings());
      this.add(new Notification());
      this.add(new Watermark());
      this.add(new Targets());
      this.add(new Renders());
      this.add(new TabGUI());
      this.add(new Account());
      this.add(new SuperKnockBack());
      this.add(new KBController());
      this.add(new AutoKnockBack());
      this.add(new AimAssist());
      this.add(new BowAimAssist());
      this.add(new BowSpammer());
      this.add(new AntiFireball());
      this.add(new AutoClicker());
      this.add(new AutoCrystal());
      this.add(new TimerRange());
      this.add(new TriggerBot());
      this.add(new BedAura());
      this.add(new Criticals());
      this.add(new BackTrack());
      this.add(new Velocity());
      this.add(new KillAura());
      this.add(new AutoSoup());
      this.add(new AutoPot());
      this.add(new AutoRod());
      this.add(new GrimFull());
      this.add(new HitBox());
      this.add(new DiscordPresence());
      this.add(new GodItemDetector());
      this.add(new HackerDetector());
      this.add(new AutoPixelParty());
      this.add(new MatchingBypass());
      this.add(new ServerCrasher());
      this.add(new FuckOffHand());
      this.add(new MurderFinder());
      this.add(new AntiExploit());
      this.add(new ClientSpoof());
      this.add(new BundleDupe());
      this.add(new ChatBypass());
      this.add(new AutoReply());
      this.add(new Disabler());
      this.add(new AutoPlay());
      this.add(new AntiStaff());
      this.add(new FakeLag());
      this.add(new AntiBot());
      this.add(new Teams());
      this.add(new MCF());
      this.add(new MiniGame());
      this.add(new BlockHunter());
      this.add(new InventoryMove());
      this.add(new NoJumpDelay());
      this.add(new KeepSprint());
      this.add(new StrafeFix());
      this.add(new TargetStrafe());
      this.add(new LongJump());
      this.add(new FastWeb());
      this.add(new Flight());
      this.add(new Spider());
      this.add(new NoSlow());
      this.add(new NoSlowTest());
      this.add(new NoPush());
      this.add(new Sprint());
      this.add(new Speed());
      this.add(new Timer());
      this.add(new GrimFly());
      this.add(new HealthBypass());
      this.add(new AutoOffhand());
      this.add(new InvManagerN());
      this.add(new AutoBucket());
      this.add(new GhostHand());
      this.add(new AutoTotem());
      this.add(new StealerN());
      this.add(new AutoTrap());
      this.add(new AntiVoid());
      this.add(new AutoTool());
      this.add(new KeyPearl());
      this.add(new AntiWeb());
      this.add(new AutoWeb());
      this.add(new NoFall());
      this.add(new Blink());
      this.add(new Stuck());
      this.add(new Eagle());
      this.add(new BlockIn());
      this.add(new LightningTracker());
      this.add(new AntiLiquid());
      this.add(new ChestWreck());
      this.add(new ChestAura());
      this.add(new FastPlace());
      this.add(new Scaffold());
      this.add(new EntityEditor());
      this.add(new LineBreakFormattor());
      this.add(new PlayerAppearance());
      this.add(new PotionEffects());
      this.add(new ItemHighlight());
      this.add(new Trajectories());
      this.add(new NameProtect());
      this.add(new ItemPhysics());
      this.add(new ScoreBoard());
      this.add(new FullBright());
      this.add(new OldHitting());
      this.add(new KillEffect());
      this.add(new CriticalParticle());
      this.add(new WorldManager());
      this.add(new TargetHUD());
      this.add(new Chatting());
      this.add(new ChestESP());
      this.add(new IronESP());
      this.add(new CustomFOV());
      this.add(new NoRender());
      this.add(new NoHurtCam());
      this.add(new ViewClip());
      this.add(new Detector());
      this.add(new NameTag());
      this.add(new ItemTag());
      this.add(new Item2D());
      this.add(new ItemHudModule());
      this.add(new BedShow());
      this.add(new Compass());
      this.add(new ESP2D());
      this.add(new Theme());
      this.add(new Glow());
      this.add(new HUD());
      this.add(new TNTips());
      this.add(new FakerVSBin());
      this.add(new InventoryHUDModule());

      for (Module module : this.modules) {
         module.preInitialize();
         if (module.isAutoEnable()) {
            module.setEnable(true);
         }
      }

      try {
         this.loadConfig();

         for (Module modulex : this.modules) {
            modulex.initialize();
            modulex.initPostRunnable();
         }
      } catch (IOException var3) {
         var3.printStackTrace();
      }
   }

   public void add(Module module) {
      if (this.get(module.getDisplayName()) != null) {
         Southside.LOGGER.warn("Duplicate module: {}", module.getDisplayName());
      }

      this.modules.add(module);
   }

   public <T extends Module> T get(Class<T> clazz) {
      return (T)this.modules.stream().filter(p -> p.getClass().equals(clazz)).findFirst().orElseThrow();
   }

   public Module get(String arg) {
      return this.modules.stream().filter(module -> module.getDisplayName().equalsIgnoreCase(arg)).findFirst().orElse(null);
   }

   public List<Module> getEnabledModules() {
      return this.modules.stream().filter(Config::isEnabled).toList();
   }

   public List<Module> getModulesByCategory(Category category) {
      return this.modules.stream().filter(module -> module.getCategory() == category).collect(Collectors.toCollection(ArrayList::new));
   }

   public <T extends Module> boolean isEnabled(Class<T> clazz) {
      return this.<T>get(clazz).isEnabled();
   }

   public void reInit(List<Module> modules) throws IOException {
      JsonElement element = JsonParser.parseReader(new BufferedReader(new FileReader(CONFIG)));
      if (!(element instanceof JsonNull)) {
         modules.forEach(module -> {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has(module.mod.name)) {
               module.load(obj);
            }
         });
      }
   }

   public void loadConfig() throws IOException {
      if (!CONFIG.exists()) {
         this.saveConfig();
      }

      JsonElement element = JsonParser.parseReader(new BufferedReader(new FileReader(CONFIG)));
      if (!(element instanceof JsonNull)) {
         JsonObject obj = element.getAsJsonObject();
         this.modules.forEach(module -> module.fromTag(obj));
         this.saveConfig();
      }
   }

   public void saveConfig() throws IOException {
      if (!CONFIG.exists()) {
         CONFIG.createNewFile();
      }

      JsonObject jsonObject = new JsonObject();

      for (Module module : Southside.INSTANCE.getModuleManager().getModules()) {
         JsonObject tag = module.toTag();
         jsonObject.add(module.mod.name, tag);
      }

      PrintWriter printWriter = new PrintWriter(new FileWriter(CONFIG));
      printWriter.println(Southside.PRETTY_GSON.toJson(jsonObject));
      printWriter.close();
   }

   public String format(Module module) {
      if (ArrayListHud.showSuffix.getValue()) {
         return module.getSuffix().isEmpty()
            ? module.getDisplayName()
            : String.format("%s %s%s", module.getDisplayName(), class_124.field_1080, module.getSuffix());
      } else {
         return module.getDisplayName();
      }
   }

   public String formatRaw(Module module) {
      if (ArrayListHud.showSuffix.getValue()) {
         return module.getSuffix().isEmpty() ? module.getDisplayName() : String.format("%s %s", module.getDisplayName(), module.getSuffix());
      } else {
         return module.getDisplayName();
      }
   }

   @Generated
   public List<Module> getModules() {
      return this.modules;
   }
}
