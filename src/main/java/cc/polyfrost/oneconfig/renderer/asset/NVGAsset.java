package cc.polyfrost.oneconfig.renderer.asset;

public class NVGAsset {
   private final int image;
   private final int width;
   private final int height;

   public NVGAsset(int image, int width, int height) {
      this.image = image;
      this.width = width;
      this.height = height;
   }

   public int getImage() {
      return this.image;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }
}
