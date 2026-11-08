package dev.southside.nanovg.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class IOUtil {
   public static byte[] toByteArray(InputStream input) throws IOException {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      byte[] data = new byte[1024];

      int nRead;
      while ((nRead = input.read(data, 0, data.length)) != -1) {
         buffer.write(data, 0, nRead);
      }

      buffer.flush();
      return buffer.toByteArray();
   }

   public static ByteBuffer toByteBuffer(InputStream inputStream) throws IOException {
      byte[] byteArray = toByteArray(inputStream);
      ByteBuffer byteBuffer = ByteBuffer.allocate(byteArray.length);
      byteBuffer.put(byteArray);
      byteBuffer.flip();
      return byteBuffer;
   }
}
