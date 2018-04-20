package util;
/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : Util.JSONUtilTest.java
 Auteur(s)   : Kopp Olivier
 Date        : 20.04.2018
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/
import User.Group;
import User.Person;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JSONUtilTest {

    @Test
    void toJson() {
        Group g = new Group(new Person("user1"), new Person("user2"), new Person("user3"));
        String json = JSONUtil.toJson(g);
        System.out.println(json);
    }

    @Test
    void parseJson() {
        Group g = new Group(new Person("user1"), new Person("user2"), new Person("user3"));
        String json = JSONUtil.toJson(g);
        Group g2 = JSONUtil.parseJson(json, Group.class);
        assertTrue(g2.getMembers().size() == 3);
        assertTrue(g2.getMembers().get(0).getID().equals("user1"));
    }
}