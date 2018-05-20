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
        byte[] message = m.getMessageContent();
        return CipherUtil.RSADecrypt(kp.getPrivate(), message);
    }

    public byte[] getPublicKey() {
        return publicKey;
    }
}
