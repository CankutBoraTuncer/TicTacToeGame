import java.io.*;
import java.net.Socket;
import java.util.Scanner;

//Game Text Protocol
public class GTP {
    public static final String MESSAGE_SYMBOL = "symbol";
    public static final String MESSAGE_ID = "id";
    public static final String MESSAGE_PLAYER_ID = "idPlayer";
    public static final String MESSAGE_OTHER = "other";
    public static final String MESSAGE_NAME = "name";
    public static final String MESSAGE_TYPE = "type";
    public static final String MESSAGE_IS_TURN = "isTurn";
    public static final String MESSAGE_PLAY = "play";
    public static final String MESSAGE_GAME = "game";
    public static final String MESSAGE_IS_VALID_PLAY = "isValidPlay";
    public static final String MESSAGE_IS_GAME_OVER = "isGameOver";
    public static final String MESSAGE_GAME_WINNER = "winner";
    public static final String MESSAGE_BOARD = "board";

    String message;

    private Socket socket;

    public GTP(Socket socket) {
        this.socket = socket;
    }

    public void addMessage(String messageType, String messageBody) {
        message += messageType + ":" + messageBody + "\r\n";
    }

    public void sendMessage() throws IOException {
        byte[] messageBytes = message.getBytes();
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(messageBytes);
    }

    public String getMessage() throws IOException {
        InputStream inputStream = socket.getInputStream();
        byte[] buffer = new byte[1024];
        int bytesRead = inputStream.read(buffer);
        return new String(buffer, 0, bytesRead);
    }

    public void clearMessage() {
        message = "";
    }

    public static String getMessageResponse(String messageType, String message) {
        String[] messages = message.split("\r\n");
        for (String messagePart : messages) {
            String[] foo = messagePart.split(":");
            if (foo[0].equals(messageType)) {
                return foo[1];
            }
        }
        return null;
    }

    public static String getMessageType(String message) {
        String[] messages = message.split("\r\n");
        return messages[1].split(":")[1];
    }
}
