package cc.polyfrost.oneconfig.internal.plugin.hooks;

import cc.polyfrost.oneconfig.internal.OneConfig;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import org.lwjgl.system.FunctionProvider;

public class Lwjgl2FunctionProvider implements FunctionProvider {
   private static final Class<?> GL_CONTEXT;
   private final Method getFunctionAddress;

   public Lwjgl2FunctionProvider() {
      try {
         this.getFunctionAddress = GL_CONTEXT.getDeclaredMethod("getFunctionAddress", String.class);
         this.getFunctionAddress.setAccessible(true);
      } catch (ReflectiveOperationException var2) {
         throw new RuntimeException("Error initializing LWJGL2FunctionProvider", var2);
      }
   }

   public long getFunctionAddress(CharSequence functionName) {
      try {
         return (Long)this.getFunctionAddress.invoke(null, functionName.toString());
      } catch (ReflectiveOperationException var3) {
         throw new RuntimeException(var3);
      }
   }

   public long getFunctionAddress(ByteBuffer byteBuffer) {
      throw new UnsupportedOperationException("LWJGL 2 does not support this method");
   }

   static {
      String libraryPath = System.getProperty("oneconfig.lwjgl2.librarypath", "");
      if (!libraryPath.isEmpty()) {
         System.setProperty("org.lwjgl.librarypath", libraryPath);
      }

      try {
         GL_CONTEXT = Class.forName("org.lwjgl.opengl.GLContext", true, OneConfig.class.getClassLoader());
      } catch (ReflectiveOperationException var2) {
         throw new RuntimeException(var2);
      }
   }
}
