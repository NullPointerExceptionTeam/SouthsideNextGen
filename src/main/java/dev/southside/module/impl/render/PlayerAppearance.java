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
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public class PlayerAppearance extends Module {
   public Switch setCape = new Switch("Client Cape", true);
   public Switch setSkin = new Switch("Player Skin", true);
   public Switch setWings = new Switch("Dragon Wings", true);
   public Button setCapeButton = new Button("Set Cape", "File Selector", "cnm", 1, this.setCapeFile());
   public Button setSkinButton = new Button("Set Skin", "File Selector", "cnm", 1, this.setSkinFile());
   private Identifier customCapeTexture;
   private Identifier customSkinTexture;

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
                     MinecraftClient.getInstance().getTextureManager().destroyTexture(this.customSkinTexture);
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
                     MinecraftClient.getInstance().getTextureManager().destroyTexture(this.customCapeTexture);
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
         this.customSkinTexture = Identifier.of(Southside.CLIENT_NAME.toLowerCase(), "textures/skin/skin.png");
      }

      if (this.customCapeTexture == null) {
         this.customCapeTexture = Identifier.of(Southside.CLIENT_NAME.toLowerCase(), "textures/cape/peiban.png");
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
                  MinecraftClient.getInstance().getTextureManager().destroyTexture(this.customCapeTexture);
               } else if (fileName.equals("skin.png")) {
                  MinecraftClient.getInstance().getTextureManager().destroyTexture(this.customSkinTexture);
               }
            }
         }
      }

      this.customCapeTexture = null;
      this.customSkinTexture = null;
      super.onDisable();
   }

   private Identifier loadTextureFromDisk(String textureType, String textureName, String filePath) {
      try {
         String namespace = Southside.CLIENT_NAME.toLowerCase();
         String path = "textures/" + textureType + "/" + textureName;
         Identifier identifier = Identifier.of(namespace, path);
         this.loadAndRegisterTexture(identifier, new File(filePath));
         return identifier;
      } catch (Exception var7) {
         System.err.println("Failed to load texture from: " + filePath);
         var7.printStackTrace();
         return null;
      }
   }

   private void loadAndRegisterTexture(Identifier identifier, File textureFile) {
      MinecraftClient.getInstance().execute(() -> {
         try (InputStream stream = new FileInputStream(textureFile)) {
            NativeImage image = NativeImage.read(stream);
            NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
            MinecraftClient.getInstance().getTextureManager().registerTexture(identifier, texture);
            System.out.println("Successfully loaded custom texture: " + identifier);
         } catch (IOException var7) {
            System.err.println("Failed to load texture: " + textureFile.getAbsolutePath());
            var7.printStackTrace();
         }
      });
   }

   @Generated
   public Identifier getCustomCapeTexture() {
      return this.customCapeTexture;
   }

   @Generated
   public Identifier getCustomSkinTexture() {
      return this.customSkinTexture;
   }
}
