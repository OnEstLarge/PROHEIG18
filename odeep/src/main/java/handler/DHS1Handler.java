package handler;

import Node.FileSharingNode;
import message.MessageHandler;
import message.MessageType;
import peer.PeerConnection;
import peer.PeerInformations;
import peer.PeerMessage;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class DHS1Handler implements MessageHandler {
    @Override
    public void handleMessage(FileSharingNode n, PeerConnection c, PeerMessage m) {
        try {
            RSAHandler RSA = new RSAHandler();
            RSA.setKeys();
            n.setTempRSAInfo(RSA);

            PeerMessage response = new PeerMessage(MessageType.DHR1, m.getIdGroup(), m.getIdTo(), m.getIdFrom(), RSA.getPublicKey());
            PeerInformations pi = null;
            for (PeerInformations p : n.getKnownPeers()) {
                if (p.getID().equals(m.getIdFrom())) {
                    pi = p;
                    break;
                }
            }
            if (pi == null) {
                throw new NullPointerException();
            } else {
                try {
                    PeerConnection p = new PeerConnection(pi);
                    p.sendMessage(response);
                    p.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
    }
}
