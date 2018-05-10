package handler;
import message.*;
import peer.PeerConnection;
import peer.PeerMessage;

public class BYEHandler implements MessageHandler {
    @Override
    public void handleMessage(PeerConnection c, PeerMessage m) {
        c.sendMessage(new PeerMessage(MessageType.UPDT, m.getIdGroup(),m.getIdTo(),m.getIdTo(),new byte[]{}));
    }
}
