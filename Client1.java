import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client1 {
    private Socket clientSocket;
    private PlayerClient player;
    private GTP gtp;

    public static void main(String[] args) throws IOException {
        Socket clientSocket = new Socket("localhost", 1235);
        Client1 client1 = new Client1(clientSocket);
        client1.listenServer();
        client1.sendMessage();
    }

    public Client1(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.gtp = new GTP(clientSocket);
        this.player = null;
    }

    public void sendMessage() {
        try {
            Scanner scanner = new Scanner(System.in);
            while(clientSocket.isConnected()){
                String message = scanner.nextLine();
                gtp.clearMessage();
                gtp.addMessage(GTP.MESSAGE_OTHER, message);
                gtp.sendMessage();
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
                        if(messageType.equals("playerInit")){
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


}
