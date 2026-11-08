package cc.polyfrost.oneconfig.utils;

import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Font;
import java.util.ArrayList;

@Deprecated
public final class TextUtils {
   @Deprecated
   public static ArrayList<String> wrapText(long vg, String text, float maxWidth, float fontSize, Font font) {
      ArrayList<String> wrappedText = new ArrayList<>();
      text = text + " ";
      int prevIndex = 0;

      for (int i = text.indexOf(" "); i >= 0; i = text.indexOf(" ", i + 1)) {
         String textPart = text.substring(0, i);
         float textWidth = NanoVGHelper.INSTANCE.getTextWidth(vg, textPart, fontSize, font);
         if (!(textWidth < maxWidth)) {
            wrappedText.add(text.substring(0, prevIndex) + " ");
            wrappedText.addAll(wrapText(vg, text.substring(prevIndex + 1), maxWidth, fontSize, font));
            break;
         }

         prevIndex = i;
      }

      if (wrappedText.size() == 0) {
         wrappedText.add(text);
      }

      String temp = wrappedText.get(wrappedText.size() - 1);
      if (temp.length() != 0) {
         wrappedText.remove(wrappedText.size() - 1);
         wrappedText.add(temp.substring(0, temp.length() - 1));
      }

      return wrappedText;
   }
}
