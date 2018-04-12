import org.bouncycastle.crypto.InvalidCipherTextException;
import org.junit.jupiter.api.Test;
import util.CipherUtil;

import javax.crypto.KeyAgreement;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.security.*;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

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

        byte[] plain2 = CipherUtil.AESDEcrypt(cipher, keyByte);
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
        byte[] decipherText = CipherUtil.AESDEcrypt(cipherText, keyByte);
        assertTrue(Arrays.equals(plainText, decipherText));
    }

    @Test
    void generateKey() {
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
    void splitKey() {
    }

    @Test
    void doECDH() throws InvalidKeyException, NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPair keyA = CipherUtil.GenerateECDHKeys();
        KeyPair keyB = CipherUtil.GenerateECDHKeys();
        byte[] secretA = CipherUtil.doECDH(keyA.getPublic(), keyB.getPrivate());
        byte[] secretB = CipherUtil.doECDH(keyB.getPublic(), keyA.getPrivate());
        assertTrue(Arrays.equals(secretA,secretB));
        System.out.print("Secret : ");
        for(int i = 0; i < secretA.length; i++){
            System.out.format("%02x ", secretA[i]);
        }

    }

    }