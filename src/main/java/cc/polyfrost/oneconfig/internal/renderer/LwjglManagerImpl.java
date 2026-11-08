package cc.polyfrost.oneconfig.internal.renderer;

import cc.polyfrost.oneconfig.renderer.LwjglManager;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.TinyFD;
import cc.polyfrost.oneconfig.renderer.asset.AssetHelper;
import cc.polyfrost.oneconfig.renderer.font.FontHelper;
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LwjglManagerImpl implements LwjglManager {
   private static final Logger LOGGER = LogManager.getLogger("OneConfig LWJGL Manager");
   private static final boolean isPojav = checkPojav();
   private static final String LWJGL_FUNCTION_PROVIDER = "cc.polyfrost.oneconfig.internal.plugin.hooks.Lwjgl2FunctionProvider";
   private final Set<String> classLoaderInclude = new CopyOnWriteArraySet<>();
   private final Map<String, Class<?>> classCache = new HashMap<>();
   private static final String JAR_NAME = "oneconfig-lwjgl3.jar";
   private AssetHelper assetHelper;
   private NanoVGHelper nanoVGHelper;
   private ScissorHelper scissorHelper;
   private FontHelper fontHelper;
   private TinyFD tinyFD;

   public LwjglManagerImpl() {
      try {
         this.nanoVGHelper = new NanoVGHelperImpl();
         this.scissorHelper = new ScissorHelperImpl();
         this.assetHelper = new AssetHelperImpl();
         this.fontHelper = new FontHelperImpl();
         this.tinyFD = new TinyFDImpl();
      } catch (Exception var2) {
         var2.printStackTrace();
      }
   }

   private boolean canBeSharedWithMc(String name) {
      for (String implClass : this.classLoaderInclude) {
         if (name.startsWith(implClass)) {
            return false;
         }
      }

      return true;
   }

   private static synchronized URL getJarFile() {
      if (isPojav) {
         return null;
      } else {
         File tempJar = new File("./OneConfig/temp/oneconfig-lwjgl3.jar");
         tempJar.mkdirs();

         try {
            tempJar.createNewFile();
         } catch (IOException var7) {
            var7.printStackTrace();
         }

         tempJar.deleteOnExit();
         String jarName = "lwjgl-legacy";

         try (InputStream in = LwjglManagerImpl.class.getResourceAsStream("/" + jarName + ".jar")) {
            assert in != null;

            Files.copy(in, tempJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
         } catch (IOException var9) {
            var9.printStackTrace();
         }

         try {
            return tempJar.toURI().toURL();
         } catch (MalformedURLException var5) {
            throw new RuntimeException(var5);
         }
      }
   }

   private static boolean checkPojav() {
      try {
         Class.forName("org.lwjgl.glfw.CallbackBridge");
         LOGGER.warn("Pojav detected, letting Pojav handle LWJGL.");
         return true;
      } catch (ClassNotFoundException var1) {
         return false;
      }
   }

   @Override
   public NanoVGHelper getNanoVGHelper() {
      return this.nanoVGHelper;
   }

   @Override
   public ScissorHelper getScissorHelper() {
      return this.scissorHelper;
   }

   @Override
   public AssetHelper getAssetHelper() {
      return this.assetHelper;
   }

   @Override
   public FontHelper getFontHelper() {
      return this.fontHelper;
   }

   @Override
   public TinyFD getTinyFD() {
      return this.tinyFD;
   }
}
