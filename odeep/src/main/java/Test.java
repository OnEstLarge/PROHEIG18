public class Test {

    public static void main(String[] args) {

        String idGroup = "group1";

        PeerInformations p1 = new PeerInformations("Lionel", "10.192.95.182", 4444);
        PeerInformations p2 = new PeerInformations("Mathieu", "10.192.91.55", 4444);

        Node n1 = new Node(p1);
        Node n2 = new Node(p2);

        String message1 = "HIHIHIHI";
        PeerMessage peerMessage1 = new PeerMessage("TEST", idGroup, p2.getID(), p1.getID(), message1.getBytes());
        String message2 = "HOHOHOHOHO";
        PeerMessage peerMessage2 = new PeerMessage("TEST", idGroup, p2.getID(), p1.getID(), message2.getBytes());

        // Client sends messages
        n2.sendToPeer(peerMessage1, p1);
        n2.sendToPeer(peerMessage2, p1);

        // Server listens
        //n1.AcceptingConnections();

    }

}
