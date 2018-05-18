package handler;

import Node.Node;
import message.MessageHandler;
import message.MessageType;
import peer.PeerConnection;
import peer.PeerMessage;
import util.CipherUtil;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class DHS1Handler implements MessageHandler {
    @Override
    public void handleMessage(Node n, PeerConnection c, PeerMessage m) {
        try {
            RSAHandler RSA = new RSAHandler();
            RSA.setKeys();
            n.setTempRSAInfo(RSA);
            PeerMessage response = new PeerMessage(MessageType.DHR1, m.getIdGroup(), m.getIdTo(), m.getIdFrom(), RSA.getPublicKey());
            c.sendMessage(response);
            c.close();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
    }
}
