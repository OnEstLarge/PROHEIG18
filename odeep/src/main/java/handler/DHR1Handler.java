package handler;

/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : handler.DHR1Handler.java
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
import util.CipherUtil;
import Node.Node;

/**
 * Classe permettant le traitement d'un message de type DHR1
 */
public class DHR1Handler implements MessageHandler{
    @Override
    public void handleMessage(Node n, PeerConnection c, PeerMessage m) {
        //recupération des info RSA pour l'échange Diffie-Hellman
        RSAInfo RSA = n.getTempRSAInfo();
        if (RSA != null) {
            byte[] foreignKey = m.getMessageContent();
            byte[] encryptedKey = CipherUtil.RSAEncrypt(CipherUtil.byteToPublicKey(foreignKey), n.getKey(m.getIdGroup()));

            //envoie de la clé AES chiffrée grâce à la clé public du destinataire
            PeerMessage response = new PeerMessage(MessageType.DHS2, m.getIdGroup(), m.getIdTo(), m.getIdFrom(), encryptedKey);
            Client.sendPM(response);
           //fin du protocole DH on efface les info RSA
            n.setTempRSAInfo(null);
            Client.getController().enableButtons();
        }
    }
}
