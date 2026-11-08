package dev.southside.gui.minegame.Gobang;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GobangAI {
   private static final Random random = new Random();

   public static Point getBestMove(GobangGame game, GobangGame.Player aiPlayer) {
      GobangGame.Player[][] board = game.getBoard();
      int size = 15;
      GobangGame.Player humanPlayer = aiPlayer == GobangGame.Player.BLACK ? GobangGame.Player.WHITE : GobangGame.Player.BLACK;
      int maxScore = -1;
      List<Point> bestMoves = new ArrayList<>();
      boolean isEmpty = true;

      for (int y = 0; y < size; y++) {
         for (int x = 0; x < size; x++) {
            if (board[y][x] != null) {
               isEmpty = false;
            } else {
               int attackScore = evaluatePosition(board, x, y, aiPlayer);
               int defenseScore = evaluatePosition(board, x, y, humanPlayer);
               int totalScore = attackScore + (int)(defenseScore * 1.2);
               if (totalScore > maxScore) {
                  maxScore = totalScore;
                  bestMoves.clear();
                  bestMoves.add(new Point(x, y));
               } else if (totalScore == maxScore) {
                  bestMoves.add(new Point(x, y));
               }
            }
         }
      }

      if (isEmpty) {
         return new Point(size / 2, size / 2);
      } else {
         return bestMoves.isEmpty() ? new Point(size / 2, size / 2) : bestMoves.get(random.nextInt(bestMoves.size()));
      }
   }

   private static int evaluatePosition(GobangGame.Player[][] board, int x, int y, GobangGame.Player player) {
      int score = 0;
      score += evaluateDirection(board, x, y, 1, 0, player);
      score += evaluateDirection(board, x, y, 0, 1, player);
      score += evaluateDirection(board, x, y, 1, 1, player);
      return score + evaluateDirection(board, x, y, 1, -1, player);
   }

   private static int evaluateDirection(GobangGame.Player[][] board, int x, int y, int dx, int dy, GobangGame.Player player) {
      int count = 1;
      int emptyEnds = 0;
      int size = 15;
      int nx = x + dx;

      int ny;
      for (ny = y + dy; nx >= 0 && nx < size && ny >= 0 && ny < size && board[ny][nx] == player; ny += dy) {
         count++;
         nx += dx;
      }

      if (nx >= 0 && nx < size && ny >= 0 && ny < size && board[ny][nx] == null) {
         emptyEnds++;
      }

      nx = x - dx;

      for (ny = y - dy; nx >= 0 && nx < size && ny >= 0 && ny < size && board[ny][nx] == player; ny -= dy) {
         count++;
         nx -= dx;
      }

      if (nx >= 0 && nx < size && ny >= 0 && ny < size && board[ny][nx] == null) {
         emptyEnds++;
      }

      if (count >= 5) {
         return 100000;
      } else {
         if (count == 4) {
            if (emptyEnds == 2) {
               return 10000;
            }

            if (emptyEnds == 1) {
               return 1000;
            }
         }

         if (count == 3) {
            if (emptyEnds == 2) {
               return 1000;
            }

            if (emptyEnds == 1) {
               return 100;
            }
         }

         if (count == 2) {
            if (emptyEnds == 2) {
               return 100;
            }

            if (emptyEnds == 1) {
               return 10;
            }
         }

         if (count == 1) {
            if (emptyEnds == 2) {
               return 10;
            }

            if (emptyEnds == 1) {
               return 1;
            }
         }

         return 0;
      }
   }
}
