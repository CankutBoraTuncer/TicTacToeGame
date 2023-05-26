import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class MessageHandler implements Runnable {

    private GTP gtp;
    private Socket socket;
    private Game game;
    private Player player;
    private ArrayList<String> allClientMessages;
    private ArrayList<String> validClientMessages;
    private int lastMessageIndex = -1;
    private volatile boolean newValidMessage;


    public MessageHandler(GTP gtp, Socket socket, Game game, Player player) {
        this.gtp = gtp;
        this.socket = socket;
        this.game = game;
        this.player = player;
        this.allClientMessages = new ArrayList<>();
        this.validClientMessages = new ArrayList<>();

    }

    @Override
    public void run() {
        while (socket.isConnected()) {
            try {
                String newMessage = gtp.getMessage();
                String senderID = GTP.getMessageResponse(GTP.SENDER_ID, newMessage);
                String receiverID = GTP.getMessageResponse(GTP.RECEIVER_ID, newMessage);
                if (senderID.equals(player.getId()) && receiverID.equals("0")) {
                    allClientMessages.add(newMessage);
                    String messageType = GTP.getMessageType(newMessage);
                    if (messageType.equals(GTP.MESSAGE_TYPE_TURN_INFO_REQUEST)) {
                        sendTurnInfo();
                    } else if (messageType.equals(GTP.MESSAGE_TYPE_BOARD_INFO_REQUEST)) {
                        sendBoardInfo();
                    } else if (game.isGameOver() == null && game.isTurnOfPlayer(player)) {
                        validClientMessages.add(newMessage);
                        newValidMessage = true;
                    } else {
                        sendRejectMoveMessage(GTP.MESSAGE_IS_TURN, GTP.NO);
                    }
                    lastMessageIndex++;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public ArrayList<String> getAllClientMessages() {
        newValidMessage = false;
        return allClientMessages;
    }

    public ArrayList<String> getValidClientMessages() {
        newValidMessage = false;
        return validClientMessages;
    }

    public void clearAllClientMessages() {
        allClientMessages.clear();
    }

    public void clearValidClientMessages() {
        validClientMessages.clear();
    }

    public String getLastValidMesssage() {
        newValidMessage = false;
        return validClientMessages.get(validClientMessages.size() - 1);
    }

    public boolean isNewValidMessage() {
        return newValidMessage;
    }

    public void sendRejectMoveMessage(String reason, String desc) throws IOException {
        gtp.clearMessage();
        gtp.addMessage(GTP.SENDER_ID, "0");
        gtp.addMessage(GTP.RECEIVER_ID, player.getId());
        gtp.addMessage(GTP.MESSAGE_TYPE, GTP.MESSAGE_TYPE_PLAYER_MOVE_RESPONSE);
        gtp.addMessage(GTP.MESSAGE_IS_VALID_PLAY, GTP.NO);
        gtp.addMessage(reason, desc);
        gtp.sendMessage();
    }

    public void sendTurnInfo() throws IOException {
        gtp.clearMessage();
        gtp.addMessage(GTP.SENDER_ID, "0");
        gtp.addMessage(GTP.RECEIVER_ID, player.getId());
        gtp.addMessage(GTP.MESSAGE_TYPE, GTP.MESSAGE_TYPE_TURN_INFO_REQUEST);
        gtp.addMessage(GTP.MESSAGE_TURN_INFO, game.whoTurn().getName());
        gtp.sendMessage();
    }

    public void sendBoardInfo() throws IOException {
        gtp.clearMessage();
        gtp.addMessage(GTP.SENDER_ID, "0");
        gtp.addMessage(GTP.RECEIVER_ID, player.getId());
        gtp.addMessage(GTP.MESSAGE_TYPE, GTP.MESSAGE_TYPE_BOARD_INFO_REQUEST);
        gtp.addMessage(GTP.MESSAGE_BOARD, game.toString());
        gtp.sendMessage();
    }
}
