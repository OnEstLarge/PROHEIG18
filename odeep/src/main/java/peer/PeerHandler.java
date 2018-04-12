package peer;
import Node.*;
import peer.*;
import message.*;/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : peer.PeerHandler.java
 Auteur(s)   : Schürch Loïc
 Date        : 15.03.2018
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/

import java.net.Socket;

public class PeerHandler implements Runnable {

    private Thread activity;
    Socket clientSocket;
    private Node node;

    public PeerHandler(Node node, Socket socket) {
        this.node = node;
        clientSocket = socket;
        activity = new Thread();
        activity.start();
    }

    public void run() {

        //new PeerConenction
        PeerConnection connection = new PeerConnection(clientSocket);

        //receive message from the connection
        PeerMessage message = connection.receiveMessage();

        //handle message
        node.getMapMessage().get(message.getType()).handleMessage(connection, message); //gerer erreur possible

        //close connection
        connection.close();
    }

}
