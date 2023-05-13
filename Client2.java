import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client2 {
    private Socket clientSocket;
    private PlayerClient player;
    private GTP gtp;

    public static void main(String[] args) throws IOException {
        Socket clientSocket = new Socket("localhost", 1235);
        System.out.println("Connected to the server.");
        Client2 client2 = new Client2(clientSocket);
        client2.listenServer();
        client2.sendMessage();
    }

    public Client2(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.gtp = new GTP(clientSocket);
        this.player = null;
    }

    public void sendMessage() {
        try {
            Scanner scanner = new Scanner(System.in);
            while(clientSocket.isConnected()){
                String play = scanner.nextLine();
                play = play.trim();
                sendPlayerMove(play);
            }
        } catch (IOException e) {
            closeEverything();
        }
    }

    public void listenServer(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String serverMessage;
                while(clientSocket.isConnected()){
                    try{
                        serverMessage = gtp.getMessage();
                        String messageType = GTP.getMessageType(serverMessage);
                        if(messageType.equals(GTP.MESSAGE_TYPE_PLAYER_INIT)){
                            String id = GTP.getMessageResponse(GTP.MESSAGE_PLAYER_ID, serverMessage);
                            char symbol = GTP.getMessageResponse(GTP.MESSAGE_SYMBOL, serverMessage).charAt(0);
                            String name = GTP.getMessageResponse(GTP.MESSAGE_NAME, serverMessage);
                            player = new PlayerClient(id, symbol, name);
                            System.out.printf("Retrieved symbol %c and ID=%s\n", symbol, id);
                        } else if(messageType.equals(GTP.MESSAGE_TYPE_TURN_INFO)){
                            String playerName = GTP.getMessageResponse(GTP.MESSAGE_TURN_INFO, serverMessage);
                            if(playerName.equals(player.getName())){
                                System.out.println("Turn information: Your turn!");
                                String board = GTP.getMessageResponse(GTP.MESSAGE_BOARD, serverMessage);
                                System.out.println("State of board\n" + board);
                                System.out.printf("Put %c to: ", player.getSymbol());
                            } else {
                                System.out.printf("Turn information: %s's turn!\n",playerName);
                                String board = GTP.getMessageResponse(GTP.MESSAGE_BOARD, serverMessage);
                                System.out.println("State of board\n" + board);
                            }
                        } else if(messageType.equals(GTP.MESSAGE_TYPE_PLAYER_MOVE_RESPONSE)){
                            boolean isValidPlay = Boolean.parseBoolean(GTP.getMessageResponse(GTP.MESSAGE_IS_VALID_PLAY, serverMessage));
                            if(!isValidPlay){
                                System.out.println("Server says: \"This is an illegal move. Please change your move!\"");
                            }
                        } else if(messageType.equals(GTP.MESSAGE_TYPE_GAME_STATUS)){
                            String winner = GTP.getMessageResponse(GTP.MESSAGE_GAME_STATUS, serverMessage);
                            System.out.println("The winner is " + winner);
                            return;
                        }
                    } catch (IOException e){
                        closeEverything();
                    }
                }
            }
        }).start();
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
        gtp.addMessage(GTP.MESSAGE_ID, player.getId());
        gtp.addMessage(GTP.MESSAGE_TYPE, GTP.MESSAGE_TYPE_PLAYER_MOVE);
        gtp.addMessage(GTP.MESSAGE_PLAY, play);
        gtp.sendMessage();
    }


}
