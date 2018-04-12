/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : MessageHandler.java
 Auteur(s)   : Schürch Loïc
 Date        : 15.03.2018
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/


public interface MessageHandler {

    public abstract void handleMessage(PeerConnection c, PeerMessage m);
}
