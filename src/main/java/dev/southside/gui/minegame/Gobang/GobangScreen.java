package dev.southside.gui.minegame.Gobang;

import java.awt.Color;
import java.awt.Point;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;

public class GobangScreen extends class_437 {
   private static final int GRID_SIZE = 30;
   private static final int BOARD_MARGIN = 20;
   private GobangGame game;
   public static boolean vsAI = true;
   private long aiMoveTime = 0L;

   public GobangScreen() {
      super(class_2561.method_30163("Gobang"));
      this.game = new GobangGame();
   }

   protected void method_25426() {
      this.game.initGame();
      super.method_25426();
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      super.method_25394(context, mouseX, mouseY, delta);
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
      return (this.field_22789 - 420) / 2;
   }

   private int getBoardStartY() {
      return (this.field_22790 - 420) / 2;
   }

   private void drawBoard(class_332 context) {
      int startX = this.getBoardStartX();
      int startY = this.getBoardStartY();
      int boardPixelSize = 420;
      context.method_25294(startX - 20, startY - 20, startX + boardPixelSize + 20, startY + boardPixelSize + 20, new Color(210, 180, 140).getRGB());

      for (int i = 0; i < 15; i++) {
         context.method_25292(startX, startX + boardPixelSize, startY + i * 30, Color.BLACK.getRGB());
         context.method_25301(startX + i * 30, startY, startY + boardPixelSize, Color.BLACK.getRGB());
      }

      int[] starPoints = new int[]{3, 11};

      for (int x : starPoints) {
         for (int y : starPoints) {
            this.drawStarPoint(context, startX + x * 30, startY + y * 30);
         }
      }

      this.drawStarPoint(context, startX + 210, startY + 210);
   }

   private void drawStarPoint(class_332 context, int x, int y) {
      context.method_25294(x - 2, y - 2, x + 3, y + 3, Color.BLACK.getRGB());
   }

   private void drawPieces(class_332 context) {
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
               context.method_25294(pieceX - radius, pieceY - radius, pieceX + radius + 1, pieceY + radius + 1, color.getRGB());
            }
         }
      }
   }

   private void drawHoverMarker(class_332 context, int mouseX, int mouseY) {
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
               context.method_25294(markerX - radius, markerY - radius, markerX + radius + 1, markerY + radius + 1, color.getRGB());
            }
         }
      }
   }

   private void drawStatus(class_332 context) {
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

      context.method_25300(this.field_22793, statusText, this.field_22789 / 2, 20, Color.WHITE.getRGB());
      String modeText = "Mode: " + (vsAI ? "Player vs AI" : "Player vs Player") + " (Press 'M' to toggle)";
      context.method_25300(this.field_22793, modeText, this.field_22789 / 2, this.field_22790 - 20, Color.GRAY.getRGB());
   }

   private void drawGameOver(class_332 context) {
      String restartText = "Press Enter to Restart";
      context.method_25300(this.field_22793, restartText, this.field_22789 / 2, 40, Color.GREEN.getRGB());
   }

   public boolean method_25402(double mouseX, double mouseY, int button) {
      if (button == 0 && !this.game.isGameOver()) {
         if (vsAI && this.game.getCurrentPlayer() == GobangGame.Player.WHITE) {
            return super.method_25402(mouseX, mouseY, button);
         }

         int startX = this.getBoardStartX();
         int startY = this.getBoardStartY();
         int gridX = (int)Math.round((mouseX - startX) / 30.0);
         int gridY = (int)Math.round((mouseY - startY) / 30.0);
         if (this.game.placePiece(gridX, gridY) && vsAI && !this.game.isGameOver() && this.game.getCurrentPlayer() == GobangGame.Player.WHITE) {
            this.aiMoveTime = System.currentTimeMillis() + 300L;
         }
      }

      return super.method_25402(mouseX, mouseY, button);
   }

   public boolean method_25404(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 77) {
         vsAI = !vsAI;
         this.game.initGame();
         return true;
      } else if (keyCode == 257 && this.game.isGameOver()) {
         this.game.initGame();
         return true;
      } else {
         return super.method_25404(keyCode, scanCode, modifiers);
      }
   }

   public boolean method_25421() {
      return false;
   }
}
