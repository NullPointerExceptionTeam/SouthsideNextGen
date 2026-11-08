package dev.southside.render;

public class RenderBuffers {
   public static final int TEXT_BUFFER_CAPACITY = 2097152;
   public static RenderBuffer text;
   public static RenderBuffer base;

   public static void init() {
      base = new RenderBuffer(16384);
      text = new RenderBuffer(2097152);
   }
}
