package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.options.impl.Color;
import cc.polyfrost.oneconfig.config.options.impl.Text;
import dev.southside.module.Category;
import dev.southside.module.Module;

public class NameProtect extends Module {
   public final Text name = new Text("Name", "请输入文本", false, false, "", 1, "Hidden");
   public final Color color = new Color("Color", new OneColor(-10878977));

   public NameProtect() {
      super(Category.Render, "NameProtect", "Protects your name from being seen by yourself");
   }
}
