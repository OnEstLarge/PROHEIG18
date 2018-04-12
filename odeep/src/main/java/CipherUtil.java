import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.google.common.primitives.Bytes;
import org.bouncycastle.jce.spec.ECParameterSpec;

import javax.crypto.KeyAgreement;
import java.security.*;
import java.util.Arrays;


public class CipherUtil {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private final static int HMAC_SIZE = 32;
    private final static int AES_KEY_SIZE = 16;
    /**
     * retourne la concatenation des data chiffrée et du HMAC
     * @param data données en claire
     * @param key clé symetrique AES de 128 bits
     * @param encrypt boolean déterminant s'il faut chiffrer ou déchiffrer
     * @return tableau de bytes contenant les données chiffrée
     * @throws InvalidCipherTextException
     */
    private static byte[] AESProcessing(byte[] data, byte[] key, boolean encrypt) throws InvalidCipherTextException {
        if(data == null || key == null){
            throw new NullPointerException();
        }
        if(key.length != AES_KEY_SIZE){
            throw new InvalidParameterException("Incorrect key length");
        }
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

    /**
     * Chiffre les donnàes passée en paramètre à l'aide d'AES-128
     * @param data données en claire
     * @param key clé symétrique de 128 bits
     * @return tableau de byte contenant les données chiffrées
     * @throws InvalidCipherTextException
     */
    public static byte[] AESEncrypt(byte[] data, byte[] key) throws InvalidCipherTextException {
        if(data == null || key == null){
            throw new NullPointerException();
        }
        if(key.length != AES_KEY_SIZE){
            throw new InvalidParameterException("Incorrect key length");
        }
        byte[][] keys = splitKey(key);
        byte[] cipherData = AESProcessing(data, keys[0], true);
        byte[] HMAC = generateHMAC(cipherData, keys[1]);
        return Bytes.concat(cipherData, HMAC);
    }

    /**
     * Déchiffre des données à l'aide d'AES-128
     * @param data données chiffrées
     * @param key clé symétrique de 128 bits
     * @return tableau de byte contenant les données déchiffrées
     * @throws InvalidCipherTextException
     */
    public static byte[] AESDEcrypt(byte[] data, byte[] key) throws InvalidCipherTextException {
        if(data == null || key == null){
            throw new NullPointerException();
        }
        if(key.length != AES_KEY_SIZE){
            throw new InvalidParameterException("Incorrect key length");
        }
        byte[][] keys = splitKey(key);
        if(!checkHMAC(data, keys[1])){
            throw new InvalidCipherTextException("HMAC failure");
        }
        byte[] rawData = new byte[data.length - HMAC_SIZE];
        for(int i = 0; i < rawData.length; i++){
            rawData[i] = data[i];
        }
        byte[] decipherData = AESProcessing(rawData, keys[0], false);
        return erasePadding(decipherData, 0);
    }

    /**
     *
     * @return
     */
    public static byte[] generateKey(){
        SecureRandom rand = new SecureRandom();
        byte[] key = new byte[32];
        rand.nextBytes(key);
        return key;
    }

    /**
     * authentifie des données chiffrées avec un HMAC généré à l'aide de la forme standard :
     * https://tools.ietf.org/html/rfc2104
     * La fonction de hashage utilisée est sha3
     * @param data donnée à authentifier
     * @param key clé d'authentification de 128 bits
     * @return tableau contenant les données authentifiées
     */
    public static byte[] generateHMAC(byte[] data, byte[] key) {
        if(data == null || key == null){
            throw new NullPointerException();
        }
        if(key.length != AES_KEY_SIZE){
            throw new InvalidParameterException("Incorrect key length");
        }
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

    /**
     *
     * @param data données à hasher
     * @return hash des données
     */
    public static byte[] generateSHA3Digest(byte[] data){
        if(data == null){
            throw new NullPointerException();
        }
        SHA3.DigestSHA3 hash = new SHA3.Digest256();
        hash.update(data);
        return hash.digest();
    }

    /**
     * effectue un XOR entre deux tableaux de byte, qui doivent être de taille identique
     * @param data1 premier tableau de byte
     * @param data2 deuxieme tableau de byte
     * @return tableau de byte contenant le xor des deux paramètres
     */
    public static byte[] XORByteArray(byte[] data1, byte[] data2){
        if(data1 == null || data2 == null){
            throw new NullPointerException();
        }
        if(data1.length != data2.length){
            throw new InvalidParameterException("Arrays must be the same size");
        }
        byte[] result = new byte[data1.length];
        for (int i = 0; i < data1.length; i++){
            result[i] = (byte)(data1[i] ^ data2[i]);
        }
        return result;
    }

    /**
     * sépare une clé de 256 bits en deux sous clé de 128 bits
     * @param key clé à séparer
     * @return tableau contenant deux clés
     */
    public static byte[][] splitKey(byte[] key){
        if(key == null){
            throw new NullPointerException();
        }
        if(key.length != 2 * AES_KEY_SIZE){
            throw new InvalidParameterException("Incorrect key length");
        }
        int keyLength = key.length/2;
        byte[][] keys = new byte[2][keyLength];
        for(int i = 0; i < keyLength; i++){
            keys[0][i] = key[i];
            keys[1][i] = key[i + keyLength];
        }
        return keys;
    }

    /**
     * vérifie la validité d'un HMAC à partir de données authentifiée
     * @param data données authentifiées
     * @param key clé d'authentification
     * @return true si le HMAC est valide, false sinon
     */
    public static boolean checkHMAC(byte[] data, byte[] key){
        if(data == null || key == null){
            throw new NullPointerException();
        }
        if(key.length != AES_KEY_SIZE){
            throw new InvalidParameterException("Incorrect key length");
        }
        int dataLength = data.length;
        byte[] HMAC = new byte[HMAC_SIZE];
        byte[] rawData = new byte[data.length - HMAC_SIZE];
        for(int i = 0; i < dataLength - HMAC_SIZE; i++){
            rawData[i] = data[i];
        }
        int index = 0;
        for(int i = dataLength - HMAC_SIZE; i < dataLength; i++){
            HMAC[index] = data[i];
            index++;
        }
        byte[] expectedHMAC = generateHMAC(rawData, key);
        return Arrays.equals(expectedHMAC, HMAC);
    }

    /**
     * Efface le padding à la fin des data
     * @param data
     * @param pad caractère de padding
     * @return un tableau contenant les données sans padding
     */
    public static byte[] erasePadding(byte[] data, int pad){
        if(data == null){
            throw new NullPointerException();
        }
        int paddingSize = 0;
        for(int i = data.length-1; i >= 0; i--){
            if(data[i] == pad){
                paddingSize++;
            }
            else{
                break;
            }
        }
        byte[] dataWithoutPadding = new byte[data.length - paddingSize];
        for(int i = 0; i < dataWithoutPadding.length; i++){
            dataWithoutPadding[i] = data[i];
        }
        return dataWithoutPadding;
    }

    /**
     * génére une pair clé publique/clé privée utilisable dans ECDH.
     * @return
     * @throws NoSuchAlgorithmException si l'algorithme n'est pas reconnue
     * @throws InvalidAlgorithmParameterException si la courbe elliptique n'est pas connue
     * @throws NoSuchProviderException
     */
    public static KeyPair GenerateECDHKeys() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchProviderException {
        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("curve25519");
        KeyPairGenerator g = KeyPairGenerator.getInstance("ECDH", "BC");
        g.initialize(ecSpec, new SecureRandom());
        KeyPair kp = g.generateKeyPair();
        return kp;
    }

    /**
     * Génére une clé secret de 256 à l'aide d'ECDH, de la clé publique du destinataire et de la clé privée d'un utilisateur
     * @param publicKey
     * @param privateKey
     * @return tableau contenant la clé générée
     * @throws InvalidKeyException
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     */
    public static byte[] doECDH(PublicKey publicKey, PrivateKey privateKey) throws InvalidKeyException, NoSuchProviderException, NoSuchAlgorithmException {
        if(publicKey == null || privateKey == null){
            throw new NullPointerException();
        }
        KeyAgreement ka = KeyAgreement.getInstance("ECDH", "BC");
        ka.init(privateKey);
        ka.doPhase(publicKey, true);

        byte[] secret = ka.generateSecret();
        return secret;
    }


}
