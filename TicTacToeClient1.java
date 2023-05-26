import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class TicTacToeClient1 {
    private Socket clientSocket;
    private Player player;
    private GTP gtp;
    boolean isTurn;
    boolean isFirstMessage;

    public static void main(String[] args) throws IOException {
        int PORT = 1235;//Integer.parseInt(args[0]);
        Socket clientSocket = new Socket("localhost", PORT);
        System.out.println("Connected to the server.");
        TicTacToeClient1 ticTacToeClient1 = new TicTacToeClient1(clientSocket);
        ticTacToeClient1.listenServer();
        ticTacToeClient1.sendMessage();
    }

    public TicTacToeClient1(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.gtp = new GTP(clientSocket);
        this.player = null;
        isTurn = false;
        isFirstMessage = true;
    }

    public void sendMessage() {
        try {
            Scanner scanner = new Scanner(System.in);
            while (clientSocket.isConnected()) {
                String play = scanner.nextLine();
                play = play.trim();
                if (play.equals("boardInfo")) {
                    sendBoardInfoRequest();
                } else if (play.equals("turnInfo")) {
                    sendTurnInfoRequest();
                } else {
                    sendPlayerMove(play);
                }

            }
        } catch (IOException e) {
            closeEverything();
        }
    }

    public void listenServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String serverMessage;
                while (clientSocket.isConnected()) {
                    try {
                        serverMessage = gtp.getMessage();
                        String senderID = GTP.getMessageResponse(GTP.SENDER_ID, serverMessage);
                        String receiverID = GTP.getMessageResponse(GTP.RECEIVER_ID, serverMessage);
                        if (isFirstMessage || (receiverID.equals(player.getId()) && senderID.equals("0"))) {
                            String messageType = GTP.getMessageType(serverMessage);
                            if (messageType.equals(GTP.MESSAGE_TYPE_PLAYER_INIT)) {
                                isFirstMessage = false;
                                String id = GTP.getMessageResponse(GTP.MESSAGE_PLAYER_ID, serverMessage);
                                char symbol = GTP.getMessageResponse(GTP.MESSAGE_SYMBOL, serverMessage).charAt(0);
                                String name = GTP.getMessageResponse(GTP.MESSAGE_NAME, serverMessage);
                                player = new Player(id, symbol, name);
                                System.out.printf("Retrieved symbol %c and ID=%s\n", symbol, id);
                            } else if (messageType.equals(GTP.MESSAGE_TYPE_TURN_INFO)) {
                                printTurnInfo(serverMessage);
                                printBoardInfo(serverMessage);
                            } else if (messageType.equals(GTP.MESSAGE_TYPE_PLAYER_MOVE_RESPONSE)) {
                                boolean isValidPlay = Boolean.parseBoolean(GTP.getMessageResponse(GTP.MESSAGE_IS_VALID_PLAY, serverMessage));
                                if (!isValidPlay) {
                                    System.out.println("Server says: \"This is an illegal move. Please change your move!\"");
                                    if (isTurn) {
                                        System.out.printf("Put %c to: ", player.getSymbol());
                                    }
                                }
                            } else if (messageType.equals(GTP.MESSAGE_TYPE_TURN_INFO_REQUEST)) {
                                printTurnInfo(serverMessage);
                                if (isTurn) {
                                    System.out.printf("Put %c to: ", player.getSymbol());
                                }
                            } else if (messageType.equals(GTP.MESSAGE_TYPE_BOARD_INFO_REQUEST)) {
                                printBoardInfo(serverMessage);

                            } else if (messageType.equals(GTP.MESSAGE_TYPE_GAME_STATUS)) {
                                String winner = GTP.getMessageResponse(GTP.MESSAGE_GAME_STATUS, serverMessage);
                                if (winner.equals("tie")) {
                                    System.out.println("The game is ended with a tie");
                                } else {
                                    System.out.println("The winner is " + winner);
                                }
                                return;
                            }
                        }
                    } catch (IOException e) {
                        closeEverything();
                    }
                }
            }
        }).start();
    }

    private void printTurnInfo(String serverMessage) {
        String playerName = GTP.getMessageResponse(GTP.MESSAGE_TURN_INFO, serverMessage);
        if (playerName.equals(player.getName())) {
            isTurn = true;
            System.out.println("Turn information: Your turn!");
        } else {
            isTurn = false;
            System.out.printf("Turn information: %s's turn!\n", playerName);
        }
    }

    private void printBoardInfo(String serverMessage) {
        String board = GTP.getMessageResponse(GTP.MESSAGE_BOARD, serverMessage);
        System.out.println("State of board\n" + board);
        if (isTurn) {
            System.out.printf("Put %c to: ", player.getSymbol());
        }

    }


    private void closeEverything() {
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.getStackTrace();
        }
    }

    public void sendPlayerMove(String play) throws IOException {
        gtp.clearMessage();
        gtp.addMessage(GTP.SENDER_ID, player.getId());
        gtp.addMessage(GTP.RECEIVER_ID, "0");
        gtp.addMessage(GTP.MESSAGE_TYPE, GTP.MESSAGE_TYPE_PLAYER_MOVE);
        gtp.addMessage(GTP.MESSAGE_PLAY, play);
        gtp.sendMessage();
    }

    public void sendBoardInfoRequest() throws IOException {
        gtp.clearMessage();
        gtp.addMessage(GTP.SENDER_ID, player.getId());
        gtp.addMessage(GTP.RECEIVER_ID, "0");
        gtp.addMessage(GTP.MESSAGE_TYPE, GTP.MESSAGE_TYPE_BOARD_INFO_REQUEST);
        gtp.sendMessage();
    }

    public void sendTurnInfoRequest() throws IOException {
        gtp.clearMessage();
        gtp.addMessage(GTP.SENDER_ID, player.getId());
        gtp.addMessage(GTP.RECEIVER_ID, "0");
        gtp.addMessage(GTP.MESSAGE_TYPE, GTP.MESSAGE_TYPE_TURN_INFO_REQUEST);
        gtp.sendMessage();
    }


}
