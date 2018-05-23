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

import main.Client;
import message.MessageHandler;
import message.MessageType;
import peer.PeerConnection;
import peer.PeerMessage;
import Node.Node;

/**
 * Classe permettant le traitement d'un message de type INVK
 */
public class INVKHandler implements MessageHandler{

    @Override
    public void handleMessage(Node n, PeerConnection c, PeerMessage m) {
        final String groupId = m.getIdGroup();
        final String toSendTo = m.getIdFrom();

        Client.getController().disableButtons();

        RSAInfo RSA = new RSAInfo();
        RSA.setKeys();
        n.setTempRSAInfo(RSA);
        PeerMessage pm = new PeerMessage(MessageType.DHS1, groupId, Client.getUsername(), toSendTo, "".getBytes());
        c.sendMessage(pm);

    }
}