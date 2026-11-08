package cc.polyfrost.oneconfig.hud;

import cc.polyfrost.oneconfig.libs.universal.UResolution;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

public class Position {
   public Position.AnchorPosition anchor;
   public float x;
   public float y;
   private float width;
   private float height;
   @Nullable
   private Hud hud;

   public Position(Hud hud, float x, float y, float width, float height, float screenWidth, float screenHeight) {
      this.hud = hud;
      this.setSize(width, height);
      this.setPosition(x, y, screenWidth, screenHeight);
   }

   public Position(Hud hud, float x, float y, float width, float height) {
      this(hud, x, y, width, height, 1920.0F, 1080.0F);
   }

   public void setPosition(float x, float y, float screenWidth, float screenHeight) {
      float rightX = x + this.width;
      float bottomY = y + this.height;
      if (x <= screenWidth / 3.0F && y <= screenHeight / 3.0F) {
         this.anchor = Position.AnchorPosition.TOP_LEFT;
      } else if (rightX >= screenWidth / 3.0F * 2.0F && y <= screenHeight / 3.0F) {
         this.anchor = Position.AnchorPosition.TOP_RIGHT;
      } else if (x <= screenWidth / 3.0F && bottomY >= screenHeight / 3.0F * 2.0F) {
         this.anchor = Position.AnchorPosition.BOTTOM_LEFT;
      } else if (rightX >= screenWidth / 3.0F * 2.0F && bottomY >= screenHeight / 3.0F * 2.0F) {
         this.anchor = Position.AnchorPosition.BOTTOM_RIGHT;
      } else if (y <= screenHeight / 3.0F) {
         this.anchor = Position.AnchorPosition.TOP_CENTER;
      } else if (x <= screenWidth / 3.0F) {
         this.anchor = Position.AnchorPosition.MIDDLE_LEFT;
      } else if (rightX >= screenWidth / 3.0F * 2.0F) {
         this.anchor = Position.AnchorPosition.MIDDLE_RIGHT;
      } else if (bottomY >= screenHeight / 3.0F * 2.0F) {
         this.anchor = Position.AnchorPosition.BOTTOM_CENTER;
      } else {
         this.anchor = Position.AnchorPosition.MIDDLE_CENTER;
      }

      this.x = x - this.getAnchorX(screenWidth) + this.getAnchorX(this.width);
      this.y = y - this.getAnchorY(screenHeight) + this.getAnchorY(this.height);
   }

   public void setPosition(float x, float y) {
      this.setPosition(x, y, UResolution.getScaledWidth(), UResolution.getScaledHeight());
   }

   public void setSize(float width, float height) {
      this.width = width;
      this.height = height;
   }

   public void setHud(Hud hud) {
      this.hud = hud;
   }

   public void updateSizePosition(float width, float height) {
      float x = this.getX();
      float y = this.getY();
      this.setSize(width, height);
      this.setPosition(x, y);
   }

   public float getX(float screenWidth) {
      return this.x + this.getAnchorX(screenWidth) - this.getAnchorX(this.width);
   }

   public float getX() {
      return this.getX(UResolution.getScaledWidth());
   }

   public void setX(float x) {
      this.x = x - this.getAnchorX(UResolution.getScaledWidth()) + this.getAnchorX(this.width);
   }

   public float getY(float screenHeight) {
      return this.y + this.getAnchorY(screenHeight) - this.getAnchorY(this.height);
   }

   public float getY() {
      return this.getY(UResolution.getScaledHeight());
   }

   public void setY(float y) {
      this.y = y - this.getAnchorY(UResolution.getScaledHeight()) + this.getAnchorY(this.height);
   }

   public float getRightX(float screenWidth) {
      return this.getX(screenWidth) + this.width;
   }

   public float getRightX() {
      return this.getRightX(UResolution.getScaledWidth());
   }

   public float getBottomY(float screenHeight) {
      return this.getY(screenHeight) + this.height;
   }

   public float getBottomY() {
      return this.getBottomY(UResolution.getScaledHeight());
   }

   public float getCenterX(float screenWidth) {
      return this.getX(screenWidth) + this.width / 2.0F;
   }

   public float getCenterX() {
      return this.getCenterX(UResolution.getScaledWidth());
   }

   public float getCenterY(float screenHeight) {
      return this.getY(screenHeight) + this.height / 2.0F;
   }

   public float getCenterY() {
      return this.getCenterY(UResolution.getScaledHeight());
   }

   public float getWidth() {
      return this.width;
   }

   public float getHeight() {
      return this.height;
   }

   private float getAnchorX(float value) {
      return value * this.anchor.x;
   }

   private float getAnchorY(float value) {
      return value * this.anchor.y;
   }

   public static Position.AnchorPosition getAnchor(int ordinal) {
      return switch (ordinal) {
         case 1 -> Position.AnchorPosition.TOP_CENTER;
         case 2 -> Position.AnchorPosition.TOP_RIGHT;
         case 3 -> Position.AnchorPosition.MIDDLE_LEFT;
         case 4 -> Position.AnchorPosition.MIDDLE_CENTER;
         case 5 -> Position.AnchorPosition.MIDDLE_RIGHT;
         case 6 -> Position.AnchorPosition.BOTTOM_LEFT;
         case 7 -> Position.AnchorPosition.BOTTOM_CENTER;
         case 8 -> Position.AnchorPosition.BOTTOM_RIGHT;
         default -> Position.AnchorPosition.TOP_LEFT;
      };
   }

   public Position.AnchorPosition getAnchor() {
      return this.anchor;
   }

   public Hud getHud() {
      return this.hud;
   }

   public static enum AnchorPosition {
      @SerializedName("0")
      TOP_LEFT(0.0F, 0.0F),
      @SerializedName("1")
      TOP_CENTER(0.5F, 0.0F),
      @SerializedName("2")
      TOP_RIGHT(1.0F, 0.0F),
      @SerializedName("3")
      MIDDLE_LEFT(0.0F, 0.5F),
      @SerializedName("4")
      MIDDLE_CENTER(0.5F, 0.5F),
      @SerializedName("5")
      MIDDLE_RIGHT(1.0F, 0.5F),
      @SerializedName("6")
      BOTTOM_LEFT(0.0F, 1.0F),
      @SerializedName("7")
      BOTTOM_CENTER(0.5F, 1.0F),
      @SerializedName("8")
      BOTTOM_RIGHT(1.0F, 1.0F);

      public final float x;
      public final float y;

      private AnchorPosition(float x, float y) {
         this.x = x;
         this.y = y;
      }
   }
}
