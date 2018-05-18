package handler;

import Node.Node;
import message.MessageHandler;
import peer.PeerConnection;
import peer.PeerMessage;

public class DHR1Handler implements MessageHandler{
    @Override
    public void handleMessage(Node n, PeerConnection c, PeerMessage m) {
        RSAHandler RSA = n.getTempRSAInfo();
        if (RSA != null) {
            RSA.sendEncryptedKey(n, m, c);
            n.setTempRSAInfo(null);
        }
    }
}
