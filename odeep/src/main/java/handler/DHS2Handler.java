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
import peer.PeerConnection;
import peer.PeerMessage;
import Node.Node;

/**
 * Classe permettant le traitement d'un message de type DHS2
 */
public class DHS2Handler implements MessageHandler {
    @Override
    public void handleMessage(Node n, PeerConnection c, PeerMessage m) {
        //stocke la clé AES dans un fichier key
        n.setKey(n.getTempRSAInfo().getFinalKey(m), m.getIdGroup());
        n.setTempRSAInfo(null);

        Client.updateJsonAfterInvitation(m.getIdGroup());
        //Client.downloadJSON(m.getIdGroup());

        Client.getController().enableButtons();
    }
}
