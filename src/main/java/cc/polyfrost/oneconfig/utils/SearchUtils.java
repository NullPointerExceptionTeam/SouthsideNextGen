package cc.polyfrost.oneconfig.utils;

import cc.polyfrost.oneconfig.internal.config.Preferences;
import java.util.Locale;

public class SearchUtils {
   public static boolean isSimilar(String s1, String s2) {
      return isSimilar(s1, s2, Preferences.INSTANCE.searchDistance.getValue().intValue());
   }

   public static boolean isSimilar(String s1, String s2, int searchDistance) {
      s1 = s1.toLowerCase(Locale.ENGLISH);
      s2 = s2.toLowerCase(Locale.ENGLISH);
      if (s1.length() <= searchDistance) {
         return s1.contains(s2);
      } else {
         boolean similar = false;

         for (String a : org.apache.commons.lang3.StringUtils.split(s1)) {
            similar = a.contains(s2) || org.apache.commons.lang3.StringUtils.getLevenshteinDistance(a, s2) <= searchDistance;
            if (similar) {
               break;
            }
         }

         return similar || s1.contains(s2) || org.apache.commons.lang3.StringUtils.getLevenshteinDistance(s1, s2) <= searchDistance;
      }
   }
}
