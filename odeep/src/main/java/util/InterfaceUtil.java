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
import org.bouncycastle.crypto.InvalidCipherTextException;
import peer.PeerMessage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
                    String dir = Constant.ROOT_GROUPS_DIRECTORY + "/" + groupID;
                    File file = new File(dir);
                    if (!file.exists() || !file.isDirectory()) {
                        file.mkdirs();
                    }
                    // Génération du fichier config
                    group = new Group(groupID, new Person(idFrom));
                    String jsonConfig = JSONUtil.toJson(group);

                    // Chiffrement du config
                    node.setKey(CipherUtil.generateKey(), groupID);
                    RandomAccessFile f = new RandomAccessFile(Constant.ROOT_GROUPS_DIRECTORY + "/" + groupID + "/" + Constant.KEY_FILENAME, "r");
                    byte[] key = new byte[(int) f.length()];
                    f.readFully(key);
                    byte[] cipherConfig = CipherUtil.AESEncrypt(jsonConfig.getBytes(), key);

                    // Ajoute le fichier 'config' chiffré localement dans le répertoire du groupe
                    JSONUtil.updateConfig(group.getID(), cipherConfig);

                    // Envoie le fichier 'config' chiffré au serveur
                    Client.uploadJSON(dir + "/" + Constant.CONFIG_FILENAME, groupID, idFrom);

                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if(out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return group;
    }

    /**
     * @param file
     * @param userID
     * @param group
     */
    public static void addFile(File file, String userID, Group group) {

        try {
            System.out.println("file.getName() = " + file.getName());
            // Vérifie que le nom de fichier est disponible (au sein du groupe)
            if (checkFilename(file.getName(), group)) {

                // Ajoute le fichier à la liste des fichiers de l'utilisateur
                group.addFile(file.getName(), userID);

                // Affecte la modification au fichier config et le chiffre
                RandomAccessFile f = new RandomAccessFile(Constant.ROOT_GROUPS_DIRECTORY + "/" + group.getID() + "/" + Constant.KEY_FILENAME, "r");
                byte[] key = new byte[(int) f.length()];
                f.readFully(key);
                byte[] cipherConfig = CipherUtil.AESEncrypt(JSONUtil.toJson(group).getBytes(), key);

                JSONUtil.updateConfig(group.getID(), cipherConfig);

                // Copie du fichier dans le répertoire 'shared_files/groupID'
                File fileDest = new File(Constant.ROOT_GROUPS_DIRECTORY + "/" + group.getID() + "/" + file.getName());

                System.out.println("\n\n\n-------COPY FILES--------------");
                System.out.println("SRC PATH = " + file.toPath().toString());
                System.out.println("DST PATH = " + fileDest.toPath().toString());

                Files.copy(file.toPath(), fileDest.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Envoi du fichier 'config' au serveur
                Client.uploadJSON(Constant.ROOT_GROUPS_DIRECTORY + "/" + group.getID() + "/" + Constant.CONFIG_FILENAME, group.getID(), userID);

                Client.refresh();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Copie le fichier dans le répertoire 'shared_files/groupID'
    private static void copyFile(File file, Group group) {

    }

    /**
     * @param filename
     * @param group
     * @return
     */
    private static boolean checkFilename(String filename, Group group) {
        for (Person person : group.getMembers()) {
            for (String f : person.getFiles()) {
                System.out.println(filename + "     ffffffffffffffffffffffffff         " + f);
                if (filename.equals(f)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @param filename
     * @param userID
     * @param group
     */
    public static void removeFile(String filename, String userID, Group group) {

        try {

            // Vérifie que le nom de fichier est disponible (au sein du groupe)
            if (!checkFilename(filename, group)) {

                // Ajoute le fichier à la liste des fichiers de l'utilisateur
                //Client.myself.removeFile(filename);
                group.getMember(userID).removeFile(filename);

                for(Person p: group.getMembers()) {
                    for(String s: p.getFiles()) {
                        System.out.println("fffffffffffff      "+s);
                    }
                }

                // Affecte la modification au fichier config et le chiffre
                RandomAccessFile f = new RandomAccessFile(Constant.ROOT_GROUPS_DIRECTORY + "/" + group.getID() + "/" + Constant.KEY_FILENAME, "r");
                byte[] key = new byte[(int) f.length()];
                f.readFully(key);
                byte[] cipherConfig = CipherUtil.AESEncrypt(JSONUtil.toJson(group).getBytes(), key);

                //InterfaceUtil.printConfig(group.getID(), key);

                JSONUtil.updateConfig(group.getID(), cipherConfig);

                //InterfaceUtil.printConfig(group.getID(), key);

                Client.uploadJSON(Constant.ROOT_GROUPS_DIRECTORY + "/" + group.getID() + "/" + Constant.CONFIG_FILENAME, group.getID(), userID);

                //remove local file - additional feature not implemented
                System.out.println(Constant.ROOT_GROUPS_DIRECTORY + "/" + group.getID() + "/" + Constant.CONFIG_FILENAME + "/" + filename);
                File file = new File(Constant.ROOT_GROUPS_DIRECTORY + "/" + group.getID() + "/" + filename);
                file.delete();

                Client.refresh();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    //TODO: pour tests uniquement
    public static void printConfig(String groupID, byte[] key) {
        // Récupère et chiffre de fichier config
        RandomAccessFile configFile = null;
        try {
            configFile = new RandomAccessFile(Constant.ROOT_GROUPS_DIRECTORY + "/" + groupID + "\"/\" + Constant.CONFIG_FILENAME", "r");
            byte[] configFileByte = new byte[(int) configFile.length()];
            configFile.readFully(configFileByte);

            byte[] plainConfig = CipherUtil.AESDecrypt(configFileByte, key);

            System.out.println("\n\n--------CONFIG--------------");
            System.out.println(new String(plainConfig) + "\n\n");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
        }

    }

}
