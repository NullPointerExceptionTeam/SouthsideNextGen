package dev.southside.render.shader;

import dev.southside.MinecraftAccessor;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.lang.ref.Reference;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;
import java.util.Locale;
import java.util.Map;
import lombok.Generated;
import net.minecraft.class_2960;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShaderFactory {
   @Generated
   private static final Logger log = LoggerFactory.getLogger(ShaderFactory.class);
   private static final Map<String, CompiledStage> compiledShaders = new Object2ObjectOpenHashMap();
   private static final Map<String, UniformBuffer> ubo = new Object2ObjectOpenHashMap();
   private static final Map<String, ShaderStorageBuffer> ssbo = new Object2ObjectOpenHashMap();

   public static CompiledStage compile(class_2960 source, String identifier, ShaderType type) {
      try {
         return compile(IOUtils.toString(MinecraftAccessor.mc.method_1478().open(source), StandardCharsets.UTF_8), identifier, type);
      } catch (IOException var4) {
         throw new RuntimeException("Failed to compile shader ", var4);
      }
   }

   public static CompiledStage compile(String source, String identifier, ShaderType type) {
      return compile(MemoryUtil.memUTF8(source), identifier, type);
   }

   public static CompiledStage compile(ByteBuffer source, String identifier, ShaderType type) {
      int shader = GL45C.glCreateShader(type.nativeType);
      if (shader == 0) {
         return null;
      } else {
         try {
            MemoryStack stack = MemoryStack.stackPush();

            try {
               long string = stack.npointer(source);
               long length = stack.nint(source.remaining());
               GL45C.nglShaderSource(shader, 1, string, length);
            } catch (Throwable var14) {
               if (stack != null) {
                  try {
                     stack.close();
                  } catch (Throwable var13) {
                     var14.addSuppressed(var13);
                  }
               }

               throw var14;
            }

            if (stack != null) {
               stack.close();
            }
         } finally {
            Reference.reachabilityFence(source);
         }

         GL45C.glCompileShader(shader);
         String var16 = MemoryUtil.memUTF8(source);
         if (GL45C.glGetShaderi(shader, 35713) == 0) {
            String log = GL45C.glGetShaderInfoLog(shader);
            GL45C.glDeleteShader(shader);
            handleCompileError(var16, log);
            return null;
         } else {
            CompiledStage stage = new CompiledStage(type, shader, identifier, var16);
            compiledShaders.put(identifier, stage);
            return stage;
         }
      }
   }

   public static Shader make(CompiledStage... stages) {
      return make(false, null, stages);
   }

   public static Shader make(ShaderCallback callback, CompiledStage... stages) {
      return make(false, callback, stages);
   }

   public static Shader make(boolean useMatUniformBlock, CompiledStage... stages) {
      return make(useMatUniformBlock, null, stages);
   }

   public static Shader make(boolean useMatUniformBlock, ShaderCallback callback, CompiledStage... stages) {
      int program = GL45C.glCreateProgram();

      for (CompiledStage stage : stages) {
         stage.attach(program);
      }

      GL45C.glLinkProgram(program);
      if (GL45C.glGetProgrami(program, 35714) == 0) {
         Object var21;
         try {
            String log = GL45C.glGetProgramInfoLog(program);
            String[] headers = new String[stages.length];

            for (int i = 0; i < headers.length; i++) {
               headers[i] = stages[i].getIdentifier() + " / " + stages[i].getType().name;
            }

            String[] sources = new String[stages.length];

            for (int i = 0; i < headers.length; i++) {
               sources[i] = stages[i].getSource();
            }

            handleLinkError(headers, sources, log);
            var21 = null;
         } finally {
            GL45C.glDeleteProgram(program);
         }

         return (Shader)var21;
      } else {
         for (CompiledStage stage : stages) {
            stage.detach(program);
            if (stage.isFragmentShader()) {
               compiledShaders.remove(stage.getIdentifier()).close();
            }
         }

         Shader shader = new Shader(program);
         if (useMatUniformBlock) {
            shader.bindUniformBlock("MatrixBlock", 0);
         }

         if (callback != null) {
            callback.call(shader);
         }

         return shader;
      }
   }

   public static void bindUBO(String name, int index) {
      UniformBuffer buf = findUBO(name);
      if (buf != null) {
         buf.bindBase(index);
      }
   }

   public static void bindSSBO(String name, int index) {
      ShaderStorageBuffer buf = findSSBO(name);
      if (buf != null) {
         buf.bindBase(index);
      }
   }

   public static UniformBuffer findOrCreateUBO(String name, int size) {
      return ubo.computeIfAbsent(name, __ -> makeUniformBuffer(size));
   }

   public static ShaderStorageBuffer findOrCreateSSBO(String name, int size) {
      return ssbo.computeIfAbsent(name, __ -> makeShaderStorageBuffer(size));
   }

   public static UniformBuffer findUBO(String name) {
      return ubo.get(name);
   }

   public static ShaderStorageBuffer findSSBO(String name) {
      return ssbo.get(name);
   }

   public static UniformBuffer makeUniformBuffer(int size) {
      UniformBuffer buffer = new UniformBuffer();
      buffer.allocate(size);
      return buffer;
   }

   public static ShaderStorageBuffer makeShaderStorageBuffer(int size) {
      ShaderStorageBuffer buffer = new ShaderStorageBuffer();
      buffer.allocate(size);
      return buffer;
   }

   private static void handleCompileError(String source, String errors) {
      Formatter f = new Formatter();
      f.format("Shader compilation error%n");
      f.format("------------------------%n");
      String[] lines = source.split("\n");

      for (int i = 0; i < lines.length; i++) {
         f.format(Locale.ROOT, "%4d\t%s%n", i + 1, lines[i]);
      }

      f.format(errors);
      log.error(f.toString());
   }

   public static void handleLinkError(String[] headers, String[] sources, String errors) {
      Formatter f = new Formatter();
      f.format("Program linking error%n");
      f.format("---------------------%n");

      for (int i = 0; i < headers.length; i++) {
         if (sources[i] != null) {
            f.format("%s%n", headers[i]);
            String[] lines = sources[i].split("\n");

            for (int j = 0; j < lines.length; j++) {
               f.format(Locale.ROOT, "%4d\t%s%n", j + 1, lines[j]);
            }
         }
      }

      f.format(errors);
      log.error(f.toString());
   }

   public static void close() {
      ubo.values().forEach(UniformBuffer::close);
      ssbo.values().forEach(ShaderStorageBuffer::close);
      ubo.clear();
      ssbo.clear();
   }
}
