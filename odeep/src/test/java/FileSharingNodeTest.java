
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileSharingNodeTest {
    @Test
    public void test1() {
        FileSharingNode file = new FileSharingNode(new PeerInformations("User1", "127.0.0.1", 80));
        assertTrue(true);
    }
}