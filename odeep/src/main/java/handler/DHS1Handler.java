package handler;

/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : handler.DHS1Handler.java
 Auteur(s)   : Kopp Olivier
 Date        : 27.04.2018
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
 * Classe permettant le traitement d'un message de type DHS1
 */
public class DHS1Handler implements MessageHandler {
    @Override
    public void handleMessage(Node n, PeerConnection c, PeerMessage m) {
        //création d'info RSA stockant la pair de clé
        RSAInfo RSA = new RSAInfo();
        //génération de clé RSA
        RSA.setKeys();
        //stockage temporaire des infos RSA dans le node
        n.setTempRSAInfo(RSA);

        PeerMessage response = new PeerMessage(MessageType.DHR1, m.getIdGroup(), m.getIdTo(), m.getIdFrom(), RSA.getPublicKey());
        Client.sendPM(response);
    }
}
