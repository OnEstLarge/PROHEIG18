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
            RSA = node.getTempRSAInfo();
            if(RSA != null) {
                RSA.sendEncryptedKey(node, message);
                node.setTempRSAInfo(null);
            }
        }
        else if (message.getType().equals(MessageType.DHS1)){
            try {
                RSA = new RSAHandler();
                RSA.setKeys();
                RSA.sendRSAPublicKey(node, RSA.getPublicKey(), message);
                node.setTempRSAInfo(RSA);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            }
        }
        else if(message.getType().equals(MessageType.DHS2)){
            node.setKey(node.getTempRSAInfo().getFinalKey(message));
            node.setTempRSAInfo(null);
            System.out.println("final key is : " + new String(node.getKey()));
        }
        else {
            node.getMapMessage().get(message.getType()).handleMessage(connection, message); //gerer erreur possible
        }

        //close connection
        connection.close();
    }

}
