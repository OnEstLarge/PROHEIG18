package handler;

/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : handler.NFILHandler.java
 Auteur(s)   : Kopp Olivier
 Date        : 18.05.2018
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/


import message.MessageHandler;
import peer.PeerConnection;
import peer.PeerMessage;
import main.Client;
import util.InterfaceUtil;
import Node.Node;

/**
 * Classe permettant le traitement d'un message de type NFIL
 */
public class NFILHandler implements MessageHandler {
    @Override
    public void handleMessage(Node n, PeerConnection c, PeerMessage m) {

        Client.downloadJSON(m.getIdGroup());
        Client.requestFile(new String(m.getMessageContent()), m.getIdGroup());
    }
}
