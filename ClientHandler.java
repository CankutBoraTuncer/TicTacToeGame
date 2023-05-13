import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

    public static ArrayList<Socket> clientSockets = new ArrayList<>();
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
        clientSockets.add(clientSocket);
        messageThread.start();

    }

    @Override
    public void run() {
        while (clientSocket.isConnected()) {
            try {
                if (game.getCurrentPlayerCount() == 2) {
                    sendPlayerData();
                    sendTurnInfoToAll();
                }
                while (true) {
                    if (game.isTurnOfPlayer(player)) {
                        System.out.printf("Waiting for %s's move\n", game.whoTurn().getName());
                        sendTurnInfoToAll();
                        while (!messageHandler.isNewValidMessage()) ;
                        String playerMessage = messageHandler.getLastValidMesssage();
                        String messageType = GTP.getMessageType(playerMessage);
                        if (messageType.equals(GTP.MESSAGE_TYPE_PLAYER_MOVE)) {
                            String playerMove = GTP.getMessageResponse(GTP.MESSAGE_PLAY, playerMessage);
                            if (game.isValidPlay(playerMove, player)) {
                                game.updateBoard(playerMove, player);
                                sendAcceptMoveMessage();
                                game.nextTurn();
                                if (!game.isGameOver()) {
                                    sendTurnInfoToAll();
                                } else {
                                    System.out.println("Game Over");
                                    sendGameWinnerToAll();
                                    return;
                                }
                            } else {
                                sendRejectMoveMessage(GTP.MESSAGE_IS_VALID_PLAY, GTP.NO);
                            }
                        } else {
                            sendRejectMoveMessage(GTP.MESSAGE_IS_TURN, GTP.NO);
                        }

                    }
                }
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

    public void sendTurnInfoToAll() {
        for (Socket socket : clientSockets) {
            try {
                gtp.clearMessage();
                gtp.addMessage(GTP.MESSAGE_ID, "0");
                gtp.addMessage(GTP.MESSAGE_TYPE, GTP.MESSAGE_TYPE_TURN_INFO);
                gtp.addMessage(GTP.MESSAGE_TURN_INFO, game.whoTurn().getName());
                gtp.addMessage(GTP.MESSAGE_BOARD, game.toString());
                gtp.sendMessage(socket);
            } catch (IOException e) {
                closeEverything();
            }
        }
    }

    public void sendGameWinnerToAll() {
        for (Socket socket : clientSockets) {
            try {
                gtp.clearMessage();
                gtp.addMessage(GTP.MESSAGE_ID, "0");
                gtp.addMessage(GTP.MESSAGE_TYPE, GTP.MESSAGE_TYPE_GAME_STATUS);
                gtp.addMessage(GTP.MESSAGE_GAME_STATUS, game.winner.getName());
                gtp.sendMessage(socket);
            } catch (IOException e) {
                closeEverything();
            }
        }
    }

    public void removeClientHandler() {
        clientSockets.remove(clientSocket);
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
