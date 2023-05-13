import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket clientSocket;
    private PlayerServer player;
    private GTP gtp;
    private Game game;
    private MessageHandler messageHandler;


    public ClientHandler(Socket clientSocket, PlayerServer player, Game game) throws IOException {
        this.clientSocket = clientSocket;
        this.game = game;
        this.gtp = new GTP(clientSocket);
        this.player = player;
        this.game.setPlayer(player);
        this.messageHandler = new MessageHandler(gtp, clientSocket, game, player);
        Thread messageThread = new Thread(messageHandler);
        clientHandlers.add(this);
        messageThread.start();

    }

    @Override
    public void run() {
        while (clientSocket.isConnected()) {
            try {
                if (game.getCurrentPlayerCount() == 2) {

                    sendPlayerData();
                    sendBroadcast("Turn of " + game.whoTurn().getName());
                }
                while (!game.isGameOver()) {
                    if (game.isTurnOfPlayer(player)) {
                        System.out.printf("Waiting for %s's move\n", game.whoTurn().getName());
                        sendInGameGTPMessage();
                        while (!messageHandler.isNewValidMessage()) ;
                        String playerMessage = messageHandler.getLastValidMesssage();
                        String messageType = GTP.getMessageType(playerMessage);
                        if (messageType.equals(GTP.MESSAGE_TYPE_PLAYER_MOVE)) {
                            String playerMove = GTP.getMessageResponse(GTP.MESSAGE_PLAY, playerMessage);
                            if (game.isValidPlay(playerMove, player)) {
                                game.updateBoard(playerMove, player);
                                sendAcceptMoveMessage();
                                game.nextTurn();
                            } else {
                                sendRejectMoveMessage(GTP.MESSAGE_IS_VALID_PLAY, GTP.NO);
                            }
                        } else {
                            sendRejectMoveMessage(GTP.MESSAGE_IS_TURN, GTP.NO);
                        }

                    }
                }
                sendBroadcast("Game is over");
                return;
            } catch (IOException e) {
                closeEverything();
                break;
            }
        }
    }

    public void sendPlayerData() throws IOException {
        gtp.clearMessage();
        gtp.addMessage(GTP.MESSAGE_ID, "0");
        gtp.addMessage(GTP.MESSAGE_TYPE, GTP.MESSAGE_TYPE_PLAYER_INIT);
        gtp.addMessage(GTP.MESSAGE_PLAYER_ID, player.getId());
        gtp.addMessage(GTP.MESSAGE_SYMBOL, String.valueOf(player.getSymbol()));
        gtp.addMessage(GTP.MESSAGE_NAME, player.getName());
        gtp.sendMessage();
    }

    public void sendRejectMoveMessage(String reason, String desc) throws IOException {
        gtp.clearMessage();
        gtp.addMessage(GTP.MESSAGE_ID, "0");
        gtp.addMessage(GTP.MESSAGE_TYPE, GTP.MESSAGE_TYPE_PLAYER_MOVE_RESPONSE);
        gtp.addMessage(GTP.MESSAGE_IS_VALID_PLAY, GTP.NO);
        gtp.addMessage(reason, desc);
        gtp.sendMessage();
    }

    public void sendAcceptMoveMessage() throws IOException {
        gtp.clearMessage();
        gtp.addMessage(GTP.MESSAGE_ID, "0");
        gtp.addMessage(GTP.MESSAGE_TYPE, GTP.MESSAGE_TYPE_PLAYER_MOVE_RESPONSE);
        gtp.addMessage(GTP.MESSAGE_IS_VALID_PLAY, GTP.YES);
        gtp.sendMessage();
    }

    public void sendInGameGTPMessage() throws IOException {
        gtp.clearMessage();
        gtp.addMessage(GTP.MESSAGE_ID, "0");
        gtp.addMessage(GTP.MESSAGE_TYPE, GTP.MESSAGE_TYPE_MOVE_REQUEST);
        gtp.addMessage(GTP.MESSAGE_IS_TURN, GTP.YES);
        gtp.addMessage(GTP.MESSAGE_BOARD, game.toString());
        gtp.sendMessage();
    }

    public void sendBroadcast(String s) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (clientHandler != this) {
                    gtp.clearMessage();
                    gtp.addMessage(GTP.MESSAGE_ID, "0");
                    gtp.addMessage(GTP.MESSAGE_TYPE, GTP.MESSAGE_TYPE_BROADCAST);
                    gtp.addMessage(GTP.MESSAGE_OTHER, s);
                    gtp.sendMessage();
                }
            } catch (IOException e) {
                closeEverything();
            }
        }
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        sendBroadcast("The player1 is left the server...");
    }

    private void closeEverything() {
        removeClientHandler();
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.getStackTrace();
        }
    }

}
