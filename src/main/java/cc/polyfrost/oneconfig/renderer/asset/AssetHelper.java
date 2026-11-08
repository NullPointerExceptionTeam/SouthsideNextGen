package cc.polyfrost.oneconfig.renderer.asset;

import cc.polyfrost.oneconfig.renderer.LwjglManager;

public interface AssetHelper {
   AssetHelper INSTANCE = LwjglManager.INSTANCE.getAssetHelper();
   int DEFAULT_FLAGS = 7;

   boolean loadImage(long var1, String var3, int var4, Class<?> var5);

   boolean loadImage(long var1, Image var3);

   boolean loadImage(long var1, Image var3, Class<?> var4);

   boolean loadImage(long var1, String var3, Class<?> var4);

   boolean loadSVG(long var1, String var3, float var4, float var5, int var6, Class<?> var7);

   boolean loadSVG(long var1, SVG var3, float var4, float var5);

   boolean loadSVG(long var1, SVG var3, float var4, float var5, Class<?> var6);

   boolean loadSVG(long var1, String var3, float var4, float var5, Class<?> var6);

   int getImage(String var1);

   NVGAsset getNVGImage(String var1);

   void removeImage(long var1, String var3);

   void clearImages(long var1);

   int getSVG(String var1, float var2, float var3);

   NVGAsset getNVGSVG(String var1);

   void removeSVG(long var1, String var3, float var4, float var5);

   void clearSVGs(long var1);
}
