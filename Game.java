import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class Game {

    static final int BOARD_SIZE = 3;
    int turnNo;
    String boardStr;
    char[][] board;
    Player[] players;
    Player winner;
    int currentPlayerCount;


    public Game() {
        this.turnNo = 0;
        this.board = new char[][]{{'_', '_', '_'}, {'_', '_', '_'}, {'_', '_', '_'}};
        this.boardStr = "_|_|_\n_|_|_\n_|_|_";
        this.winner = null;
        this.currentPlayerCount = 0;
        this.players = new Player[2];
    }

    public void nextTurn() {
        turnNo++;
    }

    public boolean isTurnOfPlayer(Player player) {
        if (turnNo % 2 == 0) {
            return player == players[0];
        } else {
            return player == players[1];
        }
    }

    public Player whoTurn(){
        if (turnNo % 2 == 0) {
            return players[0];
        } else {
            return players[1];
        }
    }

    public void setPlayer(Player player) {
        players[currentPlayerCount] = player;
        currentPlayerCount++;
    }


    public Player idToPlayer(String id){
        if(players[0].getId().equals(id)){
            return players[0];
        } else if(players[1].getId().equals(id)){
            return players[1];
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
                if (players[0].getSymbol() == board[i][0]) {
                    winner = players[0];
                } else {
                    winner = players[1];
                }
                return true;
            }
        }
        // Check columns
        for (int j = 0; j < 3; j++) {
            if (board[0][j] != '_' && board[0][j] == board[1][j] && board[1][j] == board[2][j]) {
                if (players[0].getSymbol() == board[0][j]) {
                    winner = players[0];
                } else {
                    winner = players[1];
                }
                return true;
            }
        }
        // Check diagonals
        if (board[0][0] != '_' && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            if (players[0].getSymbol() == board[0][0]) {
                winner = players[0];
            } else {
                winner = players[1];
            }
            return true;
        }
        if (board[0][2] != '_' && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            if (players[0].getSymbol() == board[0][2]) {
                winner = players[0];
            } else {
                winner = players[1];
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

    public int getCurrentPlayerCount() {
        return currentPlayerCount;
    }
}
