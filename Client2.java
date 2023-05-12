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
                        System.out.println(serverMessage);
                        String messageType = GTP.getMessageType(serverMessage);
                        if(messageType.equals(GTP.MESSAGE_TYPE_PLAYER_INIT)){
                            String id = GTP.getMessageResponse(GTP.MESSAGE_PLAYER_ID, serverMessage);
                            char symbol = GTP.getMessageResponse(GTP.MESSAGE_SYMBOL, serverMessage).charAt(0);
                            String name = GTP.getMessageResponse(GTP.MESSAGE_NAME, serverMessage);
                            player = new PlayerClient(id, symbol, name);
                            System.out.println(player);
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
