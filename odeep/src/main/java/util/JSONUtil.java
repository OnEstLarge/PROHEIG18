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

import com.google.gson.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
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

    public static void updateConfig(String groupID, String data) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter pw = new PrintWriter(groupID + "/" + CONFIG_FILE_NAME, "UTF-8");
        pw.write(data);
        pw.close();
    }

}
