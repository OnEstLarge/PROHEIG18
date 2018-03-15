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

    private static final int PORT_RANGE_MIN = 0;
    private static final int PORT_RANGE_MAX = 65535;

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

    public String getID(){
        return ID;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        if(isValidIP(address)) {
            this.address = address;
        } else {
            throw new IllegalArgumentException("Bad IP");
        }
    }

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
            if(ip.endsWith(".")) {
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) throws IllegalArgumentException{
        if(PORT_RANGE_MIN <= port && port <= PORT_RANGE_MAX) {
            this.port = port;
        } else {
            throw new IllegalArgumentException("Bad port");
        }
    }

    public String toString() {
        return ID + " - " + address + ":" + port + "\n";
    }

    //l'identifiant du pair
    private String ID;

    //l'adresse IP du pair
    private String address;

    //port TCP dédié à l'application
    private int port;
}
