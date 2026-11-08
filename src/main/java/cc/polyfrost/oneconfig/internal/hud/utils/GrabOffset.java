package cc.polyfrost.oneconfig.internal.hud.utils;

public class GrabOffset {
   private float offsetX;
   private float offsetY;

   public GrabOffset(float offsetX, float offsetY) {
      this.setOffset(offsetX, offsetY);
   }

   public GrabOffset() {
      this(0.0F, 0.0F);
   }

   public void setOffset(float offsetX, float offsetY) {
      this.offsetX = offsetX;
      this.offsetY = offsetY;
   }

   public float getX() {
      return this.offsetX;
   }

   public float getY() {
      return this.offsetY;
   }
}
