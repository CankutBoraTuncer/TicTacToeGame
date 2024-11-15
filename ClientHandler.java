import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

    public static ArrayList<Socket> sockets = new ArrayList<>();
    public static ArrayList<Player> players = new ArrayList<>();
    private Socket clientSocket;
    private Player player;
    private GTP gtp;
    private Game game;
    private MessageHandler messageHandler;


    public ClientHandler(Socket clientSocket, Player player, Game game) throws IOException {
        this.clientSocket = clientSocket;
        this.game = game;
        this.gtp = new GTP(clientSocket);
        this.player = player;
        this.game.setPlayer(player);
        this.messageHandler = new MessageHandler(gtp, clientSocket, game, player);
        Thread messageThread = new Thread(messageHandler);
        sockets.add(clientSocket);
        players.add(player);
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
                        while (!messageHandler.isNewValidMessage()) ;
                        String playerMessage = messageHandler.getLastValidMesssage();
                        String messageType = GTP.getMessageType(playerMessage);
                        if (messageType.equals(GTP.MESSAGE_TYPE_PLAYER_MOVE)) {
                            String playerMove = GTP.getMessageResponse(GTP.MESSAGE_PLAY, playerMessage);
                            if (game.isValidPlay(playerMove, player)) {
                                game.updateBoard(playerMove, player);
                                sendAcceptMoveMessage();
                                game.nextTurn();
                                String winner = game.isGameOver();
                                if (winner == null) {
                                    sendTurnInfoToAll();
                                } else {
                                    if (winner.equals("tie")) {
                                        System.out.println("Game ends with a tie");
                                    } else {
                                        System.out.println("The winner is " + winner);
                                    }
                                    sendGameWinnerToAll(winner);
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
        gtp.addMessage(GTP.SENDER_ID, "0");
        gtp.addMessage(GTP.RECEIVER_ID, player.getId());
        gtp.addMessage(GTP.MESSAGE_TYPE, GTP.MESSAGE_TYPE_PLAYER_INIT);
        gtp.addMessage(GTP.MESSAGE_PLAYER_ID, player.getId());
        gtp.addMessage(GTP.MESSAGE_SYMBOL, String.valueOf(player.getSymbol()));
        gtp.addMessage(GTP.MESSAGE_NAME, player.getName());
        gtp.sendMessage();
    }

    public void sendRejectMoveMessage(String reason, String desc) throws IOException {
        gtp.clearMessage();
        gtp.addMessage(GTP.SENDER_ID, "0");
        gtp.addMessage(GTP.RECEIVER_ID, player.getId());
        gtp.addMessage(GTP.MESSAGE_TYPE, GTP.MESSAGE_TYPE_PLAYER_MOVE_RESPONSE);
        gtp.addMessage(GTP.MESSAGE_IS_VALID_PLAY, GTP.NO);
        gtp.addMessage(reason, desc);
        gtp.sendMessage();
    }

    public void sendAcceptMoveMessage() throws IOException {
        gtp.clearMessage();
        gtp.addMessage(GTP.SENDER_ID, "0");
        gtp.addMessage(GTP.RECEIVER_ID, player.getId());
        gtp.addMessage(GTP.MESSAGE_TYPE, GTP.MESSAGE_TYPE_PLAYER_MOVE_RESPONSE);
        gtp.addMessage(GTP.MESSAGE_IS_VALID_PLAY, GTP.YES);
        gtp.sendMessage();
    }

    public void sendTurnInfoToAll() {
        for (int i = 0; i < sockets.size(); i++) {
            try {
                gtp.clearMessage();
                gtp.addMessage(GTP.SENDER_ID, "0");
                gtp.addMessage(GTP.RECEIVER_ID, players.get(i).getId());
                gtp.addMessage(GTP.MESSAGE_TYPE, GTP.MESSAGE_TYPE_TURN_INFO);
                gtp.addMessage(GTP.MESSAGE_TURN_INFO, game.whoTurn().getName());
                gtp.addMessage(GTP.MESSAGE_BOARD, game.toString());
                gtp.sendMessage(sockets.get(i));
            } catch (IOException e) {
                closeEverything();
            }
        }
    }

    public void sendGameWinnerToAll(String winner) {
        for (int i = 0; i < sockets.size(); i++) {
            try {
                gtp.clearMessage();
                gtp.addMessage(GTP.SENDER_ID, "0");
                gtp.addMessage(GTP.RECEIVER_ID, players.get(i).getId());
                gtp.addMessage(GTP.MESSAGE_TYPE, GTP.MESSAGE_TYPE_GAME_STATUS);
                gtp.addMessage(GTP.MESSAGE_GAME_STATUS, winner);
                gtp.sendMessage(sockets.get(i));
            } catch (IOException e) {
                closeEverything();
            }
        }
    }

    public void removeClientHandler() {
        System.out.println("Client exited");
        sockets.remove(clientSocket);
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
