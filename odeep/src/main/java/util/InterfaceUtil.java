package util;

/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : handler.DHR1Handler.java
 Auteur(s)   : Burgbacher Lionel, Jee Mathieu, Kopp Olivier, Piller Florent,
               Silvestri Romain, Schürch Loïc
 Date        : 20.04.2018
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/

import User.Group;
import User.Person;
import main.Client;
import Node.Node;
import peer.PeerMessage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

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
            }
        }
        return group;
    }

    /**
     * Ajoute un fichier à un groupe
     * @param file fichier à ajouter
     * @param userID nom de l'utilisateur effectuant la modification
     * @param group groupe concerné par la modification
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

    /**
     * Vérifie si un fichier est présent dans un groupe
     * @param filename fichier recherché
     * @param group groupe concerné
     * @return false si le fichier est déjà présent
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

    /**
     * Efface un fichier localement d'un groupe
     * @param filename nom du fichier à supprimer
     * @param userID nom de l'utilisateur effectuant la suppression
     * @param group groupe concerné
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
                    }
                }

                // Affecte la modification au fichier config et le chiffre
                RandomAccessFile f = new RandomAccessFile(Constant.ROOT_GROUPS_DIRECTORY + "/" + group.getID() + "/" + Constant.KEY_FILENAME, "r");
                byte[] key = new byte[(int) f.length()];
                f.readFully(key);
                byte[] cipherConfig = CipherUtil.AESEncrypt(JSONUtil.toJson(group).getBytes(), key);

                //mise à jour du fichier de config
                JSONUtil.updateConfig(group.getID(), cipherConfig);
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

    /**
     * Vérifie dans le fichier '.userInfo' si l'utilisateur possède déjà un nom d'utilisateur.
     *
     * @return le nom de l'utilisateur.
     * null, si l'utilisateur ne possède pas encore de nom.
     */
    public static String usernameExists() {
        String username = null;
        final String userFilename = ".userInfo";
        File userFile = new File("./" + userFilename);

        if (userFile.exists() && !userFile.isDirectory()) {
            username = readFromFile(userFile);
            username = username.replaceAll("[^A-Za-z0-9]", ""); //remove all non aplhanumeric character
        }

        return username;
    }

    /**
     * Lis intégralement un fichier et retourne son contenu sous forme de chaine de caractères
     * @param file Le fichier à lire
     * @return Les donneés lues sous forme de chaine de caractères
     */
    private static String readFromFile(File file) {
        StringBuilder stringBuilder = new StringBuilder();
        FileInputStream fileInputStream = null;
        BufferedReader bufferedReader = null;
        try {
            fileInputStream = new FileInputStream(file);
            bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return stringBuilder.toString();
    }

    /**
     * Ecrit des données dans un fichier
     * @param file Le fichier dans lequel écrire les données
     * @param data Les donneés à écrire
     */
    public static void writeToFile(File file, String data) {
        PrintWriter writer = null;
        try {

            writer = new PrintWriter(file, "UTF-8");
            writer.print(data);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            writer.close();
        }
    }

}
