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

    public byte[] getFinalKey(PeerMessage m) {
        byte[] message = CipherUtil.erasePadding(m.getMessageContent(), PeerMessage.PADDING_START);
        return CipherUtil.RSADecrypt(kp.getPrivate(), message);
    }

    public byte[] getPublicKey() {
        return publicKey;
    }
}
