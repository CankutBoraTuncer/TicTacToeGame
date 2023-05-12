public class Player {
    char symbol;
    String id;
    String name;



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
