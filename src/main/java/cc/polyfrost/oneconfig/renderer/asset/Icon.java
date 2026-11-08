package cc.polyfrost.oneconfig.renderer.asset;

import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import com.google.gson.annotations.SerializedName;

public class Icon {
   private final Object icon;
   public final Icon.Type type;

   public Icon(String filePath) {
      this.icon = filePath.endsWith(".svg") ? new SVG(filePath) : new Image(filePath);
      this.type = filePath.endsWith(".svg") ? Icon.Type.SVG : Icon.Type.IMAGE;
   }

   public Icon(SVG svg) {
      this.icon = svg;
      this.type = Icon.Type.SVG;
   }

   public Icon(Image image) {
      this.icon = image;
      this.type = Icon.Type.IMAGE;
   }

   public SVG getSVG() {
      return (SVG)this.icon;
   }

   public Image getImage() {
      return (Image)this.icon;
   }

   public void draw(long vg, float x, float y, float width, float height, int color, float scale) {
      if (this.type == Icon.Type.SVG) {
         NanoVGHelper.INSTANCE.drawSvg(vg, this.getSVG(), x, y, width, height, color, scale);
      } else if (this.type == Icon.Type.IMAGE) {
         NanoVGHelper.INSTANCE.drawImage(vg, this.getImage(), x, y, width, height, color);
      }
   }

   public void draw(long vg, float x, float y, float width, float height, int color) {
      if (this.type == Icon.Type.SVG) {
         NanoVGHelper.INSTANCE.drawSvg(vg, this.getSVG(), x, y, width, height, color);
      } else if (this.type == Icon.Type.IMAGE) {
         NanoVGHelper.INSTANCE.drawImage(vg, this.getImage(), x, y, width, height, color);
      }
   }

   public void draw(long vg, float x, float y, float width, float height) {
      if (this.type == Icon.Type.SVG) {
         NanoVGHelper.INSTANCE.drawSvg(vg, this.getSVG(), x, y, width, height);
      } else if (this.type == Icon.Type.IMAGE) {
         NanoVGHelper.INSTANCE.drawImage(vg, this.getImage(), x, y, width, height);
      }
   }

   public static enum Type {
      @SerializedName("0")
      SVG,
      @SerializedName("1")
      IMAGE;
   }
}
