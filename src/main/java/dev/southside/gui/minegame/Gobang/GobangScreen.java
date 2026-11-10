package dev.southside.gui.minegame.Gobang;

import java.awt.Color;
import java.awt.Point;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

public class GobangScreen extends Screen {
   private static final int GRID_SIZE = 30;
   private static final int BOARD_MARGIN = 20;
   private GobangGame game;
   public static boolean vsAI = true;
   private long aiMoveTime = 0L;

   public GobangScreen() {
      super(Text.of("Gobang"));
      this.game = new GobangGame();
   }

   protected void init() {
      this.game.initGame();
      super.init();
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      super.render(context, mouseX, mouseY, delta);
      if (vsAI && !this.game.isGameOver() && this.game.getCurrentPlayer() == GobangGame.Player.WHITE && System.currentTimeMillis() >= this.aiMoveTime) {
         Point bestMove = GobangAI.getBestMove(this.game, GobangGame.Player.WHITE);
         if (bestMove != null) {
            this.game.placePiece(bestMove.x, bestMove.y);
         }
      }

      this.drawBoard(context);
      this.drawPieces(context);
      this.drawHoverMarker(context, mouseX, mouseY);
      this.drawStatus(context);
      if (this.game.isGameOver()) {
         this.drawGameOver(context);
      }
   }

   private int getBoardStartX() {
      return (this.width - 420) / 2;
   }

   private int getBoardStartY() {
      return (this.height - 420) / 2;
   }

   private void drawBoard(DrawContext context) {
      int startX = this.getBoardStartX();
      int startY = this.getBoardStartY();
      int boardPixelSize = 420;
      context.fill(startX - 20, startY - 20, startX + boardPixelSize + 20, startY + boardPixelSize + 20, new Color(210, 180, 140).getRGB());

      for (int i = 0; i < 15; i++) {
         context.drawHorizontalLine(startX, startX + boardPixelSize, startY + i * 30, Color.BLACK.getRGB());
         context.drawVerticalLine(startX + i * 30, startY, startY + boardPixelSize, Color.BLACK.getRGB());
      }

      int[] starPoints = new int[]{3, 11};

      for (int x : starPoints) {
         for (int y : starPoints) {
            this.drawStarPoint(context, startX + x * 30, startY + y * 30);
         }
      }

      this.drawStarPoint(context, startX + 210, startY + 210);
   }

   private void drawStarPoint(DrawContext context, int x, int y) {
      context.fill(x - 2, y - 2, x + 3, y + 3, Color.BLACK.getRGB());
   }

   private void drawPieces(DrawContext context) {
      int startX = this.getBoardStartX();
      int startY = this.getBoardStartY();
      GobangGame.Player[][] board = this.game.getBoard();

      for (int y = 0; y < 15; y++) {
         for (int x = 0; x < 15; x++) {
            if (board[y][x] != null) {
               Color color = board[y][x] == GobangGame.Player.BLACK ? Color.BLACK : Color.WHITE;
               int pieceX = startX + x * 30;
               int pieceY = startY + y * 30;
               int radius = 13;
               context.fill(pieceX - radius, pieceY - radius, pieceX + radius + 1, pieceY + radius + 1, color.getRGB());
            }
         }
      }
   }

   private void drawHoverMarker(DrawContext context, int mouseX, int mouseY) {
      if (!this.game.isGameOver()) {
         int startX = this.getBoardStartX();
         int startY = this.getBoardStartY();
         int boardPixelSize = 420;
         if (mouseX >= startX - 15 && mouseX <= startX + boardPixelSize + 15 && mouseY >= startY - 15 && mouseY <= startY + boardPixelSize + 15) {
            int gridX = (mouseX - startX + 15) / 30;
            int gridY = (mouseY - startY + 15) / 30;
            if (gridX >= 0 && gridX < 15 && gridY >= 0 && gridY < 15) {
               int markerX = startX + gridX * 30;
               int markerY = startY + gridY * 30;
               Color color = this.game.getCurrentPlayer() == GobangGame.Player.BLACK ? new Color(0, 0, 0, 128) : new Color(255, 255, 255, 128);
               int radius = 13;
               context.fill(markerX - radius, markerY - radius, markerX + radius + 1, markerY + radius + 1, color.getRGB());
            }
         }
      }
   }

   private void drawStatus(DrawContext context) {
      String statusText;
      if (this.game.isGameOver()) {
         if (this.game.getWinner() != null) {
            statusText = (this.game.getWinner() == GobangGame.Player.BLACK ? "Black" : "White") + " Wins!";
         } else {
            statusText = "It's a Draw!";
         }
      } else {
         statusText = "Current Turn: " + (this.game.getCurrentPlayer() == GobangGame.Player.BLACK ? "Black" : "White");
      }

      context.drawCenteredTextWithShadow(this.textRenderer, statusText, this.width / 2, 20, Color.WHITE.getRGB());
      String modeText = "Mode: " + (vsAI ? "Player vs AI" : "Player vs Player") + " (Press 'M' to toggle)";
      context.drawCenteredTextWithShadow(this.textRenderer, modeText, this.width / 2, this.height - 20, Color.GRAY.getRGB());
   }

   private void drawGameOver(DrawContext context) {
      String restartText = "Press Enter to Restart";
      context.drawCenteredTextWithShadow(this.textRenderer, restartText, this.width / 2, 40, Color.GREEN.getRGB());
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0 && !this.game.isGameOver()) {
         if (vsAI && this.game.getCurrentPlayer() == GobangGame.Player.WHITE) {
            return super.mouseClicked(mouseX, mouseY, button);
         }

         int startX = this.getBoardStartX();
         int startY = this.getBoardStartY();
         int gridX = (int)Math.round((mouseX - startX) / 30.0);
         int gridY = (int)Math.round((mouseY - startY) / 30.0);
         if (this.game.placePiece(gridX, gridY) && vsAI && !this.game.isGameOver() && this.game.getCurrentPlayer() == GobangGame.Player.WHITE) {
            this.aiMoveTime = System.currentTimeMillis() + 300L;
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 77) {
         vsAI = !vsAI;
         this.game.initGame();
         return true;
      } else if (keyCode == 257 && this.game.isGameOver()) {
         this.game.initGame();
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   public boolean shouldPause() {
      return false;
   }
}
