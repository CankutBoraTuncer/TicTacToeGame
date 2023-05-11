import java.net.Socket;

public class PlayerServer extends Player{
    Socket clientSocket;
    public PlayerServer(Socket clientSocket, char symbol, String name){
        super();
        this.clientSocket = clientSocket;
        this.symbol = symbol;
        this.id = generateClientID();
        this.name = name;
    }

    private String generateClientID(){
        int id = 0;
        String clientAddr = clientSocket.getRemoteSocketAddress().toString();
        for(int i = 0; i < clientAddr.length(); i++){
            id += clientAddr.charAt(i);
        }
        return String.valueOf(id);
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

}
