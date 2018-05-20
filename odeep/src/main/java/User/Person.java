package User;
/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : User.Person.java
 Auteur(s)   : Kopp Olivier
 Date        : 20.04.2018
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

    public void addFile(String f){
        if(!files.contains(f)) {
            files.add(f);
        }
    }

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
