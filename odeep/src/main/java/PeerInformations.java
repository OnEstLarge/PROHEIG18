/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : PeerInformations.java
 Auteur(s)   : Schürch Loïc
 Date        : 15.03.2018
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/


/**
 * Contient les informations d'un pair nécéssaires à sa localisation dans le réseau.
 */
public class PeerInformations {

    //Constantes pour l'intervalle de ports
    public static final int PORT_RANGE_MIN = 0;
    public static final int PORT_RANGE_MAX = 65535;

    /**
     * Constructeur pour les informations d'un pair
     * @param ID      l'identifiant du pair
     * @param address l'adresse IP du pair
     * @param port    le port utilisé par le pair
     */
    public PeerInformations(String ID, String address, int port) {

        this.ID = ID;

        if(isValidIP(address)) {
            this.address = address;
        } else {
            throw new IllegalArgumentException("Bad IP");
        }

        if(PORT_RANGE_MIN <= port && port <= PORT_RANGE_MAX) {
            this.port = port;
        } else {
            throw new IllegalArgumentException("Bad port");
        }
    }

    /**
     * Getter de l'identifiant du pair
     * @return l'identifiant du pair
     */
    public String getID(){
        return ID;
    }

    /**
     * Getter de l'adresse IP du pair
     * @return l'adresse IP du pair
     */
    public String getAddress() {
        return address;
    }

    /**
     * Setter de l'adresse IP du pair
     * @param address la nouvelle adresse IP du pair
     * @throws IllegalArgumentException si l'adresse IP ne correspond pas au format X.X.X.X
     *                                  où X est un nombre entre 0 et 255.
     */
    public void setAddress(String address) throws IllegalArgumentException{
        if(isValidIP(address)) {
            this.address = address;
        } else {
            throw new IllegalArgumentException("Bad IP");
        }
    }

    /**
     * Vérifie si l'adresse IP correspond au format X.X.X.X où X est un nombre entre 0 et 255.
     * @param ip l'adresse IP à vérifier.
     * @return   true si l'adresse IP correspond au format,
     *           false sinon.
     */
    public static boolean isValidIP(String ip) {
        try {
            if(ip == null || ip.equals("")) {
                return false;
            }

            String[] parts = ip.split("\\.");
            if(parts.length != 4) {
                return false;
            }
            for(String part: parts) {
                int i = Integer.parseInt(part);
                if( i < 0 || i > 255) {
                    return false;
                }
            }
            return !ip.endsWith(".");

        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Getter du numéro de port utilisé par le pair
     * @return le numéro de port utilisé
     */
    public int getPort() {
        return port;
    }

    /**
     * Setter du numéro de port utilisé par le pair
     * @param port Le nouveau port à utiliser
     * @throws IllegalArgumentException Si le port ne se situe pas entre PORT_RANGE_MIN et PORT_RANGE_MAX.
     */
    public void setPort(int port) throws IllegalArgumentException{
        if(PORT_RANGE_MIN <= port && port <= PORT_RANGE_MAX) {
            this.port = port;
        } else {
            throw new IllegalArgumentException("Bad port");
        }
    }

    /**
     * Redéfinition de la méthode toString
     * @return Les informations d'un pair
     */
    public String toString() {
        return ID + " - " + address + ":" + port + "\n";
    }

    //l'identifiant du pair
    private String ID;

    //l'adresse IP du pair
    private String address;

    //numéro de port TCP utilisé à l'application
    private int port;
}
