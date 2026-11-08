package dev.southside.module.impl.client;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Text;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.gpt.GPTAPIUtils;

public class ClientSettings extends Module {
   public final Dropdown mode = new Dropdown("MainMenu Style", "Minecraft", "Southside", "Minecraft");
   public static final Dropdown style = new Dropdown("Sound Style", "Default", "None", "Default", "Piano", "Sigma");
   public static final Slider volumeValue = new Slider("Volume", 1, 0.1, 1, 0.01);
   public static final Dropdown prompt = new Dropdown("GPT Prompt", "Polar Fox", "Polar Fox", "Sister", "Jibril", "Custom");
   public static final Text customPrompt = new Text(
      "Custom Prompt", "改完重新切换该模式", false, false, "prompt", 100, "You are a(n) 16-years-old polar fox girl owned by Master."
   );
   public static final Dropdown progressBarStyle = new Dropdown("ProgressBar Style", "Simple", "Simple", "Classic");
   public static final Slider progressBarOffset = new Slider("ProgressBar Y Offset", 20, 0, 200, 1);
   public static final Slider progressBarScale = new Slider("ProgressBar Scale", 1, 0.5, 2, 0.05);

   public ClientSettings() {
      super(Category.Client, "ClientSettings", "Setting for Client");
   }

   @Override
   public void onEnable() {
      this.setEnable(false);
   }

   @Override
   public void initPostRunnable() {
      super.initPostRunnable();
      this.hideIf(customPrompt.getLabel(), () -> !prompt.isMode("Custom"));
      this.hideIf(volumeValue.getLabel(), () -> prompt.isMode("None"));
      this.addListener(prompt.getLabel(), () -> {
         if (GPTAPIUtils.isInitialized()) {
            GPTAPIUtils.reloadPrompt();
         }
      });
      this.addListener(customPrompt.getLabel(), () -> {
         if (prompt.isMode("Custom") && GPTAPIUtils.isInitialized()) {
            GPTAPIUtils.reloadPrompt();
         }
      });
   }
}
