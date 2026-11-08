package cc.polyfrost.oneconfig.utils;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.security.MessageDigest;
import org.jetbrains.annotations.NotNull;

public final class IOUtils {
   @Deprecated
   public static ByteBuffer resourceToByteBuffer(String path) throws IOException {
      return resourceToByteBuffer(path, IOUtils.class);
   }

   public static ByteBuffer resourceToByteBuffer(String path, Class<?> clazz) throws IOException {
      path = path.trim();
      byte[] bytes;
      if (path.startsWith("http")) {
         try (InputStream in = NetworkUtils.setupConnection(path, "OneConfig", 5000, true)) {
            bytes = org.apache.commons.io.IOUtils.toByteArray(in);
         }
      } else {
         File file = new File(path);
         InputStream stream;
         if (file.exists() && file.isFile()) {
            stream = Files.newInputStream(file.toPath());
         } else {
            stream = clazz.getResourceAsStream(path);
         }

         if (stream == null) {
            throw new FileNotFoundException(path);
         }

         bytes = org.apache.commons.io.IOUtils.toByteArray(stream);
      }

      ByteBuffer data = ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder()).put(bytes);
      ((Buffer)data).flip();
      return data;
   }

   @Deprecated
   public static ByteBuffer resourceToByteBufferNullable(String path) {
      try {
         return resourceToByteBuffer(path);
      } catch (Exception var2) {
         return null;
      }
   }

   public static ByteBuffer resourceToByteBufferNullable(String path, Class<?> clazz) {
      try {
         return resourceToByteBuffer(path, clazz);
      } catch (Exception var3) {
         return null;
      }
   }

   public static void copyStringToClipboard(String s) {
      StringSelection stringSelection = new StringSelection(s);
      Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
   }

   public static String getStringFromClipboard() {
      try {
         return Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor).toString();
      } catch (Exception var1) {
         return null;
      }
   }

   public static void copyImageToClipboard(Image image) {
      IOUtils.ImageSelection imageSelection = new IOUtils.ImageSelection(image);
      Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imageSelection, null);
   }

   public static Image getImageFromClipboard() {
      try {
         return (Image)Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.imageFlavor);
      } catch (Exception var1) {
         return null;
      }
   }

   public static String getFileChecksum(File file) {
      try {
         String var5;
         try (FileInputStream inputStream = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytesBuffer = new byte[1024];

            int bytesRead;
            while ((bytesRead = inputStream.read(bytesBuffer)) != -1) {
               digest.update(bytesBuffer, 0, bytesRead);
            }

            var5 = convertByteArrayToHexString(digest.digest());
         }

         return var5;
      } catch (Exception var8) {
         var8.printStackTrace();
         return "";
      }
   }

   private static String convertByteArrayToHexString(byte[] arrayBytes) {
      StringBuilder stringBuffer = new StringBuilder();

      for (byte arrayByte : arrayBytes) {
         stringBuffer.append(Integer.toString((arrayByte & 255) + 256, 16).substring(1));
      }

      return stringBuffer.toString();
   }

   private static class ImageSelection implements Transferable {
      private final Image image;

      public ImageSelection(Image image) {
         this.image = image;
      }

      @Override
      public DataFlavor[] getTransferDataFlavors() {
         return new DataFlavor[]{DataFlavor.imageFlavor};
      }

      @Override
      public boolean isDataFlavorSupported(DataFlavor flavor) {
         return DataFlavor.imageFlavor.equals(flavor);
      }

      @NotNull
      @Override
      public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
         if (!DataFlavor.imageFlavor.equals(flavor)) {
            throw new UnsupportedFlavorException(flavor);
         } else {
            return this.image;
         }
      }
   }
}
