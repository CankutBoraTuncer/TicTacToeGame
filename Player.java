import java.net.Socket;

public class Player {
    char symbol;
    String id;
    String name;

    Socket clientSocket;

    public Player(String id, char symbol, String name){
        this.symbol = symbol;
        this.id = id;
        this.name = name;
    }

    public Player(Socket clientSocket, char symbol, String name){
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

    @Override
    public String toString(){
        return "A client is connected, and it is assigned with the symbol " + symbol + " and ID=" + id;
    }

    public char getSymbol() {
        return symbol;
    }
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }


}
