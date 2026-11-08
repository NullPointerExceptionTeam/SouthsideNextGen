package dev.southside.utils.render;

import dev.southside.MinecraftAccessor;
import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.events.GameLeftEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.module.impl.render.HUD;
import dev.southside.module.impl.render.NameProtect;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.heypixel.VIPList;
import dev.southside.utils.misc.ClientUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.class_124;
import net.minecraft.class_2583;
import net.minecraft.class_3545;
import net.minecraft.class_5224;
import net.minecraft.class_5251;
import net.minecraft.class_5481;

public class TextReplacementUtils implements MinecraftAccessor {
   private static final List<TextReplacementUtils.ReplacementMapping> replacements = new CopyOnWriteArrayList<>();
   private static final Map<String, String> stringCache = new ConcurrentHashMap<>();
   private static final int gray = class_124.field_1080.method_532();
   private static int lastStateHash = 0;
   public static TextReplacementUtils INSTANCE;

   public static void init() {
      INSTANCE = new TextReplacementUtils();
      Southside.INSTANCE.getEventBus().register(INSTANCE);
   }

   @EventHandler
   private void onTick(TickEvent e) {
      if (!ClientUtils.nullCheck()) {
         if (e.getState() == Stage.PRE) {
            this.updateReplacementsIfNeeded();
         }
      }
   }

   private void updateReplacementsIfNeeded() {
      HUD hudModule = Southside.get().getModuleManager().get(HUD.class);
      NameProtect nameProtect = Southside.get().getModuleManager().get(NameProtect.class);
      String hudReplaceName = hudModule.replaceName.getValue();
      boolean protectEnabled = nameProtect.isEnabled();
      String protectName = nameProtect.name.getValue();
      int protectColor = nameProtect.color.getValue().getRGB();
      int vipHash = VIPList.vips.hashCode();
      String playerName = mc.field_1724 != null ? mc.field_1724.method_7334().getName() : "";
      int currentHash = Objects.hash(hudReplaceName, protectEnabled, protectName, protectColor, vipHash, playerName);
      if (currentHash != lastStateHash || replacements.isEmpty()) {
         lastStateHash = currentHash;
         this.rebuildReplacements(hudReplaceName, nameProtect, playerName);
      }
   }

   private void rebuildReplacements(String hudReplaceName, NameProtect nameProtect, String playerName) {
      replacements.clear();
      stringCache.clear();
      replacements.add(new TextReplacementUtils.ReplacementMapping("布吉岛", hudReplaceName, List.of()));
      if (nameProtect.isEnabled() && !playerName.isEmpty()) {
         String replacementName = nameProtect.name.getValue();
         List<class_3545<int[], Integer>> colorMap = new ArrayList<>();
         colorMap.add(new class_3545(new int[]{0, replacementName.length() - 1}, nameProtect.color.getValue().getRGB()));
         replacements.add(new TextReplacementUtils.ReplacementMapping(playerName, replacementName, colorMap));
      }

      VIPList.vips
         .forEach(
            (original, replacement) -> replacements.add(
               new TextReplacementUtils.ReplacementMapping(
                  original,
                  replacement,
                  List.of(
                     new class_3545(new int[]{0, 0}, gray),
                     new class_3545(new int[]{1, replacement.length() - 2}, class_124.field_1060.method_532()),
                     new class_3545(new int[]{replacement.length() - 1, replacement.length() - 1}, gray)
                  )
               )
            )
         );
   }

   @EventHandler
   private void onLeft(GameLeftEvent e) {
      replacements.clear();
      stringCache.clear();
      lastStateHash = 0;
   }

   public static boolean shouldReplace() {
      return true;
   }

   public static String replace(String original) {
      return shouldReplace() && original != null && !original.isEmpty()
         ? stringCache.computeIfAbsent(original, TextReplacementUtils::computeReplacement)
         : original;
   }

   private static String computeReplacement(String original) {
      StringBuilder output = new StringBuilder();
      char[] chars = original.toCharArray();
      boolean wasParagraph = false;
      int index = 0;

      while (index < chars.length) {
         char c = chars[index++];
         if (c == 167) {
            wasParagraph = true;
            output.append(c);
         } else if (wasParagraph) {
            wasParagraph = false;
            output.append(c);
         } else {
            boolean found = false;
            Iterator var7 = replacements.iterator();

            while (true) {
               if (var7.hasNext()) {
                  TextReplacementUtils.ReplacementMapping replacement = (TextReplacementUtils.ReplacementMapping)var7.next();
                  int commonLength = getCommonLength(replacement, chars, index - 1);
                  if (commonLength == -1) {
                     continue;
                  }

                  index += commonLength - 1;
                  output.append(replacement.replacement);
                  found = true;
               }

               if (!found) {
                  output.append(c);
               }
               break;
            }
         }
      }

      return output.toString();
   }

   private static int getCommonLength(TextReplacementUtils.ReplacementMapping replacement, char[] chars, int index) {
      char[] replacementChars = replacement.originalName.toCharArray();
      int charsIndex = index;
      int replacementCharsIndex = 0;
      boolean wasParagraph = false;

      while (replacementCharsIndex < replacementChars.length) {
         if (charsIndex > chars.length - 1) {
            return -1;
         }

         char c = chars[charsIndex++];
         if (c == 167) {
            wasParagraph = true;
         } else if (wasParagraph) {
            wasParagraph = false;
         } else if (c != replacementChars[replacementCharsIndex++]) {
            return -1;
         }
      }

      return charsIndex - index;
   }

   public record ReplacementMapping(String originalName, String replacement, List<class_3545<int[], Integer>> colors) {
   }

   public static class ReplacementOrderedText implements class_5481 {
      private final List<TextReplacementUtils.ReplacementOrderedText.MappedCharacter> mappedCharacters = new ArrayList<>();

      public ReplacementOrderedText(class_5481 original) {
         List<TextReplacementUtils.ReplacementOrderedText.MappedCharacter> originalCharacters = new ArrayList<>();
         original.accept((indexx, style, codePoint) -> {
            originalCharacters.add(new TextReplacementUtils.ReplacementOrderedText.MappedCharacter(style, false, codePoint));
            return true;
         });
         int index = 0;

         while (index < originalCharacters.size()) {
            TextReplacementUtils.ReplacementOrderedText.MappedCharacter originalChar = originalCharacters.get(index);
            if (!originalChar.bypassesNameProtection) {
               Integer replacementLen = this.tryReplaceNames(index, originalCharacters, originalChar.style);
               if (replacementLen != null) {
                  index += replacementLen;
                  continue;
               }
            }

            this.mappedCharacters.add(originalChar);
            index++;
         }
      }

      private Integer tryReplaceNames(
         int startIndex, List<TextReplacementUtils.ReplacementOrderedText.MappedCharacter> originalCharacters, class_2583 currentStyle
      ) {
         for (TextReplacementUtils.ReplacementMapping replacement : TextReplacementUtils.replacements) {
            if (!replacement.originalName.isEmpty()) {
               boolean canReplace = true;

               for (int replacementIdx = 0; replacementIdx < replacement.originalName.length(); replacementIdx++) {
                  int origIndex = startIndex + replacementIdx;
                  if (originalCharacters.size() <= origIndex || originalCharacters.get(origIndex).codePoint != replacement.originalName.charAt(replacementIdx)) {
                     canReplace = false;
                     break;
                  }
               }

               if (canReplace) {
                  int charIndex = 0;

                  for (char c : replacement.replacement.toCharArray()) {
                     class_5251 color = this.getTextColor(charIndex, replacement.colors);
                     this.mappedCharacters
                        .add(
                           new TextReplacementUtils.ReplacementOrderedText.MappedCharacter(
                              color != null ? currentStyle.method_27703(color) : currentStyle, false, c
                           )
                        );
                     charIndex++;
                  }

                  return replacement.originalName.length();
               }
            }
         }

         return null;
      }

      private class_5251 getTextColor(int index, List<class_3545<int[], Integer>> map) {
         if (map == null) {
            return null;
         } else {
            for (class_3545<int[], Integer> pair : map) {
               int[] range = (int[])pair.method_15442();
               Integer color = (Integer)pair.method_15441();
               int min = range[0];
               int max = range[1];
               if (index >= min && index <= max) {
                  return class_5251.method_27717(color);
               }
            }

            return null;
         }
      }

      public boolean accept(class_5224 visitor) {
         int index = 0;

         for (TextReplacementUtils.ReplacementOrderedText.MappedCharacter character : this.mappedCharacters) {
            if (!visitor.accept(index, character.style, character.codePoint)) {
               return false;
            }

            index++;
         }

         return true;
      }

      public record MappedCharacter(class_2583 style, boolean bypassesNameProtection, int codePoint) {
      }
   }
}
