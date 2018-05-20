package handler;

import main.Client;
import message.MessageHandler;
import message.MessageType;
import peer.PeerConnection;
import peer.PeerInformations;
import peer.PeerMessage;
import util.CipherUtil;
import Node.Node;

import java.io.IOException;

public class DHR1Handler implements MessageHandler{
    @Override
    public void handleMessage(Node n, PeerConnection c, PeerMessage m) {
        RSAHandler RSA = n.getTempRSAInfo();
        if (RSA != null) {
            byte[] foreignKey = m.getMessageContent();
            byte[] encryptedKey = CipherUtil.RSAEncrypt(CipherUtil.byteToPublicKey(foreignKey), n.getKey(m.getIdGroup()));

            PeerMessage response = new PeerMessage(MessageType.DHS2, m.getIdGroup(), m.getIdTo(), m.getIdFrom(), encryptedKey);
            Client.sendPM(response);
           /* PeerInformations pi = null;
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
            }*/
            n.setTempRSAInfo(null);
        }
    }
}
