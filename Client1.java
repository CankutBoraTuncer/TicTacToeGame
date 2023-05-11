import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client1 {
    public static void main(String[] args) {
        try {
            Socket clientSocket = new Socket("localhost", 1234);
            GTP gtp = new GTP(clientSocket);
            String gameInitMessage = gtp.getMessage();
            PlayerClient player = new PlayerClient(GTP.getMessageResponse(GTP.MESSAGE_ID, gameInitMessage), GTP.getMessageResponse(GTP.MESSAGE_SYMBOL, gameInitMessage).charAt(0), GTP.getMessageResponse(GTP.MESSAGE_NAME, gameInitMessage));
            System.out.println(gameInitMessage);
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String serverMessage = gtp.getMessage();
                if(GTP.getMessageResponse(GTP.MESSAGE_IS_GAME_OVER, serverMessage) == null){
                    if(GTP.getMessageResponse(GTP.MESSAGE_IS_TURN, serverMessage).equals("true")){
                        String board = GTP.getMessageResponse(GTP.MESSAGE_BOARD, serverMessage);
                        do{
                            System.out.println("State of board\n"+board);
                            System.out.print("Where do you want to play: ");
                            String play = scanner.nextLine();
                            sendPlayInfoGTPMessage(gtp, player.getId(), play);
                            serverMessage = GTP.getMessageResponse(GTP.MESSAGE_IS_VALID_PLAY, gtp.getMessage());
                        } while(serverMessage.equals("false"));
                    }
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendPlayInfoGTPMessage(GTP gtpMessage, String id, String play) throws IOException {
        gtpMessage.clearMessage();
        gtpMessage.addMessage(GTP.MESSAGE_ID, id);
        gtpMessage.addMessage(GTP.MESSAGE_PLAY, play);
        gtpMessage.sendMessage();
    }

}
