package handler;

/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : handler.DISCHandler.java
 Auteur(s)   :
 Date        :
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/

import User.Group;
import message.MessageHandler;
import peer.PeerConnection;
import peer.PeerMessage;
import main.Client;
import util.JSONUtil;
import Node.Node;

/**
 * Classe permettant le traitement d'un message de type DISC
 */
public class DISCHandler implements MessageHandler{
    @Override
    public void handleMessage(Node n, PeerConnection c, PeerMessage m) {
        //on recupere le fichier de config en local et on indique que la personne que l'on a essayé de contacté est hors ligne
        String group = m.getIdGroup();
        Group g = Client.getGroupById(group);
        for(int i = 0; i < g.getMembers().size(); i++){
            if(g.getMembers().get(i).equals(m.getIdTo())){
                g.getMembers().get(i).disconnect();
                //on upload le nouveau fichier de config et on demande aux autres membres de le re-télécharger
                JSONUtil.updateConfig(g);
                Client.uploadJSON(JSONUtil.toJson(g),m.getIdGroup(), m.getIdFrom());
            }
        }
    }
}
