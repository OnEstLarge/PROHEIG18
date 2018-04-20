package User;
/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : User.GroupTest.java
 Auteur(s)   : Kopp Olivier
 Date        : 20.04.2018
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GroupTest {

    private Group g;

    @BeforeEach
    void setUp() {
        g = new Group();
    }

    @Test
    void addMember() {
        g.addMember(new Person("person1"));
        g.addMember(new Person("person2"));
        assertEquals(2, g.getMembers().size());
    }

    @Test
    void deleteMembers() {
        Person p1 = new Person("person1");
        Person p2 = new Person("person2");
        g.addMember(p1);
        g.addMember(p2);
        assertEquals(2, g.getMembers().size());
        g.deleteMembers(new Person("person1"));
        assertEquals(1, g.getMembers().size());
        assertFalse(g.getMembers().contains(p1));
        assertTrue(g.getMembers().contains(p2));
    }
}