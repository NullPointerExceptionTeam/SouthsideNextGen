package dev.southside.module.impl.misc;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Text;
import dev.southside.Southside;
import dev.southside.discordipc.DiscordIPC;
import dev.southside.discordipc.RichPresence;
import dev.southside.event.Stage;
import dev.southside.event.events.SetScreenEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.math.RandomUtils;
import dev.southside.utils.network.NetPayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_3545;
import net.minecraft.class_3928;
import net.minecraft.class_404;
import net.minecraft.class_412;
import net.minecraft.class_4189;
import net.minecraft.class_420;
import net.minecraft.class_422;
import net.minecraft.class_426;
import net.minecraft.class_429;
import net.minecraft.class_440;
import net.minecraft.class_442;
import net.minecraft.class_443;
import net.minecraft.class_445;
import net.minecraft.class_446;
import net.minecraft.class_458;
import net.minecraft.class_4905;
import net.minecraft.class_500;
import net.minecraft.class_5235;
import net.minecraft.class_524;
import net.minecraft.class_525;
import net.minecraft.class_526;
import net.minecraft.class_5375;

public class DiscordPresence extends Module {
   public final Text line1String1 = new Text("Line 1 Text 1", "{player}", false, false, "First line message", 100, "{player}");
   public final Text line1String2 = new Text("Line 1 Text 2", "{server}", false, false, "First line message", 100, "{server}");
   private final Slider line1UpdateDelay = new Slider("Line 1 Update Delay", 200, 50, 1000);
   private final Dropdown line1SelectMode = new Dropdown("Line 1 Select Mode", "Sequential", "Sequential", "Random");
   public final Text line2String1 = new Text(
      "Line 2 Text 1", "Southside - {client.version}", false, false, "Second line message", 100, "Southside - {client.version}"
   );
   public final Text line2String2 = new Text("Line 2 Text 2", "{player.mode} Mode", false, false, "Second line message", 100, "{player.mode} Mode");
   public final Text line2String3 = new Text(
      "Line 2 Text 3", "Playing on {server.difficulty} difficulty.", false, false, "Second line message", 100, "Playing on {server.difficulty} difficulty."
   );
   public final Text line2String4 = new Text(
      "Line 2 Text 4", "{server.player_count} Players online", false, false, "Second line message", 100, "{server.player_count} Players online"
   );
   private final Slider line2UpdateDelay = new Slider("Line 2 Update Delay", 200, 50, 1000);
   private final Dropdown line2SelectMode = new Dropdown("Line 2 Select Mode", "Sequential", "Sequential", "Random");
   private static final RichPresence rpc = new RichPresence();
   private DiscordPresence.SmallImage currentSmallImage;
   private int ticks;
   private boolean forceUpdate;
   private boolean lastWasInMainMenu;
   private final List<String> line1Scripts = new ArrayList<>();
   private int line1Ticks;
   private int line1I;
   private final List<String> line2Scripts = new ArrayList<>();
   private int line2Ticks;
   private int line2I;
   public static final List<class_3545<String, String>> customStates = new ArrayList<>();

   public DiscordPresence() {
      super(Category.Misc, "Discord Presence", "南方Discord大王。");
   }

   public static void registerCustomState(String packageName, String state) {
      for (class_3545<String, String> pair : customStates) {
         if (((String)pair.method_15442()).equals(packageName)) {
            pair.method_34965(state);
            return;
         }
      }

      customStates.add(new class_3545(packageName, state));
   }

   public static void unregisterCustomState(String packageName) {
      customStates.removeIf(pair -> ((String)pair.method_15442()).equals(packageName));
   }

   @Override
   public void onEnable() {
      DiscordIPC.start(1407245483277287544L, null);
      rpc.setStart(System.currentTimeMillis() / 1000L);
      String largeText = "Southside nextgen 1.20.4";
      rpc.setLargeImage("southside", largeText);
      this.currentSmallImage = DiscordPresence.SmallImage.LecitronArts;
      this.recompileLine1();
      this.recompileLine2();
      this.ticks = 0;
      this.line1Ticks = 0;
      this.line2Ticks = 0;
      this.lastWasInMainMenu = false;
      this.line1I = 0;
      this.line2I = 0;
   }

   @Override
   public void onDisable() {
      DiscordIPC.stop();
   }

   private void recompile(List<String> messages, List<String> scripts) {
      scripts.clear();

      for (String message : messages) {
         scripts.add(message);
      }

      this.forceUpdate = true;
   }

   private void recompileLine1() {
      List<String> line1Messages = new ArrayList<>();
      line1Messages.add(
         this.line1String1.getValue() != null && !this.line1String1.getValue().isEmpty() ? this.line1String1.getValue() : this.line1String1.getDefaultValue()
      );
      line1Messages.add(
         this.line1String2.getValue() != null && !this.line1String2.getValue().isEmpty() ? this.line1String2.getValue() : this.line1String2.getDefaultValue()
      );
      this.recompile(line1Messages, this.line1Scripts);
   }

   private void recompileLine2() {
      List<String> line2Messages = new ArrayList<>();
      line2Messages.add(
         this.line2String1.getValue() != null && !this.line2String1.getValue().isEmpty() ? this.line2String1.getValue() : this.line2String1.getDefaultValue()
      );
      line2Messages.add(
         this.line2String2.getValue() != null && !this.line2String2.getValue().isEmpty() ? this.line2String2.getValue() : this.line2String2.getDefaultValue()
      );
      line2Messages.add(
         this.line2String3.getValue() != null && !this.line2String3.getValue().isEmpty() ? this.line2String3.getValue() : this.line2String3.getDefaultValue()
      );
      line2Messages.add(
         this.line2String4.getValue() != null && !this.line2String4.getValue().isEmpty() ? this.line2String4.getValue() : this.line2String4.getDefaultValue()
      );
      this.recompile(line2Messages, this.line2Scripts);
   }

   private String processPlaceholders(String message) {
      if (message == null) {
         return "Southside";
      } else if (message.isEmpty()) {
         return "Southside";
      } else {
         String result = message;
         if (Southside.INSTANCE != null) {
            result = message.replace("{client.version}", Southside.CLIENT_VERSION);
         }

         if (mc.method_1548() != null) {
            result = result.replace("{player}", mc.method_1548().method_1676());
         }

         if (mc.field_1724 != null) {
            result = result.replace("{player.mode}", mc.field_1724.method_7337() ? "Creative" : (mc.field_1724.method_7325() ? "Spectator" : "Survival"));
         }

         if (mc.method_1558() != null) {
            result = result.replace("{server}", mc.method_1558().field_3752);
         } else if (mc.method_1542()) {
            result = result.replace("{server}", "Singleplayer");
         } else {
            result = result.replace("{server}", "Main Menu");
         }

         if (mc.method_1562() != null && mc.method_1562().method_2880() != null) {
            int playerCount = mc.method_1562().method_2880().size();
            result = result.replace("{server.player_count}", String.valueOf(playerCount));
         } else {
            result = result.replace("{server.player_count}", "0");
         }

         if (mc.field_1687 != null) {
            result = result.replace("{server.difficulty}", mc.field_1687.method_8407().method_5460());
         } else {
            result = result.replace("{server.difficulty}", "Unknown");
         }

         return result != null && !result.isEmpty() ? result : "Southside User";
      }
   }

   @EventHandler
   private void onTick(TickEvent event) {
      if (event.state == Stage.POST) {
         boolean update = false;
         if (this.ticks < 200 && !this.forceUpdate) {
            this.ticks++;
         } else {
            this.currentSmallImage = this.currentSmallImage.next();
            this.currentSmallImage.apply();
            update = true;
            this.ticks = 0;
         }

         if (NetPayload.canUpdate()) {
            if (this.line1Ticks < this.line1UpdateDelay.getValue().intValue() && !this.forceUpdate) {
               this.line1Ticks++;
            } else {
               if (!this.line1Scripts.isEmpty()) {
                  int i = RandomUtils.nextInt(0, this.line1Scripts.size());
                  if (this.line1SelectMode.isMode("Sequential")) {
                     if (this.line1I >= this.line1Scripts.size()) {
                        this.line1I = 0;
                     }

                     i = this.line1I++;
                  }

                  String message = this.line1Scripts.get(i);
                  message = this.processPlaceholders(message);
                  if (message != null && !message.isEmpty()) {
                     rpc.setDetails(message);
                  } else {
                     rpc.setDetails("Southside User");
                  }
               }

               update = true;
               this.line1Ticks = 0;
            }

            if (this.line2Ticks < this.line2UpdateDelay.getValue().intValue() && !this.forceUpdate) {
               this.line2Ticks++;
            } else {
               if (!this.line2Scripts.isEmpty()) {
                  int ix = RandomUtils.nextInt(0, this.line2Scripts.size());
                  if (this.line2SelectMode.isMode("Sequential")) {
                     if (this.line2I >= this.line2Scripts.size()) {
                        this.line2I = 0;
                     }

                     ix = this.line2I++;
                  }

                  String message = this.line2Scripts.get(ix);
                  message = this.processPlaceholders(message);
                  if (message != null && !message.isEmpty()) {
                     rpc.setState(message);
                  } else {
                     rpc.setState("Southside Next Generation");
                  }
               }

               update = true;
               this.line2Ticks = 0;
            }
         } else if (!this.lastWasInMainMenu) {
            rpc.setDetails(mc.method_1548().method_1676());
            if (mc.field_1755 instanceof class_442) {
               rpc.setState("Looking at title screen");
            } else if (mc.field_1755 instanceof class_526) {
               rpc.setState("Selecting world");
            } else if (mc.field_1755 instanceof class_525 || mc.field_1755 instanceof class_5235) {
               rpc.setState("Creating world");
            } else if (mc.field_1755 instanceof class_524) {
               rpc.setState("Editing world");
            } else if (mc.field_1755 instanceof class_3928) {
               rpc.setState("Loading world");
            } else if (mc.field_1755 instanceof class_500) {
               rpc.setState("Selecting server");
            } else if (mc.field_1755 instanceof class_422) {
               rpc.setState("Adding server");
            } else if (mc.field_1755 instanceof class_412 || mc.field_1755 instanceof class_420) {
               rpc.setState("Connecting to server");
            } else if (mc.field_1755 instanceof class_429
               || mc.field_1755 instanceof class_440
               || mc.field_1755 instanceof class_443
               || mc.field_1755 instanceof class_446
               || mc.field_1755 instanceof class_458
               || mc.field_1755 instanceof class_426
               || mc.field_1755 instanceof class_404
               || mc.field_1755 instanceof class_5375
               || mc.field_1755 instanceof class_4189) {
               rpc.setState("Changing options");
            } else if (mc.field_1755 instanceof class_445) {
               rpc.setState("Reading credits");
            } else if (mc.field_1755 instanceof class_4905) {
               rpc.setState("Browsing Realms");
            } else {
               boolean setState = false;
               if (mc.field_1755 != null) {
                  String className = mc.field_1755.getClass().getName();

                  for (class_3545<String, String> pair : customStates) {
                     if (className.startsWith((String)pair.method_15442())) {
                        rpc.setState((String)pair.method_15441());
                        setState = true;
                        break;
                     }
                  }
               }

               if (!setState) {
                  rpc.setState("In main menu");
               }
            }

            update = true;
         }

         if (update) {
            DiscordIPC.setActivity(rpc);
         }

         this.forceUpdate = false;
         this.lastWasInMainMenu = !NetPayload.canUpdate();
      }
   }

   @EventHandler
   private void onOpenScreen(SetScreenEvent event) {
      if (!NetPayload.canUpdate()) {
         this.lastWasInMainMenu = false;
      }
   }

   static {
      registerCustomState("ru.vidtu.ias.screen.AccountScreen", "Switching account");
      registerCustomState("com.terraformersmc.modmenu.gui", "Browsing mods");
      registerCustomState("me.jellysquid.mods.sodium.client", "Changing options");
   }

   private static enum SmallImage {
      yige("yige", "yige"),
      LecitronArts("lecitronarts", "LecitronArts");

      private final String key;
      private final String text;

      private SmallImage(String key, String text) {
         this.key = key;
         this.text = text;
      }

      void apply() {
         DiscordPresence.rpc.setSmallImage(this.key, this.text);
      }

      DiscordPresence.SmallImage next() {
         return this == yige ? yige : LecitronArts;
      }
   }
}
