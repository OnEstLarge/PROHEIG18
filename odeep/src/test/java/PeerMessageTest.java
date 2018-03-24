import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PeerMessageTest {

    private static final int BLOCK_SIZE = 4096;
    private static final int HEADER_SIZE = 48;
    private static final int MESSAGE_CONTENT_SIZE = BLOCK_SIZE - HEADER_SIZE;

    private byte[] goodMessageContent = new byte[MESSAGE_CONTENT_SIZE];
    private byte[] wrongMessageContent = new byte[1];

    private PeerMessage goodPeerMessage   = new PeerMessage("XXXX", "idFrom", "idTo==", 1, goodMessageContent);

    private static int testNumber = 0;
    private static String testName;

    @BeforeEach
    public void setUp() {
        testNumber++;
        System.out.println("Test " + testNumber + " started");
    }

    @AfterEach
    public void tearDown() {
        System.out.println("Test " + testNumber + " : " + testName + " completed");
    }

    @Test
    public void getPeerMessageType() {
        assertEquals("XXXX", goodPeerMessage.getType());
    }

    @Test
    public void isUpperCaseShouldWorkOnCorrectFormattedType() {
        testName = "isUpperCaseShouldWorkOnCorrectFormattedType";
        assertTrue(PeerMessage.isUpperCase(goodPeerMessage.getType()));
    }

    @Test
    public void isUpperCaseShouldNotWorkOnBadFormattedString() {
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
    public void isValidTypeFormatShouldWorkOnCorrectFormattedType() {
        testName = "isValidTypeFormatShouldWorkOnCorrectFormattedType";
        assertTrue(PeerMessage.isValidTypeFormat(goodPeerMessage.getType()));
    }

    @Test
    public void isValidTypeFormatShouldNotWorkOnIncorrectFormattedType() {
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

    @Test
    public void isValidIdFormatShouldWorkOnCorrectId() {
        testName = "isValidIdFormatShouldWorkOnCorrectId";
        assertTrue(PeerMessage.isValidIdFormat(goodPeerMessage.getIdFrom()));
        assertTrue(PeerMessage.isValidIdFormat(goodPeerMessage.getIdTo()));
    }

    @Test
    public void isValidIdFormatShouldNotWorkOnIncorrectId() {
        testName = "isValidIdFormatShouldNotWorkOnIncorrectId";
        assertFalse(PeerMessage.isValidIdFormat("ThisIsAWrongIfFormatBecauseItIsTooLong"));
        assertFalse(PeerMessage.isValidIdFormat("id"));
        //assertFalse(PeerMessage.isValidIdFormat("InvalidId€"));
        //assertFalse(PeerMessage.isValidIdFormat("[InvalidId]"));
    }

    @Test
    public void isValidMessageContentFormatShouldWorkOnCorrectMessageContent() {
        testName = "isValidMessageContentFormatShouldWorkOnCorrectMessageContent";
        assertTrue(PeerMessage.isValidMessageContentFormat(goodPeerMessage.getMessageContent()));
    }

    @Test
    public void isValidMessageContentFormatShouldNotWorkOnIncorrectMessageContent() {
        testName = "isValidMessageContentFormatShouldNotWorkOnIncorrectMessageContent";
        assertFalse(PeerMessage.isValidMessageContentFormat(wrongMessageContent));
    }

    /*
    @Test(expected = IllegalArgumentException.class)
    public void peerMessageConstructorShouldThrowExceptionOnIncorrectFormat() {
        new PeerMessage("XXXXX", "idFrom", "idTo", goodMessageContent);
    }
    */

    @Test
    public void getFormattedMessageTest() {
        testName = "getFormattedMessageTest";
        String goodMessage = "XXXX,idFrom,idTo==,00000001,";
        goodMessage += new String(goodMessageContent);
        assertEquals(goodMessage, new String(goodPeerMessage.getFormattedMessage()));
    }
}