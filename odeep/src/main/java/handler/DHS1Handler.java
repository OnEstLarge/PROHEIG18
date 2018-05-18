package handler;

import Node.Node;
import message.MessageHandler;
import org.bouncycastle.jcajce.provider.asymmetric.RSA;
import peer.PeerConnection;
import peer.PeerMessage;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class DHS1Handler implements MessageHandler {
    @Override
    public void handleMessage(Node n, PeerConnection c, PeerMessage m) {
        try {
            RSAHandler RSA = new RSAHandler();
            RSA.setKeys();
            RSA.sendRSAPublicKey(RSA.getPublicKey(), m, c);
            n.setTempRSAInfo(RSA);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
    }
}
