package User;
/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : User.Group.java
 Auteur(s)   : Kopp Olivier, Jee Mathieu
 Date        : 20.04.2018
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Group implements Serializable{

    private String ID;
    private List<Person> members;

    public Group(){
        members = new ArrayList<Person>();
    }

    public Group(String ID) {
        this.ID = ID;
        members = new ArrayList<Person>();
    }

    public Group(String ID, Person... listPerson){
        members = new ArrayList<Person>();
        this.ID = ID;

        for(Person p : listPerson){
            members.add(p);
        }
    }

    public void addMember(Person p){
        members.add(p);
    }

    public void deleteMembers(Person person){
        int index = -1;
        for(Person p : members){
            if(p.getID().equals(person.getID())){
                index = members.indexOf(p);
                break;
            }
        }
        if(index != -1) {
            members.remove(index);
        }
    }

    public List<Person> getMembers() {
        return members;
    }

    public String getID() {
        return ID;
    }
}
