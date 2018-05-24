package User;
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
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe qui permet la gestion d'une personne dans un groupe F2F
 */
public class Person implements Serializable{
    private String ID = "";
    private List<String> files = new ArrayList<String>();
    private boolean isConnected = false;

    public Person(){}

    public Person(String ID){
        this.ID = ID;
    }

    public Person(String ID, String... files){
        this(ID);
        for(String f : files){
            this.files.add(f);
        }
    }

    /**
     * Ajoute un fichier à laliste de fichier
     * @param f fichier é ajouter
     */
    public void addFile(String f){
        if(!files.contains(f)) {
            files.add(f);
        }
    }

    /**
     * Efface un fichier de la liste des fichiers
     * @param file fichier à supprimer
     */
    public void removeFile(String file){
        files.remove(file);
    }

    public void connect(){isConnected = true;}

    public void disconnect(){isConnected = false;}

    public String getID(){
        return ID;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public List<String> getFiles() {
        return files;
    }
}
