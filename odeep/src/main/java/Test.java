public class Test {

    public static void main(String[] args) {

        PeerInformations p1 = new PeerInformations("Lionel", "192.168.0.38", 4444);
        PeerInformations p2 = new PeerInformations("Jee", "192.168.0.46", 4444);

        Node n1 = new Node(p1);
        Node n2 = new Node(p2);

        String message1 = "HIHIHIHI";
        PeerMessage peerMessage1 = new PeerMessage("TEST", message1);
        String message2 = "HOHOHOHOHO";
        PeerMessage peerMessage2 = new PeerMessage("TEST", message2);

        // Client sends messages
        //n2.sendToPeer(peerMessage1, p1);
        //n2.sendToPeer(peerMessage2, p1);

        // Server listens
        n1.AcceptingConnections();

    }

}
