package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import cc.polyfrost.oneconfig.config.options.impl.Text;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import dev.southside.event.events.Render2DEvent;
import dev.southside.gui.hud.StickerHud;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;

public class HUD extends Module {
   public Switch hotbar = new Switch("Hotbar", true);
   public Switch hotbarAnimation = new Switch("Hotbar Animation", true);
   public Switch info = new Switch("Info", true);
   public Text username = new Text("User Name", "请输入文本", false, false, "", 1, "Player");
   public Text replaceName = new Text("Server Replace to", "布吉岛", false, false, "打几把", 1, "南极岛");
   public final cc.polyfrost.oneconfig.config.options.impl.HUD stickerHud = new cc.polyfrost.oneconfig.config.options.impl.HUD("Sticker", new StickerHud());
   public static double mouseX = 0.0;
   public static double mouseY = 0.0;

   public HUD() {
      super(Category.Render, "HUD", "请输入文本。", true);
   }

   @Override
   public void initPostRunnable() {
      super.initPostRunnable();
      this.addDependency(this.username.getLabel(), this.info.getLabel());
   }

   @EventHandler
   public void onRender2D(Render2DEvent event) {
      if (this.info.getValue()) {
         String user = this.username.getValue();
         NanoVGHelper instance = NanoVGHelper.INSTANCE;
         instance.setupAndDraw(
            true,
            vg -> instance.drawTextWithFormatting(
               vg,
               user,
               mc.getWindow().getScaledWidth() - instance.getTextWidth(vg, user, 10.0F, Fonts.WQY),
               mc.getWindow().getScaledHeight() - instance.getTextHeight(vg, 10.0F, Fonts.WQY) / 2.0F,
               -1,
               10.0F,
               Fonts.WQY
            )
         );
      }
   }
}
