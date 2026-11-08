package cc.polyfrost.oneconfig.renderer.font;

import dev.southside.utils.misc.FileUtils;
import java.nio.ByteBuffer;
import lombok.Generated;

public class Font {
   private final String fileName;
   private final String name;
   private boolean loaded = false;
   private int id = -1;
   private ByteBuffer buffer = null;

   public Font(String name, String fileName) {
      this.name = name;
      this.fileName = fileName;

      try {
         this.buffer = FileUtils.readIntoNativeBuffer(Font.class.getResourceAsStream(fileName)).flip();
      } catch (Exception var4) {
         var4.printStackTrace();
      }
   }

   public String getName() {
      return this.name;
   }

   public String getFileName() {
      return this.fileName;
   }

   public boolean isLoaded() {
      return this.loaded;
   }

   public void setLoaded(boolean loaded) {
      this.loaded = loaded;
   }

   public ByteBuffer getBuffer() {
      return this.buffer;
   }

   public void setBuffer(ByteBuffer buffer) {
      this.buffer = buffer;
   }

   @Generated
   public void setId(int id) {
      this.id = id;
   }

   @Generated
   public int getId() {
      return this.id;
   }
}
