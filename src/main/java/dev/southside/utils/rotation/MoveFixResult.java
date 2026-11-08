package dev.southside.utils.rotation;

public record MoveFixResult(double adjustedAngle, int usedDelta, double error) {
   @Override
   public String toString() {
      return String.format("adjusted=%.2f°, delta=%d°, error=%.2f°", this.adjustedAngle, this.usedDelta, this.error);
   }
}
