package dev.southside.manager;

import dev.southside.Southside;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class StickerManager {
   private static final MinecraftClient mc = MinecraftClient.getInstance();
   private static final int BASE_SIZE = 64;
   private static final String[] DEFAULT_STICKERS = new String[]{"sh1t.gif", "button.gif", "hot.gif", "pig.gif", "sweat.gif"};
   private final File stickersDir;
   private final Map<String, StickerManager.StickerData> loadedStickers = new LinkedHashMap<>();
   private String activeStickerName = null;

   public StickerManager() {
      this.stickersDir = new File(Southside.FOLDER, "stickers");
      if (!this.stickersDir.exists()) {
         this.stickersDir.mkdirs();
      }

      this.extractDefaults();
      this.reload();
   }

   private void extractDefaults() {
      for (String name : DEFAULT_STICKERS) {
         File target = new File(this.stickersDir, name);
         if (!target.exists()) {
            try (InputStream is = this.getClass().getResourceAsStream("/assets/southside/stickers/" + name)) {
               if (is != null) {
                  Files.copy(is, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
               }
            } catch (IOException var11) {
               Southside.LOGGER.warn("Failed to extract default sticker: " + name, var11);
            }
         }
      }
   }

   public void reload() {
      for (StickerManager.StickerData data : this.loadedStickers.values()) {
         this.destroySticker(data);
      }

      this.loadedStickers.clear();
      this.activeStickerName = null;
      File[] files = this.stickersDir.listFiles((dir, name) -> {
         String lowerx = name.toLowerCase();
         return lowerx.endsWith(".png") || lowerx.endsWith(".gif");
      });
      if (files != null) {
         for (File file : files) {
            try {
               String name = file.getName();
               String lower = name.toLowerCase();
               if (lower.endsWith(".gif")) {
                  this.loadGif(name, file);
               } else {
                  this.loadPng(name, file);
               }
            } catch (Exception var8) {
               Southside.LOGGER.warn("Failed to load sticker: " + file.getName(), var8);
            }
         }
      }
   }

   private void loadPng(String name, File file) throws IOException {
      Identifier id = Identifier.of("southside", "sticker/" + this.sanitize(name));

      try (InputStream is = new FileInputStream(file)) {
         NativeImage image = NativeImage.read(is);
         NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
         mc.execute(() -> mc.getTextureManager().registerTexture(id, texture));
         StickerManager.StickerData data = new StickerManager.StickerData();
         data.frames = new StickerManager.FrameData[]{new StickerManager.FrameData(id, 0)};
         data.totalDuration = 0L;
         data.isAnimated = false;
         data.width = image.getWidth();
         data.height = image.getHeight();
         this.loadedStickers.put(name, data);
      }
   }

   private void loadGif(String name, File file) throws IOException {
      ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();

      try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
         reader.setInput(iis);
         int count = reader.getNumImages(true);
         if (count == 0) {
            return;
         }

         List<StickerManager.FrameData> frames = new ArrayList<>();
         int gifWidth = 0;
         int gifHeight = 0;
         long totalDuration = 0L;
         BufferedImage canvas = null;
         BufferedImage prevCanvas = null;

         for (int i = 0; i < count; i++) {
            BufferedImage raw = reader.read(i);
            IIOMetadata meta = reader.getImageMetadata(i);
            int frameX = 0;
            int frameY = 0;

            try {
               IIOMetadataNode tree = (IIOMetadataNode)meta.getAsTree("javax_imageio_gif_image_1.0");
               NodeList imgDescNodes = tree.getElementsByTagName("ImageDescriptor");
               if (imgDescNodes.getLength() > 0) {
                  Node imgDesc = imgDescNodes.item(0);
                  frameX = Integer.parseInt(imgDesc.getAttributes().getNamedItem("imageLeftPosition").getNodeValue());
                  frameY = Integer.parseInt(imgDesc.getAttributes().getNamedItem("imageTopPosition").getNodeValue());
               }
            } catch (Exception var33) {
            }

            if (canvas == null) {
               gifWidth = Math.max(raw.getWidth() + frameX, raw.getWidth());
               gifHeight = Math.max(raw.getHeight() + frameY, raw.getHeight());

               try {
                  IIOMetadataNode tree = (IIOMetadataNode)reader.getStreamMetadata().getAsTree("javax_imageio_gif_stream_1.0");
                  NodeList screenNodes = tree.getElementsByTagName("LogicalScreenDescriptor");
                  if (screenNodes.getLength() > 0) {
                     Node screen = screenNodes.item(0);
                     gifWidth = Integer.parseInt(screen.getAttributes().getNamedItem("logicalScreenWidth").getNodeValue());
                     gifHeight = Integer.parseInt(screen.getAttributes().getNamedItem("logicalScreenHeight").getNodeValue());
                  }
               } catch (Exception var32) {
               }

               canvas = new BufferedImage(gifWidth, gifHeight, 2);
            }

            int disposal = this.getDisposalMethod(meta);
            if (disposal == 3) {
               prevCanvas = this.copyImage(canvas);
            }

            Graphics2D g = canvas.createGraphics();
            if (disposal == 2 && i > 0) {
            }

            g.drawImage(raw, frameX, frameY, null);
            g.dispose();
            int delay = this.getFrameDelay(meta);
            if (delay <= 0) {
               delay = 100;
            }

            Identifier frameId = Identifier.of("southside", "sticker/" + this.sanitize(name) + "_f" + i);
            NativeImage nativeImage = this.bufferedToNative(canvas);
            NativeImageBackedTexture texture = new NativeImageBackedTexture(nativeImage);
            mc.execute(() -> mc.getTextureManager().registerTexture(frameId, texture));
            frames.add(new StickerManager.FrameData(frameId, delay));
            totalDuration += delay;
            if (disposal == 2) {
               Graphics2D g2 = canvas.createGraphics();
               g2.setComposite(AlphaComposite.Clear);
               g2.fillRect(frameX, frameY, raw.getWidth(), raw.getHeight());
               g2.setComposite(AlphaComposite.SrcOver);
               g2.dispose();
            } else if (disposal == 3 && prevCanvas != null) {
               canvas = prevCanvas;
            }
         }

         StickerManager.StickerData data = new StickerManager.StickerData();
         data.frames = frames.toArray(new StickerManager.FrameData[0]);
         data.totalDuration = totalDuration;
         data.isAnimated = true;
         data.width = gifWidth;
         data.height = gifHeight;
         this.loadedStickers.put(name, data);
      } finally {
         reader.dispose();
      }
   }

   private BufferedImage copyImage(BufferedImage src) {
      BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), 2);
      Graphics2D g = copy.createGraphics();
      g.drawImage(src, 0, 0, null);
      g.dispose();
      return copy;
   }

   private NativeImage bufferedToNative(BufferedImage src) {
      int w = src.getWidth();
      int h = src.getHeight();
      NativeImage image = new NativeImage(w, h, false);

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            int argb = src.getRGB(x, y);
            image.setColorArgb(x, y, argb);
         }
      }

      return image;
   }

   private int getFrameDelay(IIOMetadata metadata) {
      try {
         Node tree = metadata.getAsTree("javax_imageio_gif_image_1.0");
         NodeList children = tree.getChildNodes();

         for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if ("GraphicControlExtension".equals(node.getNodeName())) {
               String delayStr = node.getAttributes().getNamedItem("delayTime").getNodeValue();
               return Integer.parseInt(delayStr) * 10;
            }
         }
      } catch (Exception var7) {
      }

      return 100;
   }

   private int getDisposalMethod(IIOMetadata metadata) {
      try {
         Node tree = metadata.getAsTree("javax_imageio_gif_image_1.0");
         NodeList children = tree.getChildNodes();

         for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if ("GraphicControlExtension".equals(node.getNodeName())) {
               String val = node.getAttributes().getNamedItem("disposalMethod").getNodeValue();

               return switch (val) {
                  case "restoreToBackgroundColor" -> 2;
                  case "restoreToPrevious" -> 3;
                  default -> 0;
               };
            }
         }
      } catch (Exception var9) {
      }

      return 0;
   }

   private void destroySticker(StickerManager.StickerData data) {
      for (StickerManager.FrameData frame : data.frames) {
         mc.execute(() -> mc.getTextureManager().destroyTexture(frame.identifier));
      }
   }

   private String sanitize(String name) {
      return name.toLowerCase().replaceAll("[^a-z0-9._-]", "_");
   }

   public void use(String name) {
      if (this.loadedStickers.containsKey(name)) {
         this.activeStickerName = name;
      }
   }

   public void off() {
      this.activeStickerName = null;
   }

   public List<String> getAvailableStickers() {
      return new ArrayList<>(this.loadedStickers.keySet());
   }

   public String getActiveStickerName() {
      return this.activeStickerName;
   }

   public StickerManager.StickerRenderInfo getCurrentFrame() {
      if (this.activeStickerName == null) {
         return null;
      } else {
         StickerManager.StickerData data = this.loadedStickers.get(this.activeStickerName);
         if (data != null && data.frames.length != 0) {
            Identifier textureId;
            if (data.isAnimated && data.frames.length > 1) {
               long time = System.currentTimeMillis() % data.totalDuration;
               long acc = 0L;
               int frameIndex = 0;

               for (int i = 0; i < data.frames.length; i++) {
                  acc += data.frames[i].delayMs;
                  if (time < acc) {
                     frameIndex = i;
                     break;
                  }
               }

               textureId = data.frames[frameIndex].identifier;
            } else {
               textureId = data.frames[0].identifier;
            }

            int drawH;
            int drawW;
            if (data.width >= data.height) {
               drawW = 64;
               drawH = (int)((float)data.height / data.width * 64.0F);
            } else {
               drawH = 64;
               drawW = (int)((float)data.width / data.height * 64.0F);
            }

            return new StickerManager.StickerRenderInfo(textureId, drawW, drawH);
         } else {
            return null;
         }
      }
   }

   private record FrameData(Identifier identifier, int delayMs) {
   }

   private static class StickerData {
      StickerManager.FrameData[] frames;
      long totalDuration;
      boolean isAnimated;
      int width;
      int height;
   }

   public record StickerRenderInfo(Identifier textureId, int drawWidth, int drawHeight) {
   }
}
