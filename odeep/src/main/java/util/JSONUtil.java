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

    private static String CONFIG_FILE_NAME = "config.json";

    public static <T extends Serializable> String toJson(T object){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(object);
    }

    public static <T extends Serializable> T parseJson(String data, Class c){
        return (T)(new Gson().fromJson(data, c));
    }

    public static void updateConfig(Group group) {

    }

    public static void updateConfig(String groupID, byte[] data) throws FileNotFoundException, UnsupportedEncodingException {
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(new File("./shared_files/" + groupID + "/config.json"));
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
