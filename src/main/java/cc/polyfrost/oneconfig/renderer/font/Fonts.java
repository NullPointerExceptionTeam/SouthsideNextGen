package cc.polyfrost.oneconfig.renderer.font;

import java.util.ArrayList;
import java.util.List;

public class Fonts {
   public static final List<Font> FONTS = new ArrayList<>();
   public static final Font BOLD = register("inter-bold", "/assets/oneconfig/font/Bold.otf");
   public static final Font SEMIBOLD = register("inter-semibold", "/assets/oneconfig/font/SemiBold.ttf");
   public static final Font REGULAR = register("inter-regular", "/assets/oneconfig/font/Regular.ttf");
   public static final Font MEDIUM = REGULAR;
   public static final Font MINECRAFT_REGULAR = register("mc-regular", "/assets/oneconfig/font/Minecraft-Regular.otf");
   public static final Font MINECRAFT_BOLD = register("mc-bold", "/assets/oneconfig/font/Minecraft-Bold.otf");
   public static final Font WQY = register("wqy", "/assets/southside/fonts/ui/wqy_microhei.ttf");
   public static final Font JELLOLIGHT = register("jello-light", "/assets/southside/fonts/ui/jellolight.ttf");
   public static final Font JELLOMEDIUM = register("jello-medium", "/assets/southside/fonts/ui/jellomedium.ttf");
   public static final Font JELLOREGULAR = register("jello-regular", "/assets/southside/fonts/ui/jelloregular.ttf");

   public static Font register(String name, String path) {
      Font font = new Font(name, path);
      FONTS.add(font);
      return font;
   }
}
