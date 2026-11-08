package cc.polyfrost.oneconfig.libs.universal;

import cc.polyfrost.oneconfig.libs.universal.UMinecraft;

public class UMouse {
    public static final class Raw {
        public static double getX() {
            return UMinecraft.getMinecraft().field_1729.method_1603() * (double)UMinecraft.getMinecraft().method_22683().method_4489() / (double)Math.max(UMinecraft.getMinecraft().method_22683().method_4480(), 1);
        }

        public static double getY() {
            return UMinecraft.getMinecraft().field_1729.method_1604() * (double)UMinecraft.getMinecraft().method_22683().method_4506() / (double)Math.max(UMinecraft.getMinecraft().method_22683().method_4507(), 1);
        }
    }
}

