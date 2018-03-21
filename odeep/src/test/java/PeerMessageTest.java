import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PeerMessageTest {

    private PeerMessage message = new PeerMessage("ABCD", "This is a message");

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
    void getType() {
        assertEquals("ABCD", message.getType());
    }

    @Test
    void getMessage() {
        assertEquals("This is a message", message.getMessage());
    }

    @Test
    void isUpperCaseShouldWorkOnCorrectFormattedType() {
        testName = "isUpperCaseShouldWorkOnCorrectFormattedType";
        assertTrue(PeerMessage.isUpperCase(message.getType()));
    }

    @Test
    void isUpperCaseShouldNotWorkOnBadFormattedString() {
        testName = "isUpperCaseShouldNotWorkOnBadFormattedString";
        assertFalse(PeerMessage.isUpperCase(""));
        assertFalse(PeerMessage.isUpperCase("a"));
        assertFalse(PeerMessage.isUpperCase("1"));
        assertFalse(PeerMessage.isUpperCase("aA"));
        assertFalse(PeerMessage.isUpperCase("Aa"));
        assertFalse(PeerMessage.isUpperCase("AA-"));
        assertFalse(PeerMessage.isUpperCase("1A"));
        assertFalse(PeerMessage.isUpperCase("AAAaA"));
        assertFalse(PeerMessage.isUpperCase("AAAa"));
        assertFalse(PeerMessage.isUpperCase("aAAA"));
    }

    @Test
    void isValidTypeFormatShouldWorkOnCorrectFormattedType() {
        testName = "isValidTypeFormatShouldWorkOnCorrectFormattedType";
        assertTrue(PeerMessage.isValidTypeFormat(message.getType()));
    }

    @Test
    void isValidTypeFormatShouldNotWorkOnIncorrectFormattedType() {
        testName = "isValidTypeFormatShouldWorkOnCorrectFormattedType";
        assertFalse(PeerMessage.isValidTypeFormat(""));
        assertFalse(PeerMessage.isValidTypeFormat("-"));
        assertFalse(PeerMessage.isValidTypeFormat("1"));
        assertFalse(PeerMessage.isValidTypeFormat("aaaa"));
        assertFalse(PeerMessage.isValidTypeFormat("AAA"));
        assertFalse(PeerMessage.isValidTypeFormat("AAaA"));
        assertFalse(PeerMessage.isValidTypeFormat("AAAAA"));
        assertFalse(PeerMessage.isValidTypeFormat("AAA1"));
    }
}