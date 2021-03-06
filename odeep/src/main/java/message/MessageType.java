package message;

/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : handler.DHR1Handler.java
 Auteur(s)   : Burgbacher Lionel, Jee Mathieu, Kopp Olivier, Piller Florent,
               Silvestri Romain, Schürch Loïc
 Date        : 17.03.2018
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/

/**
 * Contient tous les types de messages utilisés par l'application
 */
public class MessageType {
    public final static String PGET = "PGET"; // demande d'envoie d'un paquet a un noeud
    public final static String RFIL = "RFIL"; // demande a un noeud si celui ci possède un fichier
    public final static String SFIL = "SFIL"; // envoie d'un fichier
    public final static String SMES = "SMES"; // envoie d'un message
    public final static String DHS1 = "DHS1"; //premier envoie du protocole DH (envoie de sa clé publique
    public final static String DHR1 = "DHR1"; //premiere reponse du protocole DH (le destinataire nous renvoie sa clé)
    public final static String DHS2 = "DHS2"; //deuxieme envoie du protocole DH (envoie de la clé, chiffrée avec le secret partagé)
    public final static String HELO = "HELO"; //initialisation de la connexion au serveur relai
    public final static String EXIT = "EXIT"; // fin de connexion au serveur relai
    public final static String INVI = "INVI"; //invitation à un groupe
    public final static String INVK = "INVK"; //invitation acceptée
    public final static String INFO = "INFO"; //demande d'info pour le pear to pear local
    public final static String DOWN = "DOWN"; //download le fichier de config depuis le serveur
    public final static String UPDT = "UPDT"; // notifie l'utilisateur qu'il faut download le fichier du groupe
    public final static String UPLO = "UPLO"; //upload le fichier de config sur le serveur
    public final static String DISC = "DISC"; //indique que le destinataire d'un message est deconnecté
    public final static String NFIL = "NFIL"; //indique qu'on ne possede pas le fichier
    public final static String NEWG = "NEWG"; //check si le nouveau groupe peut etre crée
    public final static String USRV = "USRV"; //validation du pseudo par le server
}
