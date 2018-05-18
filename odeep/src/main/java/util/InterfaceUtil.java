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
import main.Client;
import Node.Node;
import message.MessageType;
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
    public static Group createGroup(String groupID, String idFrom, Node node) {
        Group group = null;
        // Check la validité du string groupID
        if (PeerMessage.isValidIdFormat(groupID, PeerMessage.ID_GROUP_MIN_LENGTH, PeerMessage.ID_GROUP_MAX_LENGTH)) {
            FileOutputStream out = null;
            try {
                // Demande au serveur si le groupe existe déjà
                if(Client.groupValidation(groupID)) {
                    // Crée le groupe localement
                    String dir = "./shared_files/" + groupID;
                    File file = new File(dir);
                    if (!file.exists() || !file.isDirectory()) {
                        file.mkdirs();
                    }
                    // Génération du fichier config.json
                    group = new Group(groupID, new Person(idFrom));
                    String jsonConfig = JSONUtil.toJson(group);

                    // Chiffrement du config.json

                    node.setKey(CipherUtil.generateKey(), groupID);
                    RandomAccessFile f = new RandomAccessFile("./shared_files/" + groupID + "/key", "r");
                    byte[] key = new byte[(int) f.length()];
                    f.readFully(key);
                    byte[] cipherConfig = CipherUtil.AESEncrypt(jsonConfig.getBytes(), key);

                    // Ajoute le fichier 'config.json' chiffré localement dans le répertoire du groupe
                    JSONUtil.updateConfig(group.getID(), jsonConfig);
                    //TODO: stocker cipher config
                    // Envoie le fichier 'config.json' chiffré au serveur
                    Client.uploadJSON(dir + "/config.json", groupID, idFrom);

                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

            }
        }
        return group;
    }

    /**
     * @param filename
     * @param userID
     * @param groupID
     */
    public static void addFile(String filename, String userID, String groupID) {

        try {
            //Récupère le contenu du fichier config.json
            RandomAccessFile f = new RandomAccessFile("./shared_files/" + groupID + "/config.json", "r");
            byte[] configData = new byte[(int) f.length()];
            f.readFully(configData);

            Group group = JSONUtil.parseJson(new String(configData), Group.class);

            // Vérifie que le nom de fichier est disponible (au sein du groupe)
            if (checkFilename(filename, group)) {

                // Ajoute le fichier à la liste des fichiers de l'utilisateur
                group.addFile(filename, userID);

                // Affecte la modification au fichier config.json
                JSONUtil.updateConfig(groupID, JSONUtil.toJson(group));

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param filename
     * @param group
     * @return
     */
    private static boolean checkFilename(String filename, Group group) {
        for (Person person : group.getMembers()) {
            for (String f : person.getFiles()) {
                if (filename.equals(f)) {
                    return false;
                }
            }
        }
        return true;
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
