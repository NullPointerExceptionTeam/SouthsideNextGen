package dev.southside.utils.misc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import org.lwjgl.system.MemoryUtil;

public class FileUtils {
   public static ByteBuffer readIntoNativeBuffer(ReadableByteChannel channel) throws IOException {
      ByteBuffer p = null;

      try {
         if (channel instanceof SeekableByteChannel ch) {
            long rem = ch.size() - ch.position() + 1L;
            p = MemoryUtil.memAlloc((int)Math.min(rem, 2147483647L));

            while (ch.read(p) > 0) {
            }
         } else {
            p = MemoryUtil.memAlloc(4096);

            while (channel.read(p) != -1) {
               if (!p.hasRemaining()) {
                  long cap = p.capacity();
                  if (cap == 2147483647L) {
                     break;
                  }

                  p = MemoryUtil.memRealloc(p, (int)Math.min(cap + (cap >> 1), 2147483647L));
               }
            }
         }

         return p;
      } catch (Throwable var5) {
         MemoryUtil.memFree(p);
         throw var5;
      }
   }

   public static ByteBuffer readIntoNativeBuffer(InputStream stream) throws IOException {
      return readIntoNativeBuffer(Channels.newChannel(stream));
   }
}
