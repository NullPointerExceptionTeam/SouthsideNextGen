package dev.southside.nanovg.util;

import java.io.InputStream;

public class ResourceUtil {
   public static InputStream getResourceAsStream(String fileName) {
      try {
         String location = "/assets/southside/fonts/ui/" + fileName;
         return ResourceUtil.class.getResourceAsStream(location);
      } catch (Exception var2) {
         return null;
      }
   }
}
