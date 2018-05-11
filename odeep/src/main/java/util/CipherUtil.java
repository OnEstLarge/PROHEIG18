package util;
/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : util.CipherUtil.java
 Auteur(s)   : Kopp Olivier
 Date        : 05.04.2018
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.google.common.primitives.Bytes;
import org.bouncycastle.util.encoders.Base64;

import javax.crypto.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;


public class CipherUtil {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public final static int HMAC_SIZE = 32;
    public final static int AES_KEY_SIZE = 16;
    public final static int RSA_KEY_SIZE = 4096;
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
    public static byte[] AESEncrypt(byte[] data, byte[] key) {
        if(data == null || key == null){
            throw new NullPointerException();
        }
        if(key.length != 2 * AES_KEY_SIZE){
            throw new InvalidParameterException("Incorrect key length");
        }
        byte[][] keys = splitKey(key);
        byte[] cipherData = new byte[0];
        try {
            cipherData = AESProcessing(data, keys[0], true);
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
        }
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
    public static byte[] AESDecrypt(byte[] data, byte[] key) throws InvalidCipherTextException {
        if(data == null || key == null){
            throw new NullPointerException();
        }
        if(key.length != 2 * AES_KEY_SIZE){
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
        int padSize = 0;
        int index = decipherData.length - 1;
        while(decipherData[index--] == 0){
            padSize++;
        }
        return Arrays.copyOfRange(decipherData, 0, decipherData.length - padSize);
    }

    /**
     * génére une clé de 256 bits
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
     * Efface tout les caracteres a la fin d'un tableau jusqu'a rencontrer le caractere de debut de padding
     * @param data
     * @param pad caractère de debut de padding
     * @return un tableau contenant les données sans padding
     */
    public static byte[] erasePadding(byte[] data, int pad){
        if(data == null){
            throw new NullPointerException();
        }
        int paddingSize = 0;
        for(int i = data.length-1; i >= 0; i--){
            paddingSize++;
            if(data[i] == pad){
                break;
            }
        }
        byte[] dataWithoutPadding = new byte[data.length - paddingSize];
        for(int i = 0; i < dataWithoutPadding.length; i++){
            dataWithoutPadding[i] = data[i];
        }
        return dataWithoutPadding;
    }

    public static String erasePadding(String s, int pad){
        if(s == null){
            throw new NullPointerException();
        }
        int paddingSize = 0;
        for(int i = s.length()-1; i >= 0; i--){
            paddingSize++;
            if(s.charAt(i) == pad){
                break;
            }
        }
        String dataWithoutPadding = s.substring(0, s.length()-paddingSize);
        return dataWithoutPadding;
    }

    /**
     * génére une pair clé publique/clé privée utilisable dans RSA 4096.
     * @return
     * @throws NoSuchAlgorithmException si l'algorithme n'est pas reconnue
     * @throws NoSuchProviderException si le fournisseur est inconnu
     */
    public static KeyPair GenerateRSAKey() {
        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance("RSA", "BC");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        kpg.initialize(RSA_KEY_SIZE, new SecureRandom());
        return kpg.generateKeyPair();
    }

    /**
     * permet de chiffrer un tableau de byte à l'aide de RSA
     * @param key clé publique du destinataire
     * @param plain tableau de byte à chiffrer
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    public static byte[] RSAEncrypt(PublicKey key, byte[] plain) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        byte[] cipherText = new byte[0];
        try {
            cipherText = cipher.doFinal(plain);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return cipherText;
    }

    /**
     * permet de dechiffrer un tableau de byte grace a RSA
     * @param key clé privée de l'utilisateur
     * @param cipherText donnée chiffrée
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    public static byte[] RSADecrypt(PrivateKey key, byte[] cipherText) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        byte[] dectyptedText = new byte[0];
        try {
            dectyptedText = cipher.doFinal(cipherText);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return dectyptedText;
    }

    /**
     * permet de convertir un tableau de byte en une clé publique RSA, utile lors de la reception de la clé par le réseau
     * @param b tableau de byte contenant la clé encodée
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static PublicKey byteToPublicKey(byte[] b) {
        String s = new String(b);
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        X509EncodedKeySpec eks = new X509EncodedKeySpec(Base64.decode(s));
        PublicKey publicKey = null;
        try {
            publicKey = keyFactory.generatePublic(eks);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return publicKey;
    }

    /**
     * permet de transformer une clé publique RSA en tableau de byte, permmettant par la suite l'envoi de cette clé
     * @param pk clé publique à convertir
     * @return
     */
    public static byte[] publicKeyToByte(PublicKey pk) {
        return Base64.encode(pk.getEncoded());
    }

}
