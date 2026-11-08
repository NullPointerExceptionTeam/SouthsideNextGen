package cc.polyfrost.oneconfig.utils;

import com.google.gson.JsonElement;
import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class NetworkUtils {
   public static String getString(String url, String userAgent, int timeout, boolean useCaches) {
      try {
         String var5;
         try (InputStreamReader input = new InputStreamReader(setupConnection(url, userAgent, timeout, useCaches), StandardCharsets.UTF_8)) {
            var5 = org.apache.commons.io.IOUtils.toString(input);
         }

         return var5;
      } catch (Exception var9) {
         var9.printStackTrace();
         return null;
      }
   }

   public static String getString(String url) {
      return getString(url, "OneConfig/1.0.0", 5000, false);
   }

   public static JsonElement getJsonElement(String url, String userAgent, int timeout, boolean useCaches) {
      return JsonUtils.parseString(getString(url, userAgent, timeout, useCaches));
   }

   public static JsonElement getJsonElement(String url) {
      return getJsonElement(url, "OneConfig/1.0.0", 5000, false);
   }

   public static boolean downloadFile(String url, File file, String userAgent, int timeout, boolean useCaches) {
      url = url.replace(" ", "%20");

      try {
         try (
            FileOutputStream fileOut = new FileOutputStream(file);
            BufferedInputStream in = new BufferedInputStream(setupConnection(url, userAgent, timeout, useCaches));
         ) {
            org.apache.commons.io.IOUtils.copy(in, fileOut);
         }

         return true;
      } catch (Exception var13) {
         var13.printStackTrace();
         return false;
      }
   }

   public static boolean downloadFile(String url, File file) {
      return downloadFile(url, file, "OneConfig/1.0.0", 5000, false);
   }

   public static void browseLink(String uri) {
      try {
         Desktop.getDesktop().browse(URI.create(uri));
      } catch (IOException var2) {
      }
   }

   public static InputStream setupConnection(String url, String userAgent, int timeout, boolean useCaches) throws IOException {
      HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
      connection.setRequestMethod("GET");
      connection.setUseCaches(useCaches);
      connection.addRequestProperty("User-Agent", userAgent);
      connection.setReadTimeout(timeout);
      connection.setConnectTimeout(timeout);
      connection.setDoOutput(true);
      return connection.getInputStream();
   }
}
