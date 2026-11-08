package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.options.impl.Button;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.Southside;
import dev.southside.module.Category;
import dev.southside.module.Module;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import lombok.Generated;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public class PlayerAppearance extends Module {
   public Switch setCape = new Switch("Client Cape", true);
   public Switch setSkin = new Switch("Player Skin", true);
   public Switch setWings = new Switch("Dragon Wings", true);
   public Button setCapeButton = new Button("Set Cape", "File Selector", "cnm", 1, this.setCapeFile());
   public Button setSkinButton = new Button("Set Skin", "File Selector", "cnm", 1, this.setSkinFile());
   private class_2960 customCapeTexture;
   private class_2960 customSkinTexture;

   private String openPngDialog(String title) {
      MemoryStack stack = MemoryStack.stackPush();

      String var4;
      try {
         PointerBuffer patterns = stack.mallocPointer(1);
         patterns.put(stack.UTF8("*.png")).flip();
         var4 = TinyFileDialogs.tinyfd_openFileDialog(title, null, patterns, "PNG (*.png)", false);
      } catch (Throwable var6) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (stack != null) {
         stack.close();
      }

      return var4;
   }

   public Runnable setSkinFile() {
      return () -> {
         String filePath = this.openPngDialog("Select Skin");
         if (filePath != null) {
            File src = new File(filePath);
            if (src.isFile()) {
               File targetDir = Southside.FOLDER;
               if (!targetDir.exists()) {
                  targetDir.mkdirs();
               }

               File dst = new File(targetDir, "skin.png");

               try {
                  Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
                  if (this.customSkinTexture != null) {
                     class_310.method_1551().method_1531().method_4615(this.customSkinTexture);
                  }

                  this.customSkinTexture = this.loadTextureFromDisk("skin", "custom_skin", dst.getAbsolutePath());
               } catch (Exception var6) {
               }
            }
         }
      };
   }

   public Runnable setCapeFile() {
      return () -> {
         String filePath = this.openPngDialog("Select Cape");
         if (filePath != null) {
            File src = new File(filePath);
            if (src.isFile()) {
               File targetDir = Southside.FOLDER;
               if (!targetDir.exists()) {
                  targetDir.mkdirs();
               }

               File dst = new File(targetDir, "cape.png");

               try {
                  Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
                  if (this.customCapeTexture != null) {
                     class_310.method_1551().method_1531().method_4615(this.customCapeTexture);
                  }

                  this.customCapeTexture = this.loadTextureFromDisk("cape", "custom_cape", dst.getAbsolutePath());
               } catch (Exception var6) {
               }
            }
         }
      };
   }

   public PlayerAppearance() {
      super(Category.Render, "Player Appearance", "Allows you to change your player's appearance.");
   }

   @Override
   public void onEnable() {
      if (Southside.FOLDER.exists() && Southside.FOLDER.isDirectory()) {
         File[] list = Southside.FOLDER.listFiles();
         if (list != null) {
            for (File file : list) {
               String fileName = file.getName().toLowerCase();
               if (fileName.equals("cape.png")) {
                  this.customCapeTexture = this.loadTextureFromDisk("cape", "custom_cape", file.getAbsolutePath());
               } else if (fileName.equals("skin.png")) {
                  this.customSkinTexture = this.loadTextureFromDisk("skin", "custom_skin", file.getAbsolutePath());
               }
            }
         }
      }

      if (this.customSkinTexture == null) {
         this.customSkinTexture = class_2960.method_60655(Southside.CLIENT_NAME.toLowerCase(), "textures/skin/skin.png");
      }

      if (this.customCapeTexture == null) {
         this.customCapeTexture = class_2960.method_60655(Southside.CLIENT_NAME.toLowerCase(), "textures/cape/peiban.png");
      }

      super.onEnable();
   }

   @Override
   public void onDisable() {
      if (Southside.FOLDER.exists() && Southside.FOLDER.isDirectory()) {
         File[] list = Southside.FOLDER.listFiles();
         if (list != null) {
            for (File file : list) {
               String fileName = file.getName().toLowerCase();
               if (fileName.equals("cape.png")) {
                  class_310.method_1551().method_1531().method_4615(this.customCapeTexture);
               } else if (fileName.equals("skin.png")) {
                  class_310.method_1551().method_1531().method_4615(this.customSkinTexture);
               }
            }
         }
      }

      this.customCapeTexture = null;
      this.customSkinTexture = null;
      super.onDisable();
   }

   private class_2960 loadTextureFromDisk(String textureType, String textureName, String filePath) {
      try {
         String namespace = Southside.CLIENT_NAME.toLowerCase();
         String path = "textures/" + textureType + "/" + textureName;
         class_2960 identifier = class_2960.method_60655(namespace, path);
         this.loadAndRegisterTexture(identifier, new File(filePath));
         return identifier;
      } catch (Exception var7) {
         System.err.println("Failed to load texture from: " + filePath);
         var7.printStackTrace();
         return null;
      }
   }

   private void loadAndRegisterTexture(class_2960 identifier, File textureFile) {
      class_310.method_1551().execute(() -> {
         try (InputStream stream = new FileInputStream(textureFile)) {
            class_1011 image = class_1011.method_4309(stream);
            class_1043 texture = new class_1043(image);
            class_310.method_1551().method_1531().method_4616(identifier, texture);
            System.out.println("Successfully loaded custom texture: " + identifier);
         } catch (IOException var7) {
            System.err.println("Failed to load texture: " + textureFile.getAbsolutePath());
            var7.printStackTrace();
         }
      });
   }

   @Generated
   public class_2960 getCustomCapeTexture() {
      return this.customCapeTexture;
   }

   @Generated
   public class_2960 getCustomSkinTexture() {
      return this.customSkinTexture;
   }
}
