package message;
import peer.*;
import Node.*;/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : message.MessageHandler.java
 Auteur(s)   : Schürch Loïc
 Date        : 15.03.2018
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/

/**
 * Interface utilisée par tous les handlers de PeerMessage
 */
public interface MessageHandler {

    void handleMessage(Node n, PeerConnection c, PeerMessage m);
}
