package dev.southside.utils.fuck;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class GetQQLocalNumber {
   private static final Path MINIAPP_AUTH_PATH = Paths.get(System.getProperty("user.home"), "AppData", "Roaming", "QQ", "miniapp", "auth");
   private static final Path TENCENT_FILES_PATH = Paths.get(System.getProperty("user.home"), "Documents", "Tencent Files");
   private static final Pattern QQ_PATTERN = Pattern.compile("^\\d{5,12}$");

   public static List<String> getAllQQNumbers() {
      Set<String> qqNumbers = new LinkedHashSet<>();
      collectQQNumbers(MINIAPP_AUTH_PATH, qqNumbers);
      collectQQNumbers(TENCENT_FILES_PATH, qqNumbers);
      return new ArrayList<>(qqNumbers);
   }

   private static void collectQQNumbers(Path dir, Set<String> result) {
      if (Files.exists(dir)) {
         try (DirectoryStream<Path> entries = Files.newDirectoryStream(dir, x$0 -> Files.isDirectory(x$0))) {
            for (Path entry : entries) {
               String name = entry.getFileName().toString();
               if (QQ_PATTERN.matcher(name).matches()) {
                  result.add(name);
               }
            }
         } catch (IOException var8) {
         }
      }
   }
}
