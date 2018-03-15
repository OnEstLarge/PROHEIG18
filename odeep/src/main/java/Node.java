/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : Node.java
 Auteur(s)   : Schürch Loïc
 Date        : 15.03.2018
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/


import java.util.ArrayList;
import java.util.HashMap;

/**
 * Noeud P2P.
 */
public class Node {

    public Node(PeerInformations myInfos) {
        this.myInfos = myInfos; //copie profonde?

        peersInfos = new ArrayList<PeerInformations>();
        mapMessage = new HashMap<String, MessageHandler>();
    }

    public void AcceptingConnections() {

        while(nodeIsRunning) {
            //socket wait for connection

            //start a new PeerHandler
        }
    }

    //Informations sur le pair de ce noeud
    private PeerInformations myInfos;

    //Les informations sur les pairs que ce noeud connait
    private ArrayList<PeerInformations> peersInfos;

    //Association entre les types de message et leur handlers
    private HashMap<String, MessageHandler> mapMessage;

    //Permet de déterminer sur le noeud est actif
    private boolean nodeIsRunning = false;
}
