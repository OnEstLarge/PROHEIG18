package handler;

/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : handler.DHR1Handler.java
 Auteur(s)   : Burgbacher Lionel, Jee Mathieu, Kopp Olivier, Piller Florent,
               Silvestri Romain, Schürch Loïc
 Date        : 18.05.2018
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/


import message.MessageHandler;
import org.bouncycastle.crypto.InvalidCipherTextException;
import peer.PeerConnection;
import peer.PeerMessage;
import main.Client;
import util.CipherUtil;
import Node.Node;

/**
 * Classe permettant le traitement d'un message de type NFIL
 */
public class NFILHandler implements MessageHandler {
    @Override
    public void handleMessage(Node n, PeerConnection c, PeerMessage m) {
        //ce message est recu lorsqu'un personne ne possède pas le fichier demandé

        Client.downloadJSON(m.getIdGroup());
        try {
            //on redemande le fichier à une autre personne
            n.requestFile(new String(CipherUtil.AESDecrypt(m.getMessageContent(), n.getKey(m.getIdGroup()))), m.getIdGroup());
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
        }
    }
}
