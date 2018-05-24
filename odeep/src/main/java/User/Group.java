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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe qui permet la gestion d'un groupe F2F
 */
public class Group implements Serializable {

    private String ID;
    private List<Person> members;

    public Group(){
        members = new ArrayList<>();
    }

    public Group(String ID) {
        this.ID = ID;
        members = new ArrayList<>();
    }

    public Group(String ID, Person... listPerson){
        members = new ArrayList<>();
        this.ID = ID;

        for(Person p : listPerson){
            members.add(p);
        }
    }

    /**
     * Permet d'ajouter un personne à un groupe
     * @param p personne à ajouter
     */
    public void addMember(Person p){
        members.add(p);
    }

    /**
     * Permet d'ajouter un fichier à un groupe
     * @param filename nom du fichier à ajouter
     * @param userID nom de la personne souhaitant ajouter le fichier
     */
    public void addFile(String filename, String userID) {
        for(Person person : members) {
            if(person.getID().equals(userID)) {
                person.addFile(filename);
                break;
            }
        }
    }

    /**
     * Permet de supprimer une personne d'un groupe
     * @param person nom de la personne à supprimer
     */
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

    public Person getMember(String userID) {
        for(Person p : members) {
            if(p.getID().equals(userID)) {
                return p;
            }
        }
        return null;
    }

    public List<Person> getMembers() {
        return new ArrayList<>(members);
    }

    public String getID() {
        return ID;
    }
}
