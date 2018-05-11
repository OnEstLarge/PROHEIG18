package util;

/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : util.InterfaceUtil.java
 Auteur(s)   : Jee Mathieu
 Date        : 20.04.2018
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/

import peer.PeerMessage;

import javax.swing.plaf.nimbus.State;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Classe utilitaire contenant les fonctions appelées depuis l'interface graphique.
 */
public class InterfaceUtil {

    /**
     * Crée un groupe.
     *
     * @param groupID   nom du groupe
     * @return true,    groupe créé
     *         false,   groupe déjà existant ou erreur
     */
    public static boolean createGroup(String groupID) {

        // Check la validité du string groupID
        if(PeerMessage.isValidIdFormat(groupID, PeerMessage.ID_GROUP_MIN_LENGTH, PeerMessage.ID_GROUP_MAX_LENGTH)) {

            // Demande au serveur si le groupe existe déjà
            String serverIP = loadProperties("server.properties", "ip");


            // Si non, crée le groupe localement
        }

        return false;
    }

    public static void askFile(String groupID, String filename) {

    }

    /**
     * Récupère un propriété d'un fichier 'properties'.
     *
     * @param filename  fichier 'properties'
     * @param property  propriété concernée
     * @return  la valeur de la propriété 'property'
     */
    public static String loadProperties(String filename, String property) {
        String result = "";
        Properties properties = new Properties();
        InputStream in = null;

        try {
            in = new FileInputStream(filename);
            properties.load(in);
            result = properties.getProperty(property);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }



}
