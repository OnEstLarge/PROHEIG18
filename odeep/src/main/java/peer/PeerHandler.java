package peer;
import Node.*;
import com.sun.media.sound.InvalidFormatException;
import handler.RSAHandler;
import org.bouncycastle.crypto.InvalidCipherTextException;
import message.*;/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : peer.PeerHandler.java
 Auteur(s)   : Schürch Loïc
 Date        : 15.03.2018
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/
import util.CipherUtil;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

public class PeerHandler implements Runnable {

    private Thread activity;
    Socket clientSocket;
    private Node node;
    private RSAHandler RSA;

    public PeerHandler(Node node, Socket socket) {
        this.node = node;
        clientSocket = socket;
        activity = new Thread(this);
        activity.start();
    }

    public void run() {
        PeerConnection connection = new PeerConnection(clientSocket);

        PeerMessage message = null;
        try {
            message = connection.receiveMessage();
        } catch(InvalidFormatException e) {}

        //handle message
        if(message.getType().equals(MessageType.DHR1)){
            try {
                RSA = node.getTempRSAInfo();
                if(RSA != null) {
                    RSA.sendEncryptedKey(node, message);
                    node.setTempRSAInfo(null);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (InvalidCipherTextException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            }
        }
        else if (message.getType().equals(MessageType.DHS1)){
            try {
                RSA = new RSAHandler();
                RSA.setKeys();
                RSA.sendRSAPublicKey(node, RSA.getPublicKey(), message);
                node.setTempRSAInfo(RSA);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            }
        }
        else if(message.getType().equals(MessageType.DHS2)){
            try {
                node.setKey(node.getTempRSAInfo().getFinalKey(message));
                node.setTempRSAInfo(null);
            } catch (InvalidCipherTextException e) {
                e.printStackTrace();
            } catch (InvalidFormatException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            }
            System.out.println("final key is : " + new String(node.getKey()));
        }
        else {
            node.getMapMessage().get(message.getType()).handleMessage(connection, message); //gerer erreur possible
        }

        //close connection
        connection.close();
    }

}
