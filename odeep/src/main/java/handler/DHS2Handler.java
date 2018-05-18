package handler;

import Node.FileSharingNode;
import message.MessageHandler;
import peer.PeerConnection;
import peer.PeerMessage;

public class DHS2Handler implements MessageHandler {
    @Override
    public void handleMessage(FileSharingNode n, PeerConnection c, PeerMessage m) {
        n.setKey(n.getTempRSAInfo().getFinalKey(m), m.getIdGroup());
        n.setTempRSAInfo(null);
        System.out.println("final key is : " + new String(n.getKey(m.getIdGroup())));
    }
}
