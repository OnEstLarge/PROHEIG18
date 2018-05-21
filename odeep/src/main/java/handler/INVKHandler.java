package handler;

/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : handler.INVKHandler.java
 Auteur(s)   :
 Date        :
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/

import javafx.application.Platform;
import main.Client;
import message.MessageHandler;
import message.MessageType;
import peer.PeerConnection;
import peer.PeerMessage;
import Node.Node;
import util.CipherUtil;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * Classe permettant le traitement d'un message de type INVK
 */
public class INVKHandler implements MessageHandler{

    @Override
    public void handleMessage(Node n, PeerConnection c, PeerMessage m) {
        final String groupId = m.getIdGroup();
        final String toSendTo = m.getIdFrom();

        //n.setKey(CipherUtil.generateKey(), groupId);

        RSAHandler RSA = new RSAHandler();
        RSA.setKeys();
        n.setTempRSAInfo(RSA);
        PeerMessage pm = new PeerMessage(MessageType.DHS1, groupId, Client.getUsername(), toSendTo, "".getBytes());
        c.sendMessage(pm);

    }
}