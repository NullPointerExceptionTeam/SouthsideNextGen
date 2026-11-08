package dev.southside.module.impl.render;

import dev.southside.module.Category;
import dev.southside.module.Module;

public class FullBright extends Module {
   public FullBright() {
      super(Category.Render, "FullBright", "我的眼睛能夜视了。", true);
   }

   public boolean getGamma() {
      return this.isEnabled();
   }
}
