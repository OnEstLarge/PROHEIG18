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
        System.out.println("lola");
        //new PeerConenction
        PeerConnection connection = new PeerConnection(clientSocket);
        System.out.println("lolb");
        System.out.println(connection == null);
        //receive message from the connection
        PeerMessage message = null;
        try {
            message = connection.receiveMessage();
            System.out.println("lolc");
        } catch(InvalidFormatException e) {}
        //handle message
        System.out.println(message.getType());
        node.getMapMessage().get(message.getType()).handleMessage(connection, message); //gerer erreur possible
        System.out.println("lold");
        //close connection
        connection.close();
    }

}
