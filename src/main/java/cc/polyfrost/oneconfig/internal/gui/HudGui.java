package cc.polyfrost.oneconfig.internal.gui;

import cc.polyfrost.oneconfig.config.options.impl.HUD;
import cc.polyfrost.oneconfig.gui.GuiPause;
import cc.polyfrost.oneconfig.gui.OneConfigGui;
import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.hud.Position;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.internal.config.core.ConfigCore;
import cc.polyfrost.oneconfig.internal.hud.HudCore;
import cc.polyfrost.oneconfig.internal.hud.utils.GrabOffset;
import cc.polyfrost.oneconfig.internal.hud.utils.SnappingLine;
import cc.polyfrost.oneconfig.libs.universal.UKeyboard;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cc.polyfrost.oneconfig.libs.universal.UResolution;
import cc.polyfrost.oneconfig.libs.universal.UScreen;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.asset.AssetHelper;
import cc.polyfrost.oneconfig.renderer.asset.NVGAsset;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.MathUtils;
import cc.polyfrost.oneconfig.utils.gui.GuiUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HudGui extends UScreen implements GuiPause {
   private static final int SNAPPING_DISTANCE = 10;
   private final HashMap<Hud, GrabOffset> editingHuds = new HashMap<>();
   private boolean isDragging;
   private boolean isSelecting;
   private float selectX;
   private float selectY;
   public boolean isScaling;
   private String superSecretString = "";

   @Override
   public void initScreen(int width, int height) {
      HudCore.editing = true;
      super.initScreen(width, height);
   }

   @Override
   public void onDrawScreen(@NotNull UMatrixStack matrixStack, DrawContext context, int mouseX, int mouseY, float partialTicks) {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      int lineWidth = Math.max(1, Math.round(Math.min(UResolution.getWindowWidth() / 1920.0F, UResolution.getWindowHeight() / 1080.0F)));
      if (this.isDragging) {
         nanoVGHelper.setupAndDraw(vg -> this.setHudPositions(vg, mouseX, mouseY, true, true, lineWidth));
      } else if (this.isSelecting) {
         this.getHudsInRegion(this.selectX, this.selectY, mouseX, mouseY);
      } else if (this.isScaling && this.editingHuds.size() == 1) {
         nanoVGHelper.setupAndDraw(vg -> {
            Hud hudx = (Hud)this.editingHuds.keySet().toArray()[0];
            Position positionx = hudx.position.getValue();
            float scaleX = this.getXSnapping(vg, lineWidth, mouseX, positionx.getWidth(), false);
            hudx.setScale(MathUtils.clamp((scaleX - positionx.getX()) / (positionx.getWidth() / hudx.getScale()), 0.2F, 10.0F), true);
         });
      }

      float scaleFactor = UResolution.getScaleFactor();

      for (Hud hud : HudCore.huds.values()) {
         if (hud.isEnabled()) {
            Position position = hud.position.getValue();
            hud.drawAll(context, matrixStack, true);
            if (this.editingHuds.containsKey(hud)) {
               nanoVGHelper.setupAndDraw(
                  vg -> nanoVGHelper.drawText(
                     vg,
                     position.anchor.toString(),
                     (position.getX() + 2.0F) * scaleFactor,
                     (position.getY() - 6.0F) * scaleFactor,
                     Colors.BLACK,
                     10.0F * scaleFactor,
                     Fonts.MINECRAFT_REGULAR
                  )
               );
               nanoVGHelper.setupAndDraw(
                  true,
                  vg -> nanoVGHelper.drawRect(
                     vg, position.getX(), position.getY(), position.getWidth(), position.getHeight(), new Color(0, 128, 128, 60).getRGB()
                  )
               );
            }

            if (hud.isLocked()) {
               nanoVGHelper.setupAndDraw(
                  true,
                  vg -> nanoVGHelper.drawRect(
                     vg, position.getX(), position.getY(), position.getWidth(), position.getHeight(), new Color(238, 36, 36, 60).getRGB()
                  )
               );
            }

            nanoVGHelper.setupAndDraw(
               vg -> {
                  nanoVGHelper.drawLine(
                     vg,
                     position.getX() * scaleFactor - lineWidth / 2.0F,
                     position.getY() * scaleFactor - lineWidth / 2.0F,
                     position.getRightX() * scaleFactor + lineWidth / 2.0F,
                     position.getY() * scaleFactor - lineWidth / 2.0F,
                     lineWidth,
                     new Color(255, 255, 255).getRGB()
                  );
                  nanoVGHelper.drawLine(
                     vg,
                     position.getX() * scaleFactor - lineWidth / 2.0F,
                     position.getBottomY() * scaleFactor + lineWidth / 2.0F,
                     position.getRightX() * scaleFactor + lineWidth / 2.0F,
                     position.getBottomY() * scaleFactor + lineWidth / 2.0F,
                     lineWidth,
                     new Color(255, 255, 255).getRGB()
                  );
                  nanoVGHelper.drawLine(
                     vg,
                     position.getX() * scaleFactor - lineWidth / 2.0F,
                     position.getY() * scaleFactor - lineWidth / 2.0F,
                     position.getX() * scaleFactor - lineWidth / 2.0F,
                     position.getBottomY() * scaleFactor + lineWidth / 2.0F,
                     lineWidth,
                     new Color(255, 255, 255).getRGB()
                  );
                  nanoVGHelper.drawLine(
                     vg,
                     position.getRightX() * scaleFactor + lineWidth / 2.0F,
                     position.getY() * scaleFactor - lineWidth / 2.0F,
                     position.getRightX() * scaleFactor + lineWidth / 2.0F,
                     position.getBottomY() * scaleFactor + lineWidth / 2.0F,
                     lineWidth,
                     new Color(255, 255, 255).getRGB()
                  );
               }
            );
            if (this.editingHuds.containsKey(hud) && this.editingHuds.size() == 1) {
               nanoVGHelper.setupAndDraw(
                  true,
                  vg -> nanoVGHelper.drawRect(vg, position.getRightX() - 4.0F, position.getBottomY() - 4.0F, 8.0F, 8.0F, new Color(0, 128, 128, 200).getRGB())
               );
            }
         }
      }

      if (this.isSelecting) {
         nanoVGHelper.setupAndDraw(
            true, vg -> nanoVGHelper.drawRect(vg, this.selectX, this.selectY, mouseX - this.selectX, mouseY - this.selectY, new Color(0, 0, 255, 100).getRGB())
         );
      }
   }

   @Override
   public void onMouseClicked(double mouseX, double mouseY, int mouseButton) {
      super.onMouseClicked(mouseX, mouseY, mouseButton);
      if (mouseButton == 1) {
         for (Hud hud : this.editingHuds.keySet()) {
            if (this.mouseClickedHud(hud, (float)mouseX, (float)mouseY) && !hud.isLocked()) {
               float x = hud.position.getValue().getX();
               float y = hud.position.getValue().getY();
               hud.position.getValue().anchor = Position.getAnchor((hud.position.getValue().anchor.ordinal() + 1) % 9);
               hud.position.getValue().setX(x);
               hud.position.getValue().setY(y);
               return;
            }
         }
      }

      if (mouseButton == 0) {
         this.isDragging = false;
         this.isSelecting = false;
         this.isScaling = false;
         if (this.editingHuds.size() == 1) {
            Position position = ((Hud)this.editingHuds.keySet().toArray()[0]).position.getValue();
            if (mouseX >= position.getRightX() - 7.0F
               && mouseX <= position.getRightX() + 7.0F
               && mouseY >= position.getBottomY() - 7.0F
               && mouseY <= position.getBottomY() + 7.0F) {
               this.isScaling = true;
               return;
            }
         }

         for (Hud hudx : HudCore.huds.values()) {
            if (hudx.isEnabled() && this.mouseClickedHud(hudx, (float)mouseX, (float)mouseY) && !hudx.isLocked()) {
               if (!this.editingHuds.containsKey(hudx)) {
                  if (!UKeyboard.isCtrlKeyDown()) {
                     this.editingHuds.clear();
                  }

                  this.editingHuds.put(hudx, new GrabOffset());
               }

               this.isDragging = true;
               this.editingHuds
                  .forEach(
                     (hud2, grabOffset) -> grabOffset.setOffset(
                        (float)(mouseX - hud2.position.getValue().getX()), (float)(mouseY - hud2.position.getValue().getY())
                     )
                  );
               return;
            }
         }

         this.isSelecting = true;
         this.selectX = (float)mouseX;
         this.selectY = (float)mouseY;
      }
   }

   @Override
   public void onMouseReleased(double mouseX, double mouseY, int state) {
      this.isDragging = false;
      this.isSelecting = false;
      this.isScaling = false;
      super.onMouseReleased(mouseX, mouseY, state);
   }

   @Override
   public void onKeyPressed(int keyCode, int scanCode, @Nullable int modifiers) {
      if (!this.isDragging) {
         this.editingHuds.forEach((hud, grabOffset) -> grabOffset.setOffset(-hud.position.getValue().getX(), -hud.position.getValue().getY()));
         if (keyCode == 265) {
            this.setHudPositions(0.0F, -1.0F, false);
         } else if (keyCode == 264) {
            this.setHudPositions(0.0F, 1.0F, false);
         } else if (keyCode == 263) {
            this.setHudPositions(-1.0F, 0.0F, false);
         } else if (keyCode == 262) {
            this.setHudPositions(1.0F, 0.0F, false);
         }

         super.onKeyPressed(keyCode, scanCode, modifiers);
      }
   }

   @Override
   public void onCharTyped(char c, int modifiers) {
      if (!this.isDragging) {
         this.superSecretMethod(c);
         super.onCharTyped(c, modifiers);
      }
   }

   @Override
   public void onScreenClose() {
      super.onScreenClose();
      HudCore.editing = false;
      ConfigCore.saveAll();
      GuiUtils.displayScreen((Screen)OneConfigGui.create());
   }

   @Override
   public boolean doesGuiPauseGame() {
      return false;
   }

   private void getHudsInRegion(float x1, float y1, float x2, float y2) {
      if (x1 > x2) {
         float temp = x1;
         x1 = x2;
         x2 = temp;
      }

      if (y1 > y2) {
         float temp = y1;
         y1 = y2;
         y2 = temp;
      }

      this.editingHuds.clear();

      for (Hud hud : HudCore.huds.values()) {
         if (hud.isEnabled() && !hud.isLocked()) {
            Position pos = hud.position.getValue();
            if ((x1 <= pos.getX() && x2 >= pos.getX() || x1 <= pos.getRightX() && x2 >= pos.getRightX())
               && (y1 <= pos.getY() && y2 >= pos.getY() || y1 <= pos.getBottomY() && y2 >= pos.getBottomY())) {
               this.editingHuds.put(hud, new GrabOffset());
            }
         }
      }
   }

   private void setHudPositions(long vg, float mouseX, float mouseY, boolean snap, boolean locked, int lineWidth) {
      for (Hud hud : this.editingHuds.keySet()) {
         GrabOffset grabOffset = this.editingHuds.get(hud);
         Position position = hud.position.getValue();
         float x = mouseX - grabOffset.getX();
         float y = mouseY - grabOffset.getY();
         if (this.editingHuds.size() == 1 && snap) {
            x = this.getXSnapping(vg, lineWidth, x, position.getWidth(), true);
            y = this.getYSnapping(vg, lineWidth, y, position.getHeight(), true);
         }

         if (locked) {
            if (x < 0.0F) {
               x = 0.0F;
            } else if (x + position.getWidth() > UResolution.getScaledWidth()) {
               x = UResolution.getScaledWidth() - position.getWidth();
            }

            if (y < 0.0F) {
               y = 0.0F;
            } else if (y + position.getHeight() > UResolution.getScaledHeight()) {
               y = UResolution.getScaledHeight() - position.getHeight();
            }
         }

         position.setPosition(x, y);
      }
   }

   private void setHudPositions(float mouseX, float mouseY, boolean locked) {
      this.setHudPositions(0L, mouseX, mouseY, false, locked, 0);
   }

   private boolean mouseClickedHud(Hud hud, float mouseX, float mouseY) {
      Position position = hud.position.getValue();
      return mouseX >= position.getX() && mouseX <= position.getRightX() && mouseY >= position.getY() && mouseY <= position.getBottomY();
   }

   private float getXSnapping(long vg, float lineWidth, float x, float width, boolean multipleSides) {
      ArrayList<Float> lines = this.getXSnappingLines();
      ArrayList<SnappingLine> snappingLines = new ArrayList<>();
      float closest = 10.0F / UResolution.getScaleFactor();

      for (Float line : lines) {
         SnappingLine snappingLine = new SnappingLine(line, x, width, multipleSides);
         if (Math.round(snappingLine.getDistance()) == Math.round(closest)) {
            snappingLines.add(snappingLine);
         } else if (snappingLine.getDistance() < closest) {
            closest = snappingLine.getDistance();
            snappingLines.clear();
            snappingLines.add(snappingLine);
         }
      }

      if (snappingLines.isEmpty()) {
         return x;
      } else {
         for (SnappingLine snappingLine : snappingLines) {
            snappingLine.drawLine(vg, lineWidth, true);
         }

         return snappingLines.get(0).getPosition();
      }
   }

   private ArrayList<Float> getXSnappingLines() {
      ArrayList<Float> lines = new ArrayList<>();
      lines.add(UResolution.getScaledWidth() / 2.0F);

      for (Hud hud : HudCore.huds.values()) {
         if (hud.isEnabled() && !this.editingHuds.containsKey(hud)) {
            lines.add(hud.position.getValue().getX());
            lines.add(hud.position.getValue().getCenterX());
            lines.add(hud.position.getValue().getRightX());
         }
      }

      return lines;
   }

   private float getYSnapping(long vg, float lineWidth, float y, float height, boolean multipleSides) {
      ArrayList<Float> lines = this.getYSnappingLines();
      ArrayList<SnappingLine> snappingLines = new ArrayList<>();
      float closest = 10.0F / UResolution.getScaleFactor();

      for (Float line : lines) {
         SnappingLine snappingLine = new SnappingLine(line, y, height, multipleSides);
         if (Math.round(snappingLine.getDistance()) == Math.round(closest)) {
            snappingLines.add(snappingLine);
         } else if (snappingLine.getDistance() < closest) {
            closest = snappingLine.getDistance();
            snappingLines.clear();
            snappingLines.add(snappingLine);
         }
      }

      if (snappingLines.isEmpty()) {
         return y;
      } else {
         for (SnappingLine snappingLine : snappingLines) {
            snappingLine.drawLine(vg, lineWidth, false);
         }

         return snappingLines.get(0).getPosition();
      }
   }

   private ArrayList<Float> getYSnappingLines() {
      ArrayList<Float> lines = new ArrayList<>();
      lines.add(UResolution.getScaledHeight() / 2.0F);

      for (Hud hud : HudCore.huds.values()) {
         if (hud.isEnabled() && !this.editingHuds.containsKey(hud)) {
            lines.add(hud.position.getValue().getY());
            lines.add(hud.position.getValue().getCenterY());
            lines.add(hud.position.getValue().getBottomY());
         }
      }

      return lines;
   }

   private void superSecretMethod(char charTyped) {
      this.superSecretString = this.superSecretString + charTyped;
      this.superSecretString = this.superSecretString.toLowerCase();
      if (!"blahaj".substring(0, this.superSecretString.length()).equals(this.superSecretString)
         && !"blåhaj".substring(0, this.superSecretString.length()).equals(this.superSecretString)
         && !"bigrat".substring(0, this.superSecretString.length()).equals(this.superSecretString)) {
         this.superSecretString = "";
      } else if ("blahaj".equals(this.superSecretString) || "blåhaj".equals(this.superSecretString) || "bigrat".equals(this.superSecretString)) {
         String imageName = this.superSecretString;
         final String url;
         switch (imageName) {
            case "blahaj":
            case "blåhaj":
               url = "https://blahaj.shop/api/random/image?" + UUID.randomUUID();
               break;
            case "bigrat":
               url = "https://bigrat.monster/media/bigrat.png";
               break;
            default:
               return;
         }

         this.superSecretString = "";
         AtomicBoolean loaded = new AtomicBoolean();
         AssetHelper assetHelper = AssetHelper.INSTANCE;
         NanoVGHelper.INSTANCE.setupAndDraw(vg -> loaded.set(assetHelper.loadImage(vg, url, HudGui.class)));
         if (loaded.get()) {
            NVGAsset image = assetHelper.getNVGImage(url);
            int w = image.getWidth();
            int h = image.getHeight();
            float s = Math.min(300.0F / w, 300.0F / h);
            final float width = w * s;
            final float height = h * s;
            HudCore.huds.put(new HUD("bigrat", null), new Hud() {
               @Override
               protected void draw(DrawContext context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
                  NanoVGHelper.INSTANCE.setupAndDraw(true, vg -> NanoVGHelper.INSTANCE.drawImage(vg, url, x, y, width * scale, height * scale));
               }

               @Override
               protected float getWidth(float scale, boolean example) {
                  return width * scale;
               }

               @Override
               protected float getHeight(float scale, boolean example) {
                  return height * scale;
               }
            });
         }
      }
   }
}
