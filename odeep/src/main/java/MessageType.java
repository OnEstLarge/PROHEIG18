public class MessageType {
    public final static String FGET = "FGET"; // demande d'envoie d'un fichier a un noeud
    public final static String PGET = "PGET"; // demande d'envoie d'un paquet a un noeud
    public final static String RFIL = "RFIL"; // demande a un noeud si celui ci possède un fichier
    public final static String SFIL = "SFIL"; // envoie d'un fichier
    public final static String SMES = "SMES"; // envoie d'un message
    public final static String NOK = "NOK"; // réponse générique négative
    public final static String OK = "OK"; // réponse positive
    public final static String RKEY = "RKEY"; // demande de la clé public du destinataire pour le protocole ECDH
    public final static String ECDH = "ECDH"; // message de protocole ECDH
}
