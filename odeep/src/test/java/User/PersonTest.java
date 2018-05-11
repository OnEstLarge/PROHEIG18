package User;
/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : User.PersonTest.java
 Auteur(s)   : Kopp Olivier
 Date        : 20.04.2018
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class PersonTest {
    Person p = new Person();

    @Test
    void addFile() {
        String f1 = "file1";
        String f2 = "file2";
        p.addFile(f1);
        p.addFile(f2);
        assertEquals(1, p.getFiles().size());
        assertTrue(p.getFiles().contains(f1));
    }

    @Test
    void removeFile() {
        String f1 = "file1";
        String f2 = "file2";
        String f3 = "file3";
        p.addFile(f1);
        p.addFile(f2);
        p.addFile(f3);
        assertEquals(3, p.getFiles().size());
        p.removeFile(f1);
        assertEquals(2, p.getFiles().size());
    }

    @Test
    void connect() {
        p.connect();
        assertTrue(p.isConnected());
    }

    @Test
    void disconnect() {
        p.disconnect();
        assertFalse(p.isConnected());
    }
}