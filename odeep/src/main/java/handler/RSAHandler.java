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

    public void sendRSAPublicKey(byte[] data, PeerMessage message, PeerConnection pc) {
        String sender = CipherUtil.erasePadding(message.getIdFrom(), PeerMessage.PADDING_START);
        String receiver = CipherUtil.erasePadding(message.getIdTo(), PeerMessage.PADDING_START);
        String group = CipherUtil.erasePadding(message.getIdGroup(), PeerMessage.PADDING_START);

        PeerMessage response = new PeerMessage(MessageType.DHR1, group, receiver, sender, data);
        pc.sendMessage(response);
        pc.close();
    }

    public void sendEncryptedKey(Node n, PeerMessage messageReceved, PeerConnection pc) {
        String sender = CipherUtil.erasePadding(messageReceved.getIdFrom(), PeerMessage.PADDING_START);
        byte[] foreignKey = CipherUtil.erasePadding(messageReceved.getMessageContent(), PeerMessage.PADDING_START);

        byte[] encryptedKey = new byte[0];
        encryptedKey = CipherUtil.RSAEncrypt(CipherUtil.byteToPublicKey(foreignKey), n.getKey(messageReceved.getIdGroup()));
        PeerMessage message = new PeerMessage(MessageType.DHS2, messageReceved.getIdGroup(), messageReceved.getIdTo(), messageReceved.getIdFrom(), encryptedKey);

        pc.sendMessage(message);
        pc.close();
    }

    public byte[] getFinalKey(PeerMessage m) {
        byte[] message = CipherUtil.erasePadding(m.getMessageContent(), PeerMessage.PADDING_START);
        return CipherUtil.RSADecrypt(kp.getPrivate(), message);
    }

    public byte[] getPublicKey() {
        return publicKey;
    }
}
