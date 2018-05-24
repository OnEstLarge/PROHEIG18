package message;

/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : handler.DHR1Handler.java
 Auteur(s)   : Burgbacher Lionel, Jee Mathieu, Kopp Olivier, Piller Florent,
               Silvestri Romain, Schürch Loïc
 Date        : 15.03.2018
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/

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
 * Interface qui définit le comportement des handlers de PeerMessage
 */
public interface MessageHandler {

    void handleMessage(Node n, PeerConnection c, PeerMessage m);
}
