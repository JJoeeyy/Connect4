package ch.bbw.m411.connect4;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Plays a game of Connect Four on a 4x7 board (a variation of the original 6x7 board).
 * The pieces fall straight down, occupying the lowest available space within the column.
 */
public class Connect4ArenaMain {

    static final int WIDTH = 7;

    static final int HEIGHT = 4;

    static final int NOMOVE = -1;

    public static void main(String[] args) {
        new Connect4ArenaMain().play(new HumanPlayer(), new AlphaBetaPlayer());
    }

    static String toDebugString(Stone[] board) {
        var sb = new StringBuilder();
        for (int r = 0; r < HEIGHT; r++) {
            for (int c = 0; c < WIDTH; c++) {
                var value = board[r * WIDTH + c];
                sb.append(value == null ? "." : (value == Stone.RED ? "X" : "O"));
            }
            sb.append("-");
        }
        return sb.toString();
    }

    static boolean isWinning(Stone[] b, Stone color) {
        // @formatter:off

        for (int i = 0; i < 7; i++) {
            if (b[i] == color && b[i] == b[i + 7] && b[i + 7] == b[i + 14] && b[i + 14] == b[i + 21]) {
                return true;
            }
        }

        for (int i = 0; i < 4; i++) {
            if (b[i] == color && b[i] == b[i + 8] && b[i + 8] == b[i + 16] && b[i + 16] == b[i + 24]) {
                return true;
            }
        }

        for (int i = 3; i < 7; i++) {
            if (b[i] == color && b[i] == b[i + 6] && b[i + 6] == b[i + 12] && b[i + 12] == b[i + 18]) {
                return true;
            }
        }

        for (int i = 0; i < 22; i+=7) {
            for (int j = i; j < i+4; j++) {
                if (b[j] == color && b[j] == b[j+1] && b[j+1] == b[j+2] && b[j+2] == b[j+3] ){
                    return true;
                }
            }
        }
        return false;
        // @formatter:on
    }

    Connect4Player play(Connect4Player red, Connect4Player blue) {
        if (red == blue) {
            throw new IllegalStateException("must be different players (simply create two instances)");
        }
        var board = new Stone[WIDTH * HEIGHT];
        red.initialize(Arrays.copyOf(board, board.length), Stone.RED);
        blue.initialize(Arrays.copyOf(board, board.length), Stone.BLUE);
        var lastMove = NOMOVE;
        var currentPlayer = red;
        for (int round = 0; round < board.length; round++) {
            var currentColor = currentPlayer == red ? Stone.RED : Stone.BLUE;
            System.out.println(HumanPlayer.toPrettyString(board) + currentColor + " to play next...");
            lastMove = currentPlayer.play(lastMove);
            if (lastMove < 0 || lastMove >= WIDTH * HEIGHT ||
                    board[lastMove] != null && (lastMove < WIDTH || board[lastMove - WIDTH] != null)) {
                throw new IllegalStateException("cannot play to position " + lastMove + " @ " + toDebugString(board));
            }
            board[lastMove] = currentColor;
            if (isWinning(board, currentColor)) {
                System.out.println(
                        HumanPlayer.toPrettyString(board) + "...and the winner is: " + currentColor + " @ " + toDebugString(board));
                return currentPlayer;
            }
            currentPlayer = currentPlayer == red ? blue : red;
        }
        System.out.println(HumanPlayer.toPrettyString(board) + "...it's a DRAW @ " + toDebugString(board));
        return null; // null implies a draw
    }

    public enum Stone {
        RED, BLUE;

        public Stone opponent() {
            return this == RED ? BLUE : RED;
        }
    }

    public interface Connect4Player {

        /**
         * Called before the game starts and guaranteed to only be called once per livetime of the player.
         *
         * @param board       the starting board, usually an empty board.
         * @param colorToPlay the color of this player
         */
        void initialize(Stone[] board, Stone colorToPlay);

        /**
         * Perform a next move, will only be called if the Game is not over yet.
         * Each player has to keep an internal state of the 4x7 board, wher the 0-index is on the bottom row.
         * The index-layout looks as:
         * <pre>
         * 21 22 23 24 25 26 27
         * 14 15 16 17 18 19 20
         *  7  8  9 10 11 12 13
         *  0  1  2  3  4  5  6
         * </pre>
         *
         * @param opponendPlayed the last index where the opponent played to (in range 0 - width*height exclusive)
         *                       or -1 if this is the first move.
         * @return an index to play to (in range 0 - width*height exclusive)
         */
        int play(int opponendPlayed);
    }

    /**
     * An abstract helper class to keep track of a board (and whatever we or the opponent played).
     */
    public abstract static class DefaultPlayer implements Connect4Player {

        Stone[] board;

        Stone myColor;

        @Override
        public void initialize(Stone[] board, Stone colorToPlay) {
            this.board = board;
            myColor = colorToPlay;
        }

        @Override
        public int play(int opponendPlayed) {
            if (opponendPlayed != NOMOVE) {
                board[opponendPlayed] = myColor.opponent();
            }
            var playTo = play();
            board[playTo] = myColor;
            return playTo;
        }

        /**
         * Givent the current {@link #board}, find a suitable position-index to play to.
         *
         * @return the position to play to as defined by {@link Connect4Player#play(int)}.
         */
        abstract int play();

    }

    public static class HumanPlayer extends DefaultPlayer {

        static String toPrettyString(Stone[] board) {
            var sb = new StringBuilder();
            for (int r = HEIGHT - 1; r >= 0; r--) {
                for (int c = 0; c < WIDTH; c++) {
                    var index = r * WIDTH + c;
                    if (board[index] == null) {
                        if (index < WIDTH || board[index - WIDTH] != null) {
                            sb.append("\033[37m" + index + "\033[0m ");
                            if (index < 10) {
                                sb.append(" ");
                            }
                        } else {
                            sb.append("\033[37m.\033[0m  ");
                        }
                    } else if (board[index] == Stone.RED) {
                        sb.append("\033[1;31mX\033[0m  ");
                    } else {
                        sb.append("\033[1;34mO\033[0m  ");
                    }
                }
                sb.append("\n");
            }
            return sb.toString();
        }

        @Override
        int play() {
            System.out.println("where to to put the next " + myColor + "?");
            var scanner = new Scanner(System.in, StandardCharsets.UTF_8);
            return Integer.parseInt(scanner.nextLine());
        }

    }

    public static class GreedyPlayer extends DefaultPlayer {

        @Override
        int play() {
            for (int c = 0; c < WIDTH; c++) {
                for (int r = 0; r < HEIGHT; r++) {
                    var index = r * WIDTH + c;
                    if (board[index] == null) {
                        return index;
                    }
                }
            }
            throw new IllegalStateException("cannot play at all");
        }
    }

    public static class MinMaxPlayer extends DefaultPlayer {

        int bestMove;

        int initialFreeFields;

        int nodeCount;

        ArrayList<Integer> possibleMoves;

        public MinMaxPlayer(int depth) {
            initialFreeFields = depth;
        }

        // the default depth is 8
        public MinMaxPlayer() {
            initialFreeFields = 8;
        }

        @Override
        int play() {
            getScore(myColor, initialFreeFields);
            return bestMove;
        }

        private long getScore(Stone myColor, int depth) {
            // finds all possible move
            possibleMoves = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                for (int j = i; j < 27; j += 7) {
                    if (board[j] == null) {
                        possibleMoves.add(j);
                        break;
                    }
                }
            }

            nodeCount++;
            if (isWinning(board, myColor.opponent())) {
                return -10000;  // we already lost :(
            }
            if (possibleMoves.size() == 0) {
                return evaluate(myColor);  // it's a draw
            }

            if (depth == 0) {
                return evaluate(myColor);
            }


            long bestValue = -1000000;
            for (int i : possibleMoves) {
                if (board[i] == null) {
                    board[i] = myColor; // play a stone

                    var currentValue = -getScore(myColor.opponent(), depth - 1);

                    board[i] = null; // revert the last move
                    if (currentValue > bestValue) {
                        bestValue = currentValue;
                        if (depth == initialFreeFields) {
                            bestMove = i; // a bit of a hack: we have to return a position (not a score)
                        }
                    }
                }
            }
            return bestValue;
        }

        private int evaluate(Stone myColor) {
            // point system
            int[] points = {
                    3, 4, 6, 7, 6, 4, 3,
                    2, 4, 6, 7, 6, 4, 2,
                    2, 4, 6, 7, 6, 4, 2,
                    3, 4, 6, 7, 6, 4, 3};

            int totalPoints = 0;
            // adds the points together
            for (int i = 0; i < 28; i++) {
                if (board[i] == myColor) {
                    totalPoints += points[i];
                } else if (board[i] == myColor.opponent()) {
                    totalPoints -= points[i];
                }
            }
            return totalPoints;
        }
    }

    public static class AlphaBetaPlayer extends DefaultPlayer {

        int bestMove;

        int initialFreeFields;

        int nodeCount;

        ArrayList<Integer> possibleMoves;

        public AlphaBetaPlayer(int depth) {
            initialFreeFields = depth;
        }

        public AlphaBetaPlayer() {
            initialFreeFields = 10;
        }

        @Override
        int play() {
            // start of a move
            long startTime = System.currentTimeMillis();

            alphabeta(board, myColor, initialFreeFields, -100000, 100000);

            // end of a move
            long endTime = System.currentTimeMillis();
            // the time the move had to finish
            long time = endTime - startTime;
            // prints how long the move was in milliseconds
            System.out.println("Time in milliseconds: " + time);

            return bestMove;
        }

        private long alphabeta(Stone[] board, Stone myColor, int depth, long alpha, long beta) {
            // finds all possible moves
            possibleMoves = new ArrayList<>();
            int row = 3;
            for (int i = 0; i < 7; i++) {
                // with this little hack it looks from the inside out
                // now it makes the alphaBeta much faster
                if (i % 2 == 0) {
                    row += i;
                } else {
                    row -= i;
                }
                for (int j = row; j < 28; j += 7) {
                    if (board[j] == null) {
                        possibleMoves.add(j);
                        break;
                    }
                }
            }

            nodeCount++;

            // check if opponent wins the game
            if (isWinning(board, myColor.opponent())) {
                return -10000;  // we already lost :(
            }

            // evaluate if depth is zero or there are no more possible moves
            if (depth == 0 || possibleMoves.size() == 0) {
                return evaluate(myColor, board);
            }

            long maxWert = alpha;

            // iterates throw all possible moves
            for (int i : possibleMoves) {
                board[i] = myColor; // play a stone

                var currentValue = -alphabeta(board, myColor.opponent(), depth - 1, -beta, -maxWert);

                board[i] = null; // revert the last move

                if (currentValue > maxWert) {
                    maxWert = currentValue;
                    if (depth == initialFreeFields) {
                        bestMove = i; // a bit of a hack: we have to return a position (not a score)
                    }
                }
                if (maxWert >= beta) {
                    break; // alphabeta pruning
                }
            }
            return maxWert;
        }

        private int evaluate(Stone myColor, Stone[] board) {
            // point system
            int[] points = {
                    3, 4, 6, 7, 6, 4, 3,
                    2, 4, 6, 7, 6, 4, 2,
                    2, 4, 6, 7, 6, 4, 2,
                    3, 4, 6, 7, 6, 4, 3};

            int totalPoints = 0;

            // add the points together
            for (int i = 0; i < 28; i++) {
                if (board[i] == myColor) {
                    totalPoints += points[i];
                } else if (board[i] == myColor.opponent()) {
                    totalPoints -= points[i];
                }
            }
            return totalPoints;
        }
    }
}
