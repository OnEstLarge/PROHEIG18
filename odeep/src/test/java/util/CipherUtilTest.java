package util;

import com.sun.media.sound.InvalidFormatException;
import message.MessageType;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.Test;
import peer.PeerMessage;
import util.CipherUtil;

import javax.crypto.KeyAgreement;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : Util.CipherUtil.java
 Auteur(s)   : Kopp Olivier
 Date        : 05.04.2018
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/
class CipherUtilTest {

    @Test
    void AESEncrypt() throws InvalidCipherTextException, UnsupportedEncodingException {
        String s = "hello world";
        byte[] plain = s.getBytes();
        String key = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        byte[] keyByte = key.getBytes();
        assertEquals(32, keyByte.length);
        byte[] cipher = CipherUtil.AESEncrypt(plain, keyByte);
        assertFalse(cipher.equals(plain));
        for(int i = 0; i < cipher.length; i++){
            System.out.format("%02x ", cipher[i]);
        }
        System.out.println(" ");
        System.out.println("plain : " + new String(plain,"UTF-8"));
        System.out.println("cipher : " + new String(cipher,"UTF-8"));

        byte[] plain2 = CipherUtil.AESDecrypt(cipher, keyByte);
        System.out.println("cipher : " + new String(cipher,"UTF-8"));
        System.out.println("plain : " + new String(plain2,"UTF-8"));
    }

    @Test
    void AESDEcrypt() throws InvalidCipherTextException {
        String text = "hello world";
        byte[] plainText = text.getBytes();
        String key = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        byte[] keyByte = key.getBytes();
        assertEquals(32, keyByte.length);
        byte[] cipherText = CipherUtil.AESEncrypt(plainText, keyByte);
        byte[] decipherText = CipherUtil.AESDecrypt(cipherText, keyByte);
        System.out.println(new String(decipherText));
        assertTrue(Arrays.equals(plainText, decipherText));
    }

    @Test
    void generateKeyAlwaysDifferent() {

        byte[] k1 = CipherUtil.generateKey();
        byte[] k2 = CipherUtil.generateKey();

        assertEquals(k1.length, k2.length);
        assertFalse(Arrays.equals(k1, k2));
    }

    @Test
    void generateHMAC() {
    }

    @Test
    void generateSHA3Digest() {
        String s = "hello world";
        byte[] digest = CipherUtil.generateSHA3Digest(s.getBytes());
        //calculate on https://www.browserling.com/tools/sha3-hash
        byte[] expected = new byte[] {(byte)0x64,0x4b,(byte)0xcc,0x7e,0x56,0x43,0x73,0x04,0x09,(byte)0x99,(byte)0xaa,(byte)0xc8,(byte)0x9e,0x76,0x22,(byte)0xf3,(byte)0xca,0x71,(byte)0xfb,(byte)0xa1,(byte)0xd9,0x72,(byte)0xfd,(byte)0x94,(byte)0xa3,0x1c,0x3b,(byte)0xfb,(byte)0xf2,0x4e,0x39,0x38};
        assertTrue(Arrays.equals(digest,expected));
    }

    @Test
    void XORByteArray() {
    }

    @Test
    void splitKey() throws InvalidFormatException {

        byte[] key = CipherUtil.generateKey();
        String keyString = new String(key);
        byte[][] keySplit = CipherUtil.splitKey(key);

        byte[] newKey = new byte[key.length];

        int length = key.length/2;

        for (int i = 0; i < length ; i++) {

            newKey[i] = keySplit[0][i];
            newKey[i+length] = keySplit[1][i];
        }

        String newKeyString = new String(newKey);

        assertTrue(keyString.equals(newKeyString));
    }


    @Test
    void RSAEncryptAndDecrypt(){

        String m = "Hello tout le monde";
        byte[] mToByte = m.getBytes();
        KeyPair k = CipherUtil.GenerateRSAKey();

        byte[] cipherText = CipherUtil.RSAEncrypt(k.getPublic(), mToByte );
        byte[] plainText = CipherUtil.RSADecrypt(k.getPrivate(), cipherText);

        String plainTextString = new String(plainText);
        assertTrue(m.equals(plainTextString));
    }

}