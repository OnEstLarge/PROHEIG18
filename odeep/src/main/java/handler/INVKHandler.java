package handler;

/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : handler.DHR1Handler.java
 Auteur(s)   : Burgbacher Lionel, Jee Mathieu, Kopp Olivier, Piller Florent,
               Silvestri Romain, Schürch Loïc
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
 * Classe permettant le traitement d'un message de type INVK
 */
public class INVKHandler implements MessageHandler{

    @Override
    public void handleMessage(Node n, PeerConnection c, PeerMessage m) {
        //ce message est recu lorsqu'une personne a accepté une invitation
        final String groupId = m.getIdGroup();
        final String toSendTo = m.getIdFrom();

        //lorsqu'un invitation est en cours, on empeche tout action de l'utilisateur
        Client.getController().disableButtons();

        //initialisation des paramétres RSA
        RSAInfo RSA = new RSAInfo();
        RSA.setKeys();
        n.setTempRSAInfo(RSA);
        //envoie du premier message du protocole.
        PeerMessage pm = new PeerMessage(MessageType.DHS1, groupId, Client.getUsername(), toSendTo, "".getBytes());
        c.sendMessage(pm);

    }
}