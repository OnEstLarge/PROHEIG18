package message;

public class MessageType {
    public final static String FGET = "FGET"; // demande d'envoie d'un fichier a un noeud
    public final static String PGET = "PGET"; // demande d'envoie d'un paquet a un noeud
    public final static String RFIL = "RFIL"; // demande a un noeud si celui ci possède un fichier
    public final static String SFIL = "SFIL"; // envoie d'un fichier
    public final static String SMES = "SMES"; // envoie d'un message
    public final static String NOK = "NOK"; // réponse générique négative
    public final static String OK = "OK"; // réponse positive
    public final static String DHS1 = "DHS1"; //premier envoie du protocole DH (envoie de sa clé publique
    public final static String DHR1 = "DHR1"; //premiere reponse du protocole DH (le destinataire nous renvoie sa clé)
    public final static String DHS2 = "DHS2"; //deuxieme envoie du protocole DH (envoie de la clé, chiffrée avec le secret partagé)
    public final static String HELO = "HELO"; //initialisation de la connexion au serveur relai
    public final static String BYE = "BYE"; // fin de connexion au serveur relai
}
