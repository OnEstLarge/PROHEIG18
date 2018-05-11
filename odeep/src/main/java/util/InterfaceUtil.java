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

import User.Group;
import User.Person;
import message.MessageType;
import org.bouncycastle.crypto.InvalidCipherTextException;
import peer.PeerMessage;

import java.io.*;
import java.util.Properties;

/**
 * Classe utilitaire contenant les fonctions appelées depuis l'interface graphique.
 */
public class InterfaceUtil {

    /**
     * Crée un groupe.
     *
     * @param groupID nom du groupe
     * @return true,    groupe créé
     * false,   groupe déjà existant ou erreur
     */
    public static boolean createGroup(String groupID, String idFrom, String idTo) {

        // Check la validité du string groupID
        if (PeerMessage.isValidIdFormat(groupID, PeerMessage.ID_GROUP_MIN_LENGTH, PeerMessage.ID_GROUP_MAX_LENGTH)) {

            try {
                // Demande au serveur si le groupe existe déjà
                String serverIP = loadProperties("server.properties", "ip");
                PeerMessage message = new PeerMessage(MessageType.NEWG, groupID, idFrom, idTo, groupID.getBytes());

                // Génération du fichier config.json
                Group group = new Group(groupID, new Person(idFrom));
                String jsonConfig = JSONUtil.toJson(group);
                JSONUtil.updateConfig(group.getID(), jsonConfig);

                // Chiffrement du config.json
                RandomAccessFile f = new RandomAccessFile("./shared_files" + groupID + "/key", "r");
                byte[] key = new byte[(int) f.length()];
                f.readFully(key);
                byte[] cipherConfig = CipherUtil.AESEncrypt(JSONUtil.toJson(jsonConfig).getBytes(), key);

                //TODO : PAS FINI
                // Crée le groupe localement
                String dir = "./shared_files/" + groupID;
                File file = new File(dir);

                if (!file.exists() || !file.isDirectory()) {
                    file.mkdirs();
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidCipherTextException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void askFile(String groupID, String filename) {

    }

    /**
     * Récupère un propriété d'un fichier 'properties'.
     *
     * @param filename fichier 'properties'
     * @param property propriété concernée
     * @return la valeur de la propriété 'property'
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
