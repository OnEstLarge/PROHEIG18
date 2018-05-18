package handler;


import message.MessageHandler;
import peer.PeerConnection;
import peer.PeerMessage;
import main.Client;
import util.InterfaceUtil;
import Node.Node;

public class NFILHandler implements MessageHandler {
    @Override
    public void handleMessage(Node n, PeerConnection c, PeerMessage m) {
        Client.downloadJSON(m.getIdGroup());
        InterfaceUtil.askFile(m.getIdGroup(), new String(m.getMessageContent()));
    }
}
