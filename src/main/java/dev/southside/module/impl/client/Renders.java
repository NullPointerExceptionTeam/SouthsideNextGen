package dev.southside.module.impl.client;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.module.Category;
import dev.southside.module.Module;

public class Renders extends Module {
   public static Renders INSTANCE;
   public final Switch msaa = new Switch("MSAA", "抗锯齿。", true);

   public Renders() {
      super(Category.Client, "Renders", "渲染。");
      INSTANCE = this;
   }

   @Override
   public void onEnable() {
      this.setEnable(false);
   }

   public static boolean useMSAA() {
      return INSTANCE.msaa.getValue();
   }
}
