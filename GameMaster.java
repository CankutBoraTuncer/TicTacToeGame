import java.net.*;
import java.io.*;

public class GameMaster {

    public static final int MAX_PLAYER_COUNT = 2;
    private final ServerSocket serverSocket;
    private int playerCount;
    private Game game;

    public GameMaster(ServerSocket serverSocket) {
        this.playerCount = 0;
        this.serverSocket = serverSocket;
        this.game = new Game();
    }

    public void initServer() {
        try {
            while (!serverSocket.isClosed()) {
                if (playerCount < MAX_PLAYER_COUNT) {
                    System.out.println("Server is initialized");
                    Socket clientSocket1 = serverSocket.accept();
                    PlayerServer player1 = new PlayerServer(clientSocket1, 'X', "player1");
                    ClientHandler clientHandler1 = new ClientHandler(clientSocket1, player1, game);
                    System.out.println("Player 1 joined");
                    playerCount++;
                    Socket clientSocket2 = serverSocket.accept();
                    PlayerServer player2 = new PlayerServer(clientSocket2, '0', "player2");
                    ClientHandler clientHandler2 = new ClientHandler(clientSocket2, player2, game);
                    playerCount++;
                    System.out.println("Player 2 joined");
                    Thread t1 = new Thread(clientHandler1);
                    t1.start();
                    Thread t2 = new Thread(clientHandler2);
                    t2.start();
                }
            }
        } catch (IOException e) {
            closeServer();
        }
    }

    public void closeServer() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.getStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1235);
        GameMaster gameMaster = new GameMaster(serverSocket);
        gameMaster.initServer();
    }
}

