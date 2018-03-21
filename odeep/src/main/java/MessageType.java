public enum MessageType {
    FGET, // demande d'envoie d'un fichier a un noeud
    PGET, // demande d'envoie d'un paquet a un noeud
    HAVF, // demande a un noeud si celui ci possède un fichier
    NOK, // réponse générique négative
    OK; // réponse positive
}
