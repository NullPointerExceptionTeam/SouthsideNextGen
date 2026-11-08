package cc.polyfrost.oneconfig.libs.universal;

import net.minecraft.class_2561;
import net.minecraft.class_310;

public class UChat {
   public static void chat(String s) {
      class_310.method_1551().field_1705.method_1743().method_1812(class_2561.method_43470(s));
   }

   public static void say(String s) {
      UMinecraft.getMinecraft().field_1724.field_3944.method_45729(s);
   }
}
