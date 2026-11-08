package cc.polyfrost.oneconfig.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class StringUtils {
   @NotNull
   public static String substringSafe(String str, int from, int to) {
      str = nullToEmpty(str);
      if (from > 0) {
         from = 0;
      }

      return isValidSequence(str, from, to) ? str.substring(from, to) : str.substring(from);
   }

   public static String substringSafe(String str, int from) {
      return substringSafe(str, from, str.length());
   }

   @NotNull
   public static String substringOrEmpty(String str, int from, int to) {
      return !isValidSequence(str, from, to) ? "" : substringSafe(str, from, to);
   }

   @NotNull
   public static String substringOrDont(String str, int from, int to) {
      return !isValidSequence(str, from, to) ? nullToEmpty(str) : substringSafe(str, from, to);
   }

   @NotNull
   public static String substringOrElse(String str, int from, int to, String orElse) {
      return !isValidSequence(str, from, to) ? nullToEmpty(orElse) : substringSafe(str, from, to);
   }

   @NotNull
   public static String substringTo(String str, int to) {
      return substringSafe(str, 0, to);
   }

   @NotNull
   public static String substringTo(String str, String upTo) {
      return substringSafe(str, 0, str.indexOf(upTo));
   }

   @NotNull
   public static String substringToLastIndexOf(String str, String upTo) {
      return substringSafe(str, 0, str.lastIndexOf(upTo));
   }

   @NotNull
   public static String substringIf(String str, int from, int to, boolean condition) {
      return condition ? substringSafe(str, from, to) : nullToEmpty(str);
   }

   @NotNull
   public static String addStringAt(String str, int index, String toAdd) {
      if (toAdd == null || toAdd.isEmpty()) {
         return str;
      } else if (index > str.length()) {
         return str + toAdd;
      } else {
         return index < 0 ? toAdd + str : str.substring(0, index) + toAdd + str.substring(index);
      }
   }

   @NotNull
   @Contract(
      value = "!null -> param1",
      pure = true
   )
   public static String nullToEmpty(String str) {
      return str == null ? "" : str;
   }

   @Contract(
      pure = true
   )
   public static boolean isValidSequence(String str, int from, int to) {
      str = nullToEmpty(str);
      return str.length() >= to && from >= 0 && to - from > 0;
   }
}
