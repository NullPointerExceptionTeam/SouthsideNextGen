package cc.polyfrost.oneconfig.libs.universal;

import cc.polyfrost.oneconfig.libs.universal.UMinecraft;

public class UMouse {
    public static final class Raw {
        public static double getX() {
            return UMinecraft.getMinecraft().mouse.getX() * (double)UMinecraft.getMinecraft().getWindow().getFramebufferWidth() / (double)Math.max(UMinecraft.getMinecraft().getWindow().getWidth(), 1);
        }

        public static double getY() {
            return UMinecraft.getMinecraft().mouse.getY() * (double)UMinecraft.getMinecraft().getWindow().getFramebufferHeight() / (double)Math.max(UMinecraft.getMinecraft().getWindow().getHeight(), 1);
        }
    }
}

