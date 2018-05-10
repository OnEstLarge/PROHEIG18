package handler;


import message.MessageHandler;
import peer.PeerConnection;
import peer.PeerMessage;
import util.DatabaseUtil;
import util.InterfaceUtil;

public class NFILHandler implements MessageHandler {
    @Override
    public void handleMessage(PeerConnection c, PeerMessage m) {
        DatabaseUtil.downloadJSON(m.getIdGroup());
        InterfaceUtil.askFile(m.getIdGroup(), new String(m.getMessageContent()));
    }
}
