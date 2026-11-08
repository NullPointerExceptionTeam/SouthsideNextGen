package dev.southside.utils.vector;

import java.nio.FloatBuffer;
import lombok.Generated;

public class Vector2f {
   public float x;
   public float y;

   public Vector2f() {
      this.x = 0.0F;
      this.y = 0.0F;
   }

   public Vector2f(float x, float y) {
      this.x = x;
      this.y = y;
   }

   public float lengthSquared() {
      return this.x * this.x + this.y * this.y;
   }

   public float length() {
      return (float)Math.sqrt(this.lengthSquared());
   }

   public Vector2f normalize() {
      float length = this.length();
      return this.divide(length);
   }

   public Vector2f add(Vector2f other) {
      float x = this.x + other.x;
      float y = this.y + other.y;
      return new Vector2f(x, y);
   }

   public Vector2f negate() {
      return this.scale(-1.0F);
   }

   public Vector2f subtract(Vector2f other) {
      return this.add(other.negate());
   }

   public Vector2f scale(float scalar) {
      float x = this.x * scalar;
      float y = this.y * scalar;
      return new Vector2f(x, y);
   }

   public Vector2f divide(float scalar) {
      return this.scale(1.0F / scalar);
   }

   public float dot(Vector2f other) {
      return this.x * other.x + this.y * other.y;
   }

   public Vector2f lerp(Vector2f other, float alpha) {
      return this.scale(1.0F - alpha).add(other.scale(alpha));
   }

   public void toBuffer(FloatBuffer buffer) {
      buffer.put(this.x).put(this.y);
      buffer.flip();
   }

   @Generated
   public float getX() {
      return this.x;
   }

   @Generated
   public float getY() {
      return this.y;
   }
}
