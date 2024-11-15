import java.net.*;
import java.io.*;

public class TicTacToeServer {

    public static final int MAX_PLAYER_COUNT = 2;
    private final ServerSocket serverSocket;
    private int playerCount;
    private Game game;

    public TicTacToeServer(ServerSocket serverSocket) {
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
                    Player player1 = new Player(clientSocket1, 'X', "player1");
                    ClientHandler clientHandler1 = new ClientHandler(clientSocket1, player1, game);
                    System.out.printf("A client is connected, and it is assigned with the symbol %c and ID=%s\n", player1.getSymbol(), player1.getId());
                    playerCount++;
                    Socket clientSocket2 = serverSocket.accept();
                    Player player2 = new Player(clientSocket2, '0', "player2");
                    ClientHandler clientHandler2 = new ClientHandler(clientSocket2, player2, game);
                    playerCount++;
                    System.out.printf("A client is connected, and it is assigned with the symbol %c and ID=%s\n", player2.getSymbol(), player2.getId());
                    System.out.println("The game is started.");
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
        int PORT = Integer.parseInt(args[0]);
        ServerSocket serverSocket = new ServerSocket(PORT);
        TicTacToeServer ticTacToeServer = new TicTacToeServer(serverSocket);
        ticTacToeServer.initServer();
    }
}

