/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : Node.java
 Auteur(s)   : Burgbacher Lionel, Jee Mathieu, Schürch Loïc
 Date        : 15.03.2018
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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
     * @param peers nouveau(x) pair(s) à ajouter
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
        // TODO: check typeMessage format
        mapMessage.put(typeMessage, handler);
    }

    /**
     * Ferme la connexion entrante du Noeud (Node).
     */
    public void shutdown() {
        nodeIsRunning = false;
    }

    /**
     * Ouvre la connexion entrante du Noeud (Node).
     */
    public void turnOn() {
        nodeIsRunning = true;
    }

    public ArrayList<PeerInformations> getKnownPeers() {
        return knownPeers;
    }

    /**
     * Retire un pair de la map 'mapMessage'.
     *
     * @param peers pair(s) à retirer
     */
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

    /**
     *
     * @param message
     * @param peer
     */
    public void sendToPeer(PeerMessage message, PeerInformations peer) {
    
    }

    public HashMap<String, MessageHandler> getMapMessage() {
        return mapMessage;
    }

    /**
     *
     */
    public void AcceptingConnections() {

        ServerSocket serverSocket;

        try {
            serverSocket = new ServerSocket(myInfos.getPort());
        } catch (IOException ex) {
            return; //A gerer
        }

        while(nodeIsRunning) {
            //socket wait for connection
            try {
                Socket clientSocket = serverSocket.accept();
                PeerHandler peerHandler = new PeerHandler(this, clientSocket);
            } catch (IOException ex) {

            }
        }
    }

    // Informations sur le pair de ce noeud
    private PeerInformations myInfos;

    // Les informations sur les pairs que ce noeud connait
    private ArrayList<PeerInformations> knownPeers;

    // Association entre les types de message et leur handlers
    private HashMap<String, MessageHandler> mapMessage;

    // Permet de déterminer sur le noeud est actif
    private boolean nodeIsRunning = true;
}
