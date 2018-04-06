import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.google.common.primitives.Bytes;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.security.*;

import static javafx.scene.input.KeyCode.K;

public class CipherUtil {

    /**
     * retourne la concatenation des data chiffrée et du HMAC
     * @param data
     * @param key
     * @param encrypt
     * @return
     * @throws InvalidCipherTextException
     */
    private static byte[] AESProcessing(byte[] data, byte[] key, boolean encrypt) throws InvalidCipherTextException {
        Security.addProvider(new BouncyCastleProvider());

        BlockCipher AES = new AESEngine();
        CBCBlockCipher blockCipher = new CBCBlockCipher(AES);
        PaddedBufferedBlockCipher in = new PaddedBufferedBlockCipher(blockCipher, new PKCS7Padding());
        KeyParameter k = new KeyParameter(key);
        in.init(encrypt, k);
        byte[] out = new byte[in.getOutputSize(data.length)];
        int numberOfByte = in.processBytes(data, 0, data.length, out, 0);
        in.doFinal(out, numberOfByte);

        return out;
    }

    public static byte[] AESEncrypt(byte[] data, byte[] key) throws InvalidCipherTextException {
        byte[][] keys = splitKey(key);
        byte[] cipherData = AESProcessing(data, keys[0], true);
        byte[] HMAC = generateHMAC(cipherData, keys[1]);
        return Bytes.concat(cipherData, HMAC);
    }

    public static byte[] AESDEcrypt(byte[] data, byte[] key) throws InvalidCipherTextException {
        return AESProcessing(data, key, false);
    }

    public static byte[] generateKey(){
        SecureRandom rand = new SecureRandom();
        byte[] key = new byte[32];
        rand.nextBytes(key);
        return key;
    }

    public static byte[] generateHMAC(byte[] data, byte[] key) {
        byte[] ipad = new byte[16];
        byte[] opad = new byte[16];
        for(int i = 0; i < 16; i++){
            ipad[i] = 0x36;
            opad[i] = 0x5c;
        }
        byte[] keyOpad = XORByteArray(key, opad);
        byte[] keyIpad = XORByteArray(key, ipad);
        byte[] keyIpadMess = Bytes.concat(keyIpad, data);
        byte[] digestRight = generateSHA3Digest(keyIpadMess);
        byte[] keyOpadDigest = Bytes.concat(keyOpad, digestRight);
        byte[] digest = generateSHA3Digest(keyOpadDigest);
        return digest;
    }

    public static byte[] generateSHA3Digest(byte[] data){
        SHA3.DigestSHA3 hash = new SHA3.Digest256();
        hash.update(data);
        return hash.digest();
    }

    public static byte[] XORByteArray(byte[] data1, byte[] data2){
        byte[] result = new byte[data1.length];
        for (int i = 0; i < data1.length; i++){
            result[i] = (byte)(data1[i] ^ data2[i]);
        }
        return result;
    }

    public static byte[][] splitKey(byte[] key){
        int keyLength = key.length/2;
        byte[][] keys = new byte[2][keyLength];
        for(int i = 0; i < keyLength; i++){
            keys[0][i] = key[i];
            keys[1][i] = key[i + keyLength];
        }
        return keys;
    }

}
