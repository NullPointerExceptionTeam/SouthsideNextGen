package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Text;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import dev.southside.event.events.Render2DEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import java.awt.Color;

public class FakerVSBin extends Module {
   public Text scrollText = new Text("Scroll Text", "请输入滚动文本", false, false, "在屏幕上方滚动显示的文本内容", 2, "本影片由東方紅對戰平臺傾情贊助  本影片資源僅供學習交流使用,切勿用於其他用途");
   public Slider fontSize = new Slider("Font Size", 14.0F, 8.0F, 30.0F, 1.0F);
   public Slider scrollSpeed = new Slider("Scroll Speed", 2.0F, 0.5F, 10.0F, 0.5F);
   private float scrollOffset = 0.0F;

   public FakerVSBin() {
      super(Category.Render, "FakerVSBin", "滚动字幕赞助商广告。");
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.scrollOffset = 0.0F;
   }

   @EventHandler
   public void onRender2D(Render2DEvent event) {
      if (mc.method_22683() != null) {
         String text = this.scrollText.getValue();
         if (text != null && !text.isEmpty()) {
            float size = this.fontSize.getValue().floatValue();
            float speed = this.scrollSpeed.getValue().floatValue();
            int screenWidth = mc.method_22683().method_4486();
            int screenHeight = mc.method_22683().method_4502();
            float y = screenHeight / 5.0F;
            NanoVGHelper instance = NanoVGHelper.INSTANCE;
            instance.setupAndDraw(true, vg -> {
               float textWidth = instance.getTextWidth(vg, text, size, Fonts.WQY);
               float textHeight = instance.getTextHeight(vg, size, Fonts.WQY);
               float x = screenWidth - this.scrollOffset;
               instance.drawText(vg, text, x + 1.0F, y - textHeight / 2.0F + 1.0F, new Color(0, 0, 0, 150).getRGB(), size, Fonts.WQY);
               instance.drawText(vg, text, x, y - textHeight / 2.0F, new Color(255, 255, 255, 230).getRGB(), size, Fonts.WQY);
               this.scrollOffset += speed;
               if (this.scrollOffset > screenWidth + textWidth) {
                  this.scrollOffset = 0.0F;
               }
            });
         }
      }
   }
}
