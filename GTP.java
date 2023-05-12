import java.io.*;
import java.net.Socket;
import java.util.Scanner;

//Game Text Protocol
public class GTP extends GTPMessages{
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
        for (String messagePart : messages) {
            String[] foo = messagePart.split(":");
            if(foo[0].equals(GTP.MESSAGE_TYPE)){
                return foo[1];
            }
        }
        return null;
    }
}
