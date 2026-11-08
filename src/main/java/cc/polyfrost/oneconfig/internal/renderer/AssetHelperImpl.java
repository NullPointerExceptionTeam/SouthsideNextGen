package cc.polyfrost.oneconfig.internal.renderer;

import cc.polyfrost.oneconfig.renderer.asset.AssetHelper;
import cc.polyfrost.oneconfig.renderer.asset.Image;
import cc.polyfrost.oneconfig.renderer.asset.NVGAsset;
import cc.polyfrost.oneconfig.renderer.asset.SVG;
import cc.polyfrost.oneconfig.utils.IOUtils;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.nanovg.NSVGImage;
import org.lwjgl.nanovg.NanoSVG;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

public final class AssetHelperImpl implements AssetHelper {
   public static final int DEFAULT_FLAGS = 7;
   private final Map<String, NVGAsset> imageHashMap = new HashMap<>();
   private final Map<String, NVGAsset> svgHashMap = new HashMap<>();

   @Override
   public boolean loadImage(long vg, String fileName, int flags, Class<?> clazz) {
      if (!this.imageHashMap.containsKey(fileName)) {
         int[] width = new int[]{0};
         int[] height = new int[]{0};
         int[] channels = new int[]{0};
         ByteBuffer image = IOUtils.resourceToByteBufferNullable(fileName, clazz);
         if (image == null) {
            return false;
         } else {
            ByteBuffer buffer = STBImage.stbi_load_from_memory(image, width, height, channels, 4);
            if (buffer == null) {
               return false;
            } else {
               this.imageHashMap.put(fileName, new NVGAsset(NanoVG.nvgCreateImageRGBA(vg, width[0], height[0], flags, buffer), width[0], height[0]));
               return true;
            }
         }
      } else {
         return true;
      }
   }

   @Override
   public boolean loadImage(long vg, Image image) {
      return this.loadImage(vg, image.filePath, image.flags, image.getClass());
   }

   @Override
   public boolean loadImage(long vg, Image image, Class<?> clazz) {
      return this.loadImage(vg, image.filePath, image.flags, clazz);
   }

   @Override
   public boolean loadImage(long vg, String fileName, Class<?> clazz) {
      return this.loadImage(vg, fileName, 7, clazz);
   }

   @Override
   public boolean loadSVG(long vg, String fileName, float width, float height, int flags, Class<?> clazz) {
      String name = fileName + "-" + width + "-" + height;
      if (!this.svgHashMap.containsKey(name)) {
         try {
            InputStream inputStream = this.getClass().getResourceAsStream(fileName);
            if (inputStream == null) {
               return false;
            } else {
               StringBuilder resultStringBuilder = new StringBuilder();
               BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

               String line;
               try {
                  while ((line = br.readLine()) != null) {
                     resultStringBuilder.append(line);
                  }
               } catch (Throwable var20) {
                  try {
                     br.close();
                  } catch (Throwable var19) {
                     var20.addSuppressed(var19);
                  }

                  throw var20;
               }

               br.close();
               CharSequence s = resultStringBuilder.toString();
               NSVGImage svg = NanoSVG.nsvgParse(s, "px", 96.0F);
               if (svg == null) {
                  return false;
               } else {
                  long rasterizer = NanoSVG.nsvgCreateRasterizer();
                  int w = (int)svg.width();
                  int h = (int)svg.height();
                  float scale = Math.max(width / w, height / h);
                  w = (int)(w * scale);
                  h = (int)(h * scale);
                  ByteBuffer image = MemoryUtil.memAlloc(w * h * 4);
                  NanoSVG.nsvgRasterize(rasterizer, svg, 0.0F, 0.0F, scale, image, w, h, w * 4);
                  NanoSVG.nsvgDeleteRasterizer(rasterizer);
                  NanoSVG.nsvgDelete(svg);
                  this.svgHashMap.put(name, new NVGAsset(NanoVG.nvgCreateImageRGBA(vg, w, h, flags, image), w, h));
                  return true;
               }
            }
         } catch (Exception var21) {
            System.err.println("Failed to parse SVG file");
            var21.printStackTrace();
            return false;
         }
      } else {
         return true;
      }
   }

   @Override
   public boolean loadSVG(long vg, SVG svg, float width, float height) {
      return this.loadSVG(vg, svg.filePath, width, height, svg.flags, svg.getClass());
   }

   @Override
   public boolean loadSVG(long vg, SVG svg, float width, float height, Class<?> clazz) {
      return this.loadSVG(vg, svg.filePath, width, height, svg.flags, clazz);
   }

   @Override
   public boolean loadSVG(long vg, String fileName, float width, float height, Class<?> clazz) {
      return this.loadSVG(vg, fileName, width, height, 7, clazz);
   }

   @Override
   public int getImage(String fileName) {
      return this.imageHashMap.get(fileName).getImage();
   }

   @Override
   public NVGAsset getNVGImage(String fileName) {
      return this.imageHashMap.get(fileName);
   }

   @Override
   public void removeImage(long vg, String fileName) {
      NanoVG.nvgDeleteImage(vg, this.imageHashMap.get(fileName).getImage());
      this.imageHashMap.remove(fileName);
   }

   @Override
   public void clearImages(long vg) {
      Map<String, NVGAsset> temp = new HashMap<>(this.imageHashMap);

      for (String image : temp.keySet()) {
         NanoVG.nvgDeleteImage(vg, this.imageHashMap.get(image).getImage());
         this.imageHashMap.remove(image);
      }
   }

   @Override
   public int getSVG(String fileName, float width, float height) {
      String name = fileName + "-" + width + "-" + height;
      return this.svgHashMap.get(name).getImage();
   }

   @Override
   public NVGAsset getNVGSVG(String fileName) {
      return this.svgHashMap.get(fileName);
   }

   @Override
   public void removeSVG(long vg, String fileName, float width, float height) {
      String name = fileName + "-" + width + "-" + height;
      NanoVG.nvgDeleteImage(vg, this.imageHashMap.get(name).getImage());
      this.svgHashMap.remove(name);
   }

   @Override
   public void clearSVGs(long vg) {
      Map<String, NVGAsset> temp = new HashMap<>(this.svgHashMap);

      for (String image : temp.keySet()) {
         NanoVG.nvgDeleteImage(vg, this.svgHashMap.get(image).getImage());
         this.svgHashMap.remove(image);
      }
   }
}
