package cc.polyfrost.oneconfig.renderer.asset;

public class Image {
   public final String filePath;
   public final int flags;

   public Image(String filePath, int flags) {
      this.filePath = filePath;
      this.flags = flags;
   }

   public Image(String filePath) {
      this(filePath, 7);
   }
}
