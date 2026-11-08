package dev.southside.nanovg;

import java.util.ArrayList;
import java.util.List;

public class NanoFontLoader {
   public static List<NanoFontRenderer> renderers = new ArrayList<>();
   public static NanoFontRenderer harmony;

   public static void registerFonts() {
      harmony = new NanoFontRenderer("harmony", "harmony");
      renderers.add(harmony);
   }

   public static String[] getRenderers() {
      String[] values = new String[renderers.size()];

      for (int i = 0; i < renderers.size(); i++) {
         NanoFontRenderer fontRenderer = renderers.get(i);
         values[i] = fontRenderer.getName();
      }

      return values;
   }
}
