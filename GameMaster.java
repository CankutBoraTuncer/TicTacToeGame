import java.net.*;
import java.io.*;

public class GameMaster {

    public static final int MAX_PLAYER_COUNT = 2;
    private final ServerSocket serverSocket;
    private int playerCount;
    private Game game;

    public GameMaster(ServerSocket serverSocket) {
        this.playerCount = 1;
        this.serverSocket = serverSocket;
        this.game = new Game();
    }

    public void initServer() {
        try {
            while (!serverSocket.isClosed()) {
                if (playerCount > MAX_PLAYER_COUNT) {
                    Socket clientSocket = serverSocket.accept();
                    PlayerServer player = new PlayerServer(clientSocket, 'X', "player" + playerCount);
                    playerCount++;
                    if(playerCount == 1){
                        game.setPlayer1(player);
                    } else {
                        game.setPlayer2(player);
                    }
                    ClientHandler clientHandler = new ClientHandler(clientSocket, player, game);
                    Thread thread = new Thread(clientHandler);
                    thread.start();
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

//    public static void createInitGTPMessage(Socket connectionSocket, Player player) throws IOException {
//        GTP gtpMessage = new GTP(connectionSocket);
//        gtpMessage.addMessage(GTP.MESSAGE_ID, player.getId());
//        gtpMessage.addMessage(GTP.MESSAGE_SYMBOL, String.valueOf(player.getSymbol()));
//        gtpMessage.sendMessage();
//    }
}

//class ClientHandler implements Runnable {
//    Game game;
//    Socket clientSocket;
//    PlayerServer player;
//    GTP gtpMessage;
//    private volatile boolean flag = false;
//    private Thread thread;
//
//    public void start() {
//        thread = new Thread(this);
//        thread.start();
//    }
//
//    public void stop() {
//        flag = true;
//    }
//
//    public ClientHandler(Socket socket, PlayerServer player, Game game) {
//        this.clientSocket = socket;
//        this.player = player;
//        this.game = game;
//        this.gtpMessage = new GTP(clientSocket);
//    }
//
//
//    public void run() {
//        while (!flag) {
//            while (!game.isGameOver()) {
//                try {
//                    System.out.println("Waiting for " + player.getName() + "'s move");
//                    String clientMessage = gtpMessage.getMessage();
//                    String clientID = GTP.getMessageResponse(GTP.MESSAGE_ID, clientMessage);
//                    if (game.isTurnOfPlayer(game.idToPlayer(clientID))) {
//
//                        sendInGameGTPMessage();
//                        String playerMove = GTP.getMessageResponse(GTP.MESSAGE_PLAY, gtpMessage.getMessage());
//                        while (!game.isValidPlay(playerMove, player)) {
//                            sendRejectMoveMessage();
//                            playerMove = GTP.getMessageResponse(GTP.MESSAGE_PLAY, gtpMessage.getMessage());
//                        }
//                        game.updateBoard(playerMove, player);
//                        System.out.println(game);
//                        sendAcceptMoveMessage();
//                        game.nextTurn();
//
//                    } else {
//                        sendRejectMoveMessage();
//                    }
//                    System.out.println("The winner is " + game.winner.getName());
//                    sendEndOfGameMessage();
//                } catch (IOException e) {
//                    e.getStackTrace();
//                }
//                System.out.println(Thread.currentThread().getName() + " Stopped");
//                return;
//            }
//        }
//    }
//
//    public void sendInGameGTPMessage() throws IOException {
//        gtpMessage.clearMessage();
//        gtpMessage.addMessage(GTP.MESSAGE_IS_TURN, "true");
//        gtpMessage.addMessage(GTP.MESSAGE_BOARD, game.toString());
//        gtpMessage.sendMessage();
//    }
//
//    public void sendRejectMoveMessage() throws IOException {
//        gtpMessage.clearMessage();
//        gtpMessage.addMessage(GTP.MESSAGE_IS_VALID_PLAY, "false");
//        gtpMessage.sendMessage();
//    }
//
//
//    public void sendAcceptMoveMessage() throws IOException {
//        gtpMessage.clearMessage();
//        gtpMessage.addMessage(GTP.MESSAGE_IS_VALID_PLAY, "true");
//        gtpMessage.sendMessage();
//    }
//
//    public void sendEndOfGameMessage() throws IOException {
//        gtpMessage.clearMessage();
//        gtpMessage.addMessage(GTP.MESSAGE_IS_GAME_OVER, "true");
//        gtpMessage.addMessage(GTP.MESSAGE_GAME_WINNER, game.winner.getName());
//        gtpMessage.sendMessage();
//    }
//}


//    boolean gameFlag = true;
//
//    ServerSocket welcomeSocket = new ServerSocket(1234);
//
//        while (gameFlag) {
//                Socket connectionSocket1 = welcomeSocket.accept();
//                PlayerServer player1 = new PlayerServer(connectionSocket1, 'X', "player1");
//                System.out.println(player1);
//                createInitGTPMessage(connectionSocket1, player1);
//
//                Socket connectionSocket2 = welcomeSocket.accept();
//                PlayerServer player2 = new PlayerServer(connectionSocket2, '0', "player2");
//                System.out.println(player2);
//                createInitGTPMessage(connectionSocket2, player2);
//
//                Game game = new Game(player1, player2);
//                System.out.println("The game is started");
//
//                ClientHandler t1 = new ClientHandler(connectionSocket1, player1, game);
//                t1.start();
//                ClientHandler t2 = new ClientHandler(connectionSocket2, player2, game);
//                t2.start();
//
//                while (!game.isGameOver()) ;
//                t1.stop();
//                t2.stop();
//                }

