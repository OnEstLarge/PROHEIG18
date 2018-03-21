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
        this.myInfos = myInfos;

        knownPeers = new ArrayList<PeerInformations>();
        mapMessage = new HashMap<String, MessageHandler>();
    }

    /**
     * Ajoute un ou plusieurs pairs à la liste de pairs connus.
     *
     * @param peers
     */
    public void addPeer(PeerInformations... peers) {
        for(PeerInformations peer : peers) {
            if (!knownPeers.contains(peer)) {
                knownPeers.add(peer);
            }
        }
    }

    /**
     * Ajoute un nouveau type de message accompagné de son handler dans la map 'mapMessage'.
     *
     * @param typeMessage type de message reçu/envoyé (format: 4 lettres majuscules, ex: XXXX)
     * @param handler     handler correspondant au type de message
     */
    public void addMessageHandler(String typeMessage, MessageHandler handler) {

    }

    /**
     * Ferme la connexion entrante du Noeud
     */
    public void shutdown() {
        nodeIsRunning = false;
    }

    public void turnOn() {
        nodeIsRunning = true;
    }

    public ArrayList<PeerInformations> getKnownPeers() {
        return knownPeers;
    }

    public void removeKnownPeers(PeerInformations... peers) {
        for(PeerInformations peer : peers) {
            if (knownPeers.contains(peer)) {
                knownPeers.remove(peer);
            }
        }
    }

    public PeerInformations getNodePeer() {
        return myInfos;
    }

    public void sendToPeer(PeerMessage message, PeerInformations peer) {

    }

    /**
     *
     */
    public void AcceptingConnections() {

        while(nodeIsRunning) {
            //socket wait for connection

            //start a new PeerHandler
        }
    }

    //Informations sur le pair de ce noeud
    private PeerInformations myInfos;

    //Les informations sur les pairs que ce noeud connait
    private ArrayList<PeerInformations> knownPeers;

    //Association entre les types de message et leur handlers
    private HashMap<String, MessageHandler> mapMessage;

    //Permet de déterminer sur le noeud est actif
    private boolean nodeIsRunning = true;
}
