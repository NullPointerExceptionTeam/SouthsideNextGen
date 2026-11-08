package dev.southside.module.impl.client;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.HUD;
import cc.polyfrost.oneconfig.config.options.impl.Text;
import dev.southside.gui.hud.WatermarkHud;
import dev.southside.module.Category;
import dev.southside.module.Module;
import lombok.Generated;

public class Watermark extends Module {
   private final Dropdown mode = new Dropdown("Style", "Watermark Style", "Southside", "Southside", "SigmaJello", "Old", "Retro");
   public final Text clientName = new Text("Client Name", "请输入客户端名称", false, false, "Watermark显示的客户端名称", 1, "Southside");
   public final HUD hud = new HUD("Watermark", new WatermarkHud());

   public Watermark() {
      super(Category.Client, "Watermark", "漏狗", true);
   }

   public String getClientName() {
      String name = this.clientName.getValue();
      return name != null && !name.isBlank() ? name.trim() : "Southside";
   }

   @Generated
   public Dropdown getMode() {
      return this.mode;
   }
}
