package handler;

import peer.PeerMessage;
import util.CipherUtil;

import java.security.*;

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
