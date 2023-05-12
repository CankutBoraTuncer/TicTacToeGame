import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket clientSocket;
    private PlayerServer player;
    private GTP gtp;
    private Game game;


    public ClientHandler(Socket clientSocket, PlayerServer player, Game game){
        try{
            this.clientSocket = clientSocket;
            this.game = game;
            this.gtp = new GTP(clientSocket);
            this.player = player;
            clientHandlers.add(this);
            sendPlayerData();
            sendBroadcast("A new player is connected!");
        } catch (IOException e){
            closeEverything();
        }
    }

    @Override
    public void run() {
        String playerMessage;
        String messageID;
        Player p;
        while(clientSocket.isConnected()){
            try{
                playerMessage = gtp.getMessage();
                messageID = GTP.getMessageResponse(GTP.MESSAGE_ID, playerMessage);
                p = game.idToPlayer(messageID);
                if(game.isTurnOfPlayer(p)){

                } else {

                }
            } catch(IOException e){
                closeEverything();
                break;
            }
        }
    }

    public void sendPlayerData() throws IOException {
        gtp.clearMessage();
        gtp.addMessage(GTP.MESSAGE_ID, "0");
        gtp.addMessage(GTP.MESSAGE_TYPE, "playerInit");
        gtp.addMessage(GTP.MESSAGE_PLAYER_ID, player.getId());
        gtp.addMessage(GTP.MESSAGE_SYMBOL, String.valueOf(player.getSymbol()));
        gtp.addMessage(GTP.MESSAGE_NAME, player.getName());
        gtp.sendMessage();
    }

    public void sendBroadcast(String s) {
        for(ClientHandler clientHandler : clientHandlers){
            try{
                if(clientHandler != this){
                    gtp.clearMessage();
                    gtp.addMessage(GTP.MESSAGE_ID, player.getId());
                    gtp.addMessage(GTP.MESSAGE_TYPE, "broadcast");
                    gtp.addMessage(GTP.MESSAGE_OTHER, s);
                    gtp.sendMessage();
                }
            } catch (IOException e){
                closeEverything();
            }
        }
    }

    public void removeClientHandler(){
        clientHandlers.remove(this);
        sendBroadcast("The player1 is left the server...");
    }
    private void closeEverything() {
        removeClientHandler();
        try{
            if(clientSocket != null){
                clientSocket.close();
            }
        } catch (IOException e){
            e.getStackTrace();
        }
    }

}
