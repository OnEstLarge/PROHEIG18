package handler;


import message.MessageHandler;
import peer.PeerConnection;
import peer.PeerMessage;
import main.Client;
import util.InterfaceUtil;

public class NFILHandler implements MessageHandler {
    @Override
    public void handleMessage(PeerConnection c, PeerMessage m) {
        Client.downloadJSON(m.getIdGroup());
        InterfaceUtil.askFile(m.getIdGroup(), new String(m.getMessageContent()));
    }
}
