package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import dev.southside.module.Category;
import dev.southside.module.Module;

public class ViewClip extends Module {
   public Slider scale = new Slider("Scale", 1.0F, 0.1F, 10.0F, 0.1F);

   public ViewClip() {
      super(Category.Render, "ViewClip", "无视方块阻挡的相机");
   }
}
