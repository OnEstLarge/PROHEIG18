package util;

/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : util.JSONUtil.java
 Auteur(s)   : Kopp Olivier, Jee Mathieu
 Date        : 20.04.2018
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/

import User.Group;
import com.google.gson.*;

import java.io.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Permet la ser/deserialisation d'objet, ainsi que la mise a jour du fichier de configuration des groupes
 */
public class JSONUtil {

    /**
     * Serialisation d'un objet
     * @param object objet à serialiser
     * @param <T>
     * @return payload JSON
     */
    public static <T extends Serializable> String toJson(T object){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(object);
    }

    /**
     * Transforme un payload JSON en objet
     * @param data payload JSON
     * @param c Class de l'objet à générer
     * @param <T>
     * @return L'objet déserialisé
     */
    public static <T extends Serializable> T parseJson(String data, Class c){
        return (T)(new Gson().fromJson(data, c));
    }

    /**
     * Mise à jour du fichier de config local à partir d'un objet group
     * @param group Objet à écrire dans le fichier de config
     */
    public static void updateConfig(Group group) {
        try {
            RandomAccessFile f = new RandomAccessFile(Constant.ROOT_GROUPS_DIRECTORY + "/" + group.getID() + "/" + Constant.KEY_FILENAME, "r");

            byte[] key = new byte[(int) f.length()];
            f.readFully(key);
            byte[] cipherConfig = CipherUtil.AESEncrypt(JSONUtil.toJson(group).getBytes(), key);

            updateConfig(group.getID(), cipherConfig);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ecriture du fichier de config localement
     * @param groupID groupe concerné
     * @param data données à écrire
     */
    public static void updateConfig(String groupID, byte[] data) {
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(new File(Constant.ROOT_GROUPS_DIRECTORY + "/" + groupID + "/" + Constant.CONFIG_FILENAME));
            fout.write(data);
            fout.flush();
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            if(fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
