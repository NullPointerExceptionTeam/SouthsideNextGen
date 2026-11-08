package cc.polyfrost.oneconfig.platform;

public interface MousePlatform {
   double getMouseX();

   double getMouseY();

   double getDWheel();

   double getMouseDX();

   double getMouseDY();

   int getButtonState(int var1);

   boolean isButtonDown(int var1);
}
