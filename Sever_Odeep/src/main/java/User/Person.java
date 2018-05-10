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
    private List<File> files = new ArrayList<File>();
    private boolean isConnected = false;

    public Person(){}

    public Person(String ID){
        this.ID = ID;
    }

    public Person(String ID, File... files){
        this(ID);
        for(File f : files){
            this.files.add(f);
        }
    }

    public void addFile(File f){
        if(!files.contains(f)) {
            files.add(f);
        }
    }

    public void removeFile(File file){
        int index = -1;
        for(File f : files){
            if(f.getName().equals(file.getName())){
                index = files.indexOf(f);
                break;
            }
        }
        if(index != -1) {
            files.remove(index);
        }
    }

    public void connect(){isConnected = true;}

    public void disconnect(){isConnected = false;}

    public String getID(){
        return ID;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public List<File> getFiles() {
        return files;
    }
}
