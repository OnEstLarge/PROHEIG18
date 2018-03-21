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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
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
        for (PeerInformations peer : peers) {
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
    public void addMessageHandler(String typeMessage, MessageHandler handler) throws IllegalArgumentException{
        if(!PeerMessage.isValidTypeFormat(typeMessage)) {
            throw new IllegalArgumentException("Bad type format");
        }
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
        for (PeerInformations peer : peers) {
            knownPeers.remove(peer);
        }
    }

    public PeerInformations getNodePeer() {
        return myInfos;
    }

    /**
     * @param message
     * @param peer
     */
    public void sendToPeer(PeerMessage message, PeerInformations peer) {
        Socket clientSocket = null;
        OutputStream outputStream = null;

        try {
            clientSocket = new Socket(peer.getAddress(), peer.getPort());
            outputStream = clientSocket.getOutputStream();

            outputStream.write(message.getMessage().getBytes());
            System.out.println("Message sent from: " + myInfos.getID() + ", to: " + peer.getID());

        } catch (UnknownHostException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            // close outputStream and clientSocket
            cleanup(outputStream, null, clientSocket, null);
        }
    }

    public HashMap<String, MessageHandler> getMapMessage() {
        return new HashMap<String, MessageHandler>(mapMessage);
    }

    /**
     *
     */
    public void AcceptingConnections() {

        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(myInfos.getPort());
        } catch (IOException ex) {
            return; //A gerer
        } finally {
            // close serverSocket
            cleanup(null, null, null, serverSocket);
        }

        while (nodeIsRunning) {
            //socket wait for connection
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
                PeerHandler peerHandler = new PeerHandler(this, clientSocket);
            } catch (IOException ex) {

            } finally {
                // close clientSocket
                cleanup(null, null, clientSocket, null);
            }
        }
    }

    /**
     *
     */
    private void cleanup(OutputStream out, InputStream in, Socket clientSocket, ServerSocket serverSocket) {
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if(clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if(serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
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
