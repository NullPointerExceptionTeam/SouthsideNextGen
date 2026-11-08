package cc.polyfrost.oneconfig.internal.init;

import java.io.File;

public class OneConfigInit {
   public static void initialize(String[] args) {
      File oneConfigDir = new File("./OneConfig");
      oneConfigDir.mkdirs();
      new File(oneConfigDir, "configs").mkdirs();
   }
}
