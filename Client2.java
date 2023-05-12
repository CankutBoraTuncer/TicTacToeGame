import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client2 {
    private Socket clientSocket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private PlayerServer player;
    private GTP gtp;

    public static void main(String[] args) throws IOException {
        Socket clientSocket = new Socket("localhost", 1234);
        Client1 client1 = new Client1(clientSocket);
        client1.listenServer();
        client1.sendMessage();
    }

//    public Client2(Socket clientSocket) {
//        try {
//            this.clientSocket = clientSocket;
//            this.bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
//            this.gtp = new GTP(bufferedWriter, bufferedReader);
//            this.player = null;
//        } catch (IOException e) {
//            closeEverything();
//        }
//    }


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
                        System.out.println(serverMessage);
                    } catch (IOException e){
                        closeEverything();
                    }
                }
            }
        }).start();
    }

    private void closeEverything() {
        try {
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.getStackTrace();
        }
    }


}
