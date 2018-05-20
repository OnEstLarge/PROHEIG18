package handler;

import message.MessageHandler;
import peer.PeerConnection;
import peer.PeerMessage;
import Node.Node;

public class DHS2Handler implements MessageHandler {
    @Override
    public void handleMessage(Node n, PeerConnection c, PeerMessage m) {
        n.setKey(n.getTempRSAInfo().getFinalKey(m), m.getIdGroup());
        n.setTempRSAInfo(null);
        System.out.println("final key is : " + new String(n.getKey(m.getIdGroup())));
    }
}
