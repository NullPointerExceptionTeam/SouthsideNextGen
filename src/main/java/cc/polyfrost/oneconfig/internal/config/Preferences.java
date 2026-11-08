package cc.polyfrost.oneconfig.internal.config;

import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cc.polyfrost.oneconfig.config.options.WrappedValue;
import cc.polyfrost.oneconfig.config.options.impl.Button;
import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.KeyBind;
import cc.polyfrost.oneconfig.config.options.impl.Number;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import cc.polyfrost.oneconfig.gui.OneConfigGui;
import cc.polyfrost.oneconfig.internal.gui.BlurHandler;
import cc.polyfrost.oneconfig.platform.Platform;
import cc.polyfrost.oneconfig.utils.TickDelay;

public class Preferences extends InternalConfig {
   public static Preferences INSTANCE;
   public KeyBind oneConfigKeyBind = new KeyBind("Click GUI Keybind", "Choose what key opens the OneConfig UI", 2, new OneKeyBind(344)) {
      @Override
      public String subcategory() {
         return "GUI Settings";
      }
   };
   public Switch enableBlur = new Switch("Enable Blur", true) {
      @Override
      public String subcategory() {
         return "GUI Settings";
      }
   };
   public Slider blurMaxRadius = new Slider(
      "Blur Max Radius", 1.0F, 50.0F, 0, "Maximum blur radius. Higher values look stronger but cost more FPS.", false, 0.1F, 5.0F
   ) {
      @Override
      public String subcategory() {
         return "GUI Settings";
      }
   };
   public Slider blurFadeStep = new Slider(
      "Blur Fade Step", 0.01F, 2.0F, 0, "Blur animation speed per frame. Higher values fade faster and are lighter on FPS.", false, 0.01F, 0.25F
   ) {
      @Override
      public String subcategory() {
         return "GUI Settings";
      }
   };
   public Slider blurSamplingScale = new Slider(
      "Blur Sample Rate",
      1.0F,
      4.0F,
      0,
      "Reduces blur sampling density while keeping blur range. 1.0 is full quality; higher values improve FPS.",
      false,
      0.1F,
      1.0F
   ) {
      @Override
      public String subcategory() {
         return "GUI Settings";
      }
   };
   public Switch enableCustomScale = new Switch("Use custom GUI scale", false) {
      @Override
      public String subcategory() {
         return "GUI Settings";
      }
   };
   public Switch enableCustomNotificationScale = new Switch("Use custom Notification scale", false) {
      @Override
      public String subcategory() {
         return "GUI Settings";
      }
   };
   public Slider customScale = new Slider("Custom GUI scale", 0.5F, 2.0F, 0, "", false, 1.0F) {
      @Override
      public String subcategory() {
         return "GUI Settings";
      }
   };
   public Slider customNotificationScale = new Slider("Custom Notification scale", 0.5F, 2.0F, 0, "", false, 1.0F) {
      @Override
      public String subcategory() {
         return "GUI Settings";
      }
   };
   public Dropdown openingBehavior = new Dropdown(
      "Opening Behavior", new String[]{"Mods", "Preferences", "Previous page", "Smart reset"}, "Choose which page will show when you open OneConfig", 2, 3
   ) {
      @Override
      public String category() {
         return "Behavior";
      }

      @Override
      public String subcategory() {
         return "GUI Settings";
      }
   };
   public Switch showPageAnimationOnOpen = new Switch(
      "Show opening page animation", "Whether or not to show the page switch animation when opening OneConfig", 2, false
   ) {
      @Override
      public String category() {
         return "Behavior";
      }

      @Override
      public String subcategory() {
         return "GUI Settings";
      }
   };
   public Slider timeUntilReset = new Slider(
      "Time before reset", 5.0F, 60.0F, 0, "How much time (in seconds) before opening back to the \"Mods\" page", false, 15
   ) {
      @Override
      public String category() {
         return "Behavior";
      }

      @Override
      public String subcategory() {
         return "GUI Settings";
      }
   };
   public Number searchDistance = new Number("Search Distance", 0.0F, 10.0F, 0, 2, "The maximum Levenshtein distance to search for similar config names", 2) {
      @Override
      public String category() {
         return "Behavior";
      }

      @Override
      public String subcategory() {
         return "Search";
      }
   };
   public Switch guiOpenAnimation = new Switch("Opening Animation", "Plays an animation when opening the GUI", true) {
      @Override
      public String category() {
         return "Animations";
      }

      @Override
      public String subcategory() {
         return "Global";
      }
   };
   public Switch guiClosingAnimation = new Switch("Closing Animation", "Plays an animation when closing the GUI", true) {
      @Override
      public String category() {
         return "Animations";
      }

      @Override
      public String subcategory() {
         return "Global";
      }
   };
   public Slider animationTime = new Slider("Opening Time", 0.05F, 2.0F, 0, "The duration of the opening and closing animations, in seconds", false, 0.6F) {
      @Override
      public String category() {
         return "Animations";
      }

      @Override
      public String subcategory() {
         return "Global";
      }
   };
   public Dropdown animationType = new Dropdown("Opening Type", new String[]{"Subtle", "Full"}, "The type of opening/closing animation to use", 1, 0) {
      @Override
      public String category() {
         return "Animations";
      }

      @Override
      public String subcategory() {
         return "Global";
      }
   };
   public Switch showPageAnimations = new Switch("Show Page Animations", "Enables or disables the page switch animation", true) {
      @Override
      public String category() {
         return "Animations";
      }

      @Override
      public String subcategory() {
         return "Pages";
      }
   };
   public Slider pageAnimationDuration = new Slider(
      "Page Animation Duration", 0.1F, 0.6F, 0, "The duration of the page switch animation, in seconds", false, 0.3F
   ) {
      @Override
      public String category() {
         return "Animations";
      }

      @Override
      public String subcategory() {
         return "Pages";
      }
   };
   public Switch toggleSwitchBounce = new Switch("Toggle Switch Bounce", "Enables or disables the bounce animation on toggle switches", true) {
      @Override
      public String category() {
         return "Animations";
      }

      @Override
      public String subcategory() {
         return "Toggles";
      }
   };
   public Slider trackerResponseDuration = new Slider(
      "Tracker Response Time", 0.0F, 100.0F, 0, "The time it takes for the slider tracker to move, in milliseconds", false, 60
   ) {
      @Override
      public String category() {
         return "Animations";
      }

      @Override
      public String subcategory() {
         return "Sliders";
      }
   };
   public Button button = new Button("Show First Launch Message", "Show", "Shows the first launch message again", 1, () -> {
      firstLaunch.setValue(true);
      oneconfigOpened = false;
      this.save();
   });
   public static WrappedValue<Boolean> firstLaunch = new WrappedValue<>("First Launch", true);
   public static transient boolean oneconfigOpened = false;

   public Preferences() {
      super("Preferences", "Preferences.json");
      INSTANCE = this;
      this.initialize();
      this.addListener(this.enableBlur.getLabel(), () -> BlurHandler.INSTANCE.reloadBlur(Platform.getGuiPlatform().getCurrentScreen()));
      this.addListener(this.blurMaxRadius.getLabel(), () -> BlurHandler.INSTANCE.reloadBlur(Platform.getGuiPlatform().getCurrentScreen()));
      this.addListener(this.blurFadeStep.getLabel(), () -> BlurHandler.INSTANCE.reloadBlur(Platform.getGuiPlatform().getCurrentScreen()));
      this.addListener(this.blurSamplingScale.getLabel(), () -> BlurHandler.INSTANCE.reloadBlur(Platform.getGuiPlatform().getCurrentScreen()));
      this.registerKeyBind(this.oneConfigKeyBind.getValue(), () -> new TickDelay(() -> Platform.getGuiPlatform().setCurrentScreen(OneConfigGui.create()), 1));
      this.addListener(this.animationTime.getLabel(), () -> {
         if (INSTANCE.guiOpenAnimation.getValue()) {
            OneConfigGui.INSTANCE.isClosed = true;
         }
      });
      this.addDependency(this.blurMaxRadius.getLabel(), this.enableBlur.getLabel());
      this.addDependency(this.blurFadeStep.getLabel(), this.enableBlur.getLabel());
      this.addDependency(this.blurSamplingScale.getLabel(), this.enableBlur.getLabel());
      this.addDependency(this.guiClosingAnimation.getLabel(), this.guiOpenAnimation.getLabel());
      this.addDependency(this.timeUntilReset.getLabel(), "Smart Opening Behavior", () -> this.openingBehavior.getValue() == 3);
      this.addDependency(this.pageAnimationDuration.getLabel(), this.showPageAnimations.getLabel());
      INSTANCE = this;
   }

   public static Preferences getInstance() {
      return INSTANCE == null ? (INSTANCE = new Preferences()) : INSTANCE;
   }
}
