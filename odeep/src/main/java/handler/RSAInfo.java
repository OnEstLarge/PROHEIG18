package handler;

/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : handler.RSAInfo.java
 Auteur(s)   : Kopp Olivier
 Date        : 27.04.2018
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/

import peer.PeerMessage;
import util.CipherUtil;

import java.security.*;

public class RSAInfo {

    private KeyPair kp;
    private byte[] publicKey;

    public RSAInfo() {
    }

    public void setKeys() {
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
