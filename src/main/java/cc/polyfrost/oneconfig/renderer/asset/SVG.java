package cc.polyfrost.oneconfig.renderer.asset;

public class SVG {
   public final String filePath;
   public final int flags;

   public SVG(String filePath, int flags) {
      this.filePath = filePath;
      this.flags = flags;
   }

   public SVG(String filePath) {
      this(filePath, 7);
   }
}
