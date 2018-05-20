package peer;
import com.sun.media.sound.InvalidFormatException;
import handler.RSAHandler;
import Node.Node;
import message.MessageType;
/*
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
    private RSAHandler RSA;

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



        node.getMapMessage().get(message.getType()).handleMessage(node,connection, message); //gerer erreur possible

        if(message.getType() != MessageType.DHS1 || message.getType() != MessageType.DHR1 || message.getType() != MessageType.DHS2) {
            //close connection
            connection.close();
        }

    }

}
