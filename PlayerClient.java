import java.net.Socket;

public class PlayerClient extends Player{
    public PlayerClient(String id, char symbol, String name){
        super();
        this.symbol = symbol;
        this.id = id;
        this.name = name;
    }
}
