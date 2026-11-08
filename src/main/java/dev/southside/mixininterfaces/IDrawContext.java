package dev.southside.mixininterfaces;

import net.minecraft.class_2960;

public interface IDrawContext {
   default void drawTexture(
      class_2960 texture, float x, float y, float width, float height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight
   ) {
   }

   default void drawTexture(
      class_2960 texture,
      float x1,
      float x2,
      float y1,
      float y2,
      float z,
      int regionWidth,
      int regionHeight,
      float u,
      float v,
      int textureWidth,
      int textureHeight
   ) {
   }

   default void drawTexturedQuad(class_2960 texture, float x1, float x2, float y1, float y2, float z, float u1, float u2, float v1, float v2) {
   }

   default void fillGradientVertical(int startX, int startY, int endX, int endY, int z, int colorStart, int colorEnd) {
   }
}
