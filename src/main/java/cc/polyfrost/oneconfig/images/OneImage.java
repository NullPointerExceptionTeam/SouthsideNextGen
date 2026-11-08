package cc.polyfrost.oneconfig.images;

import cc.polyfrost.oneconfig.utils.IOUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Objects;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus.Experimental;

@Experimental
public class OneImage {
   private static final Logger LOGGER = LogManager.getLogger("OneConfig Images");
   private BufferedImage image;
   private Graphics2D graphics = null;
   private final int width;
   private final int height;

   public OneImage(String filePath) throws IOException {
      this.image = ImageIO.read(Objects.requireNonNull(OneImage.class.getResourceAsStream(filePath)));
      this.width = this.image.getWidth();
      this.height = this.image.getHeight();
   }

   public OneImage(InputStream is) throws IOException {
      this.image = ImageIO.read(is);
      this.width = this.image.getWidth();
      this.height = this.image.getHeight();
   }

   public OneImage(File file) throws IOException {
      this.image = ImageIO.read(Objects.requireNonNull(file));
      this.width = this.image.getWidth();
      this.height = this.image.getHeight();
   }

   public OneImage(BufferedImage image) {
      this.image = image;
      this.width = image.getWidth();
      this.height = image.getHeight();
   }

   public OneImage(int width, int height) {
      this.width = width;
      this.height = height;
      this.image = new BufferedImage(width, height, 2);
   }

   public BufferedImage getImage() {
      return this.image;
   }

   protected void setImage(BufferedImage img) {
      this.image = img;
   }

   public Graphics2D getG2D() {
      if (this.graphics == null) {
         this.graphics = this.image.createGraphics();
         this.graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      }

      return this.graphics;
   }

   public void dispose() {
      if (this.graphics != null) {
         this.graphics.dispose();
         this.graphics = null;
      }
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public void crop(int startX, int startY, int width, int height) {
      this.image = this.image.getSubimage(startX, startY, width, height);
   }

   public void getColorAtPos(int x, int y) {
      this.image.getRGB(x, y);
   }

   public void setColorAtPos(int x, int y, int argb) {
      this.image.setRGB(x, y, argb);
   }

   public void save(String filePath) throws IOException {
      ImageIO.write(this.image, "png", new File(filePath));
   }

   public JsonObject uploadToImgur() {
      try {
         URL url = new URL("https://api.imgur.com/3/image");
         HttpURLConnection con = (HttpURLConnection)url.openConnection();
         con.setDoInput(true);
         con.setDoOutput(true);
         con.setRequestMethod("POST");
         con.setRequestProperty("Authorization", "Client-ID 6cfc432a9954f4d");
         con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
         con.connect();
         ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
         ImageIO.write(this.image, "png", byteOut);
         String encoded = URLEncoder.encode("image", "UTF-8") + "=" + URLEncoder.encode(Base64.getEncoder().encodeToString(byteOut.toByteArray()), "UTF-8");
         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
         writer.write(encoded);
         byteOut.close();
         writer.close();
         if (con.getResponseCode() != 200) {
            LOGGER.error("Error uploading image to Imgur: " + con.getResponseCode());
            return null;
         } else {
            BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
            JsonObject object = new JsonParser().parse(rd).getAsJsonObject();
            rd.close();
            return object;
         }
      } catch (Exception var8) {
         var8.printStackTrace();
         LOGGER.error("Error uploading image to Imgur.");
         return null;
      }
   }

   public String uploadToImgur(boolean copy) {
      JsonObject object = this.uploadToImgur();
      String link = object.get("data").getAsJsonObject().get("link").getAsString();
      if (copy) {
         IOUtils.copyStringToClipboard(link);
      }

      return link;
   }

   public void copyToClipboard() {
      IOUtils.copyImageToClipboard(this.image);
      this.dispose();
   }

   public void setBrightness(float brightness) {
      this.maskColor(new Color(0.0F, 0.0F, 0.0F, brightness));
   }

   public void maskColor(Color color) {
      Graphics2D g2d = this.getG2D();
      g2d.setColor(color);
      g2d.fillRect(0, 0, this.width, this.height);
      this.dispose();
   }

   public void maskPaint(Paint paint) {
      Graphics2D g2d = this.getG2D();
      g2d.setPaint(paint);
      g2d.fillRect(0, 0, this.width, this.height);
      this.dispose();
   }

   public void drawLine(Stroke stroke, int sx, int sy, int ex, int ey) {
      Graphics2D g2d = this.getG2D();
      g2d.setStroke(stroke);
      g2d.drawLine(sx, sy, ex, ey);
      this.dispose();
   }

   public void drawLine(int sx, int sy, int ex, int ey, int width) {
      this.drawLine(new BasicStroke(width), sx, sy, ex, ey);
   }

   public void drawTexturedRect(TexturePaint paint, int x, int y, int width, int height) {
      Graphics2D g2d = this.getG2D();
      g2d.setPaint(paint);
      g2d.fillRect(x, y, width, height);
      this.dispose();
   }

   public void drawRect(int x, int y, int width, int height, int color) {
      Graphics2D g2d = this.getG2D();
      g2d.setColor(new Color(color, true));
      g2d.drawRect(x, y, width, height);
      this.dispose();
   }

   public void drawRoundedRect(int x, int y, int width, int height, int radius, int color) {
      Graphics2D g2d = this.getG2D();
      g2d.setColor(new Color(color, true));
      g2d.fillRoundRect(x, y, width, height, radius, radius);
      this.dispose();
   }

   public void drawPolygon(int x, int y, Polygon polygon, int color) {
      Graphics2D g2d = this.getG2D();
      g2d.setColor(new Color(color, true));
      g2d.translate(x, y);
      g2d.fillPolygon(polygon);
      this.dispose();
   }

   public void drawOval(int x, int y, int width, int height, int color) {
      Graphics2D g2d = this.getG2D();
      g2d.setColor(new Color(color, true));
      g2d.fillOval(x, y, width, height);
      this.dispose();
   }

   public void drawTriangle(Point p1, Point p2, Point p3, int color) {
      Graphics2D g2d = this.getG2D();
      g2d.setColor(new Color(color, true));
      g2d.fillPolygon(new int[]{p1.x, p2.x, p3.x}, new int[]{p1.y, p2.y, p3.y}, 3);
      this.dispose();
   }

   public void drawCircle(int x, int y, int radius, int color) {
      Graphics2D g2d = this.getG2D();
      g2d.setColor(new Color(color, true));
      g2d.fillRoundRect(x, y, radius, radius, radius, radius);
      this.dispose();
   }

   public void drawString(String text, int x, int y, Font font, int color) {
      Graphics2D g2d = this.getG2D();
      g2d.setColor(new Color(color, true));
      g2d.setFont(font);
      g2d.drawString(text, x, y);
      this.dispose();
   }

   public void scale(double sx, double sy) {
      if (sx != 1.0 || sy != 1.0) {
         BufferedImage old = this.image;
         this.image = new BufferedImage((int)Math.abs(sx * this.image.getWidth()), (int)Math.abs(this.image.getHeight() * sy), 2);
         Graphics2D g2d = this.getG2D();
         g2d.drawImage(old, new AffineTransformOp(AffineTransform.getScaleInstance(sx, sy), 2), 0, 0);
         this.dispose();
      }
   }

   public void setSize(int width, int height) {
      if (width != this.width || height != this.height) {
         this.scale((double)width / this.width, (double)height / this.height);
      }
   }

   public void rotate(double angle) {
      if (angle != 0.0 && angle != 360.0) {
         Graphics2D g2d = this.getG2D();
         g2d.drawImage(this.image, new AffineTransformOp(AffineTransform.getRotateInstance(Math.toRadians(angle)), 2), 0, 0);
         this.dispose();
      }
   }

   public void translate(int moveX, int moveY) {
      Graphics2D g2d = this.getG2D();
      g2d.drawImage(this.image, new AffineTransformOp(AffineTransform.getTranslateInstance(moveX, moveY), 2), 0, 0);
      this.dispose();
   }

   public void flipHorizontal() {
      this.scale(-1.0, 1.0);
   }

   public void flipVertical() {
      this.scale(1.0, -1.0);
   }
}
