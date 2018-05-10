package handler;

import message.MessageHandler;
import peer.PeerConnection;
import peer.PeerMessage;
import util.DatabaseUtil;

public class UPDTHandler implements MessageHandler{
    @Override
    public void handleMessage(PeerConnection c, PeerMessage m) {
        DatabaseUtil.downloadJSON("./" + m.getIdGroup() + "/key", m.getIdGroup());
    }
}
