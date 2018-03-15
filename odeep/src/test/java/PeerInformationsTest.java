/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : PeerInformationsTest.java
 Auteur(s)   : Schürch Loïc
 Date        : 15.03.2018
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PeerInformationsTest {

    private PeerInformations peerInfo = new PeerInformations("User1", "10.10.10.10", 80);

    private static int numero = 0;

    private static String testName;

    @BeforeEach
    void setUp() {
        numero++;
        System.out.println("Test " + numero + " started");
    }

    @AfterEach
    void tearDown() {
        System.out.println("Test " + numero + " : " + testName + " completed");
    }

    @Test
    void getID() {
        testName = "getID()";
        assertEquals("User1", peerInfo.getID());
    }

    @Test
    void getAddress() {
        testName = "getAddress()";
        assertEquals("10.10.10.10", peerInfo.getAddress());
    }

    @Test
    void toStringShouldReturnTheCorrectResult() {
        testName = "toStringShouldReturnTheCorrectResult()";
        assertTrue(peerInfo.toString().equals("User1 - 10.10.10.10:80\n"));
    }

    @Test
    void settingValidIPAddressShouldWork() {
        testName = "settingValidIPAddressShouldWork()";
        peerInfo.setAddress("11.11.11.11");
        assertEquals("11.11.11.11", peerInfo.getAddress());
    }

    @Test
    void setIncorrectIPAddressShouldThrowAnException() {
        testName = "setIncorrectIPAddressShouldThrowAnException()";
        try{
            peerInfo.setAddress("1.1.1");
        } catch(IllegalArgumentException e) {
            assertEquals("Bad IP", e.getMessage());
        }
    }

    @Test
    void isValidIPShouldWorkForAValidIP() {
        testName = "isValidIPShouldWorkForAValidIP()";
        assertTrue(PeerInformations.isValidIP("125.123.1.50"));
        assertTrue(PeerInformations.isValidIP("0.0.0.0"));
        assertTrue(PeerInformations.isValidIP("255.255.255.255"));
    }

    @Test
    void isValidIPShouldNotWorkForAnIPNotValid() {
        testName = "isValidIPShouldNotWorkForAnIPNotValid()";
        assertFalse(PeerInformations.isValidIP(""));
        assertFalse(PeerInformations.isValidIP("a"));
        assertFalse(PeerInformations.isValidIP("1"));
        assertFalse(PeerInformations.isValidIP("1.1.1"));
        assertFalse(PeerInformations.isValidIP("...."));
        assertFalse(PeerInformations.isValidIP("1.1..1"));
        assertFalse(PeerInformations.isValidIP("1.1.1.1.1"));
        assertFalse(PeerInformations.isValidIP("1.1.1.1."));
        assertFalse(PeerInformations.isValidIP(".1.1.1.1"));
        assertFalse(PeerInformations.isValidIP("333.1.1.1"));
        assertFalse(PeerInformations.isValidIP("256.256.256.256"));
        assertFalse(PeerInformations.isValidIP("-1.1.1.1"));
    }

    @Test
    void getPort() {
        testName = "getPort()";
        assertEquals(80, peerInfo.getPort());
    }

    @Test
    void setValidPortShouldWork() {
        testName="setPort()";
        peerInfo.setPort(81);
        assertEquals(81, peerInfo.getPort());
    }

    @Test
    void setNegativePortNumberShouldNotWork() {
        testName = "setNegativePortNumberShouldNotWork()";
        try{
            peerInfo.setPort(PeerInformations.PORT_RANGE_MIN -1);
        } catch(IllegalArgumentException e) {
            assertEquals("Bad port", e.getMessage());
        }
    }

    @Test
    void setTooBigPortNumberShouldNotWork() {
        testName = "setTooBigPortNumberShouldNotWork()";
        try{
            peerInfo.setPort(PeerInformations.PORT_RANGE_MAX + 1);
        } catch(IllegalArgumentException e) {
            assertEquals("Bad port", e.getMessage());
        }
    }
}