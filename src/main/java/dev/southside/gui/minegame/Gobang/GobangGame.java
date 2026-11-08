package dev.southside.gui.minegame.Gobang;

import lombok.Generated;

public class GobangGame {
   public static final int BOARD_SIZE = 15;
   private final GobangGame.Player[][] board = new GobangGame.Player[15][15];
   private GobangGame.Player currentPlayer;
   private boolean gameOver;
   private GobangGame.Player winner;
   private int moveCount;

   public GobangGame() {
      this.initGame();
   }

   public void initGame() {
      for (int i = 0; i < 15; i++) {
         for (int j = 0; j < 15; j++) {
            this.board[i][j] = null;
         }
      }

      this.currentPlayer = GobangGame.Player.BLACK;
      this.gameOver = false;
      this.winner = null;
      this.moveCount = 0;
   }

   public boolean placePiece(int x, int y) {
      if (!this.gameOver && x >= 0 && x < 15 && y >= 0 && y < 15 && this.board[y][x] == null) {
         this.board[y][x] = this.currentPlayer;
         this.moveCount++;
         if (this.checkForWin(x, y)) {
            this.gameOver = true;
            this.winner = this.currentPlayer;
         } else if (this.moveCount == 225) {
            this.gameOver = true;
         } else {
            this.currentPlayer = this.currentPlayer == GobangGame.Player.BLACK ? GobangGame.Player.WHITE : GobangGame.Player.BLACK;
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean checkForWin(int x, int y) {
      GobangGame.Player player = this.board[y][x];
      return this.checkLine(x, y, 1, 0, player)
         || this.checkLine(x, y, 0, 1, player)
         || this.checkLine(x, y, 1, 1, player)
         || this.checkLine(x, y, 1, -1, player);
   }

   private int countPieces(int x, int y, int dx, int dy, GobangGame.Player player) {
      int count = 0;

      for (int i = 1; i < 5; i++) {
         int nx = x + i * dx;
         int ny = y + i * dy;
         if (nx < 0 || nx >= 15 || ny < 0 || ny >= 15 || this.board[ny][nx] != player) {
            break;
         }

         count++;
      }

      return count;
   }

   private boolean checkLine(int x, int y, int dx, int dy, GobangGame.Player player) {
      int count = 1;
      count += this.countPieces(x, y, dx, dy, player);
      count += this.countPieces(x, y, -dx, -dy, player);
      return count >= 5;
   }

   @Generated
   public GobangGame.Player[][] getBoard() {
      return this.board;
   }

   @Generated
   public GobangGame.Player getCurrentPlayer() {
      return this.currentPlayer;
   }

   @Generated
   public boolean isGameOver() {
      return this.gameOver;
   }

   @Generated
   public GobangGame.Player getWinner() {
      return this.winner;
   }

   @Generated
   public int getMoveCount() {
      return this.moveCount;
   }

   public static enum Player {
      BLACK,
      WHITE;
   }
}
