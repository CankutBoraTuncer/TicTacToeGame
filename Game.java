import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class Game {

    static final int BOARD_SIZE = 3;
    int turnNo;
    String boardStr;
    char[][] board;
    Player player1;
    Player player2;
    Player winner;


    public Game() {
        this.turnNo = 0;
        this.board = new char[][]{{'_', '_', '_'}, {'_', '_', '_'}, {'_', '_', '_'}};
        this.boardStr = "_|_|_\n_|_|_\n_|_|_";
        this.winner = null;
    }

    public void nextTurn() {
        turnNo++;
    }

    public boolean isTurnOfPlayer(Player player) {
        if (turnNo % 2 == 0) {
            return player == player1;
        } else {
            return player == player2;
        }
    }

    public void setPlayer1(Player player1) {
        this.player1 = player1;
    }

    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }

    public Player idToPlayer(String id){
        if(player1.getId().equals(id)){
            return player1;
        } else if(player2.getId().equals(id)){
            return player2;
        } else {
            return null;
        }
    }

    public boolean isValidPlay(String play, Player player) {
        int r = play.charAt(0) - '0';
        int c = play.charAt(2) - '0';
        if (r < 0 || r >= BOARD_SIZE || c < 0 || c >= BOARD_SIZE || board[r][c] != '_') {
            System.out.printf("Received %c on (%d,%d). It is a illegal move.\n", player.getSymbol(), r, c);
            return false;
        } else {
            System.out.printf("Received %c on (%d,%d). It is a legal move.\n", player.getSymbol(), r, c);
            return true;
        }

    }

    public void updateBoard(String play, Player player) {
        int r = play.charAt(0) - '0';
        int c = play.charAt(2) - '0';
        board[r][c] = player.getSymbol();
        boardStr = "\n";
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                boardStr += "|" + board[i][j];
            }
            boardStr += "|\n";
        }
    }

    public boolean isGameOver() {
        // Check rows
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != '_' && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                if (player1.getSymbol() == board[i][0]) {
                    winner = player1;
                } else {
                    winner = player2;
                }
                return true;
            }
        }
        // Check columns
        for (int j = 0; j < 3; j++) {
            if (board[0][j] != '_' && board[0][j] == board[1][j] && board[1][j] == board[2][j]) {
                if (player1.getSymbol() == board[0][j]) {
                    winner = player1;
                } else {
                    winner = player2;
                }
                return true;
            }
        }
        // Check diagonals
        if (board[0][0] != '_' && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            if (player1.getSymbol() == board[0][0]) {
                winner = player1;
            } else {
                winner = player2;
            }
            return true;
        }
        if (board[0][2] != '_' && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            if (player1.getSymbol() == board[0][2]) {
                winner = player1;
            } else {
                winner = player2;
            }
            return true;
        }

        // Check if all cells are filled
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == '_') {
                    return false;
                }
            }
        }
        // If none of the above conditions are true, the game is not over
        return false;
    }

    @Override
    public String toString() {
        return boardStr;
    }
}
