package handler;

import message.MessageType;
import peer.PeerConnection;
import peer.PeerInformations;
import peer.PeerMessage;
import util.CipherUtil;
import Node.Node;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.*;
import java.security.spec.*;

public class RSAHandler {

    private KeyPair kp;
    private byte[] publicKey;

    public RSAHandler() {
    }

    public void setKeys() throws NoSuchAlgorithmException, NoSuchProviderException {
        kp = CipherUtil.GenerateRSAKey();
        publicKey = CipherUtil.publicKeyToByte(kp.getPublic());
    }

    public void sendRSAPublicKey(Node n, byte[] b, PeerMessage message) {
        String sender = CipherUtil.erasePadding(message.getIdFrom(), PeerMessage.PADDING_START);
        String receiver = CipherUtil.erasePadding(message.getIdTo(), PeerMessage.PADDING_START);
        String group = CipherUtil.erasePadding(message.getIdGroup(), PeerMessage.PADDING_START);

        PeerMessage response = new PeerMessage(MessageType.DHR1, group, receiver, sender, b);
        PeerInformations pi = null;
        for (PeerInformations p : n.getKnownPeers()) {
            if (p.getID().equals(sender)) {
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
    }

    public void sendEncryptedKey(Node n, PeerMessage messageReceved, String group) {
        String sender = CipherUtil.erasePadding(messageReceved.getIdFrom(), PeerMessage.PADDING_START);
        byte[] foreignKey = CipherUtil.erasePadding(messageReceved.getMessageContent(), PeerMessage.PADDING_START);

        byte[] encryptedKey = new byte[0];
        encryptedKey = CipherUtil.RSAEncrypt(CipherUtil.byteToPublicKey(foreignKey), n.getKey(group));
        PeerMessage message = new PeerMessage(MessageType.DHS2, messageReceved.getIdGroup(), messageReceved.getIdTo(), messageReceved.getIdFrom(), encryptedKey);

        PeerInformations pi = null;
        for (PeerInformations p : n.getKnownPeers()) {
            if (p.getID().equals(sender)) {
                pi = p;
                break;
            }
        }
        try {
            PeerConnection pc = new PeerConnection(pi);
            pc.sendMessage(message);
            pc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] getFinalKey(PeerMessage m) {
        byte[] message = CipherUtil.erasePadding(m.getMessageContent(), PeerMessage.PADDING_START);
        return CipherUtil.RSADecrypt(kp.getPrivate(), message);
    }

    public byte[] getPublicKey() {
        return publicKey;
    }
}
