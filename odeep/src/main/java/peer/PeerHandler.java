package peer;
import Node.*;
import com.sun.media.sound.InvalidFormatException;
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
        activity = new Thread(this);
        activity.start();
    }

    public void run() {
        PeerConnection connection = new PeerConnection(clientSocket);

        PeerMessage message = null;
        try {
            message = connection.receiveMessage();
        } catch(InvalidFormatException e) {}

        //handle message
        node.getMapMessage().get(message.getType()).handleMessage(connection, message); //gerer erreur possible

        //close connection
        connection.close();
    }

}
