public class Test {

    public static void main(String[] args) {

        PeerInformations p1 = new PeerInformations("Lionel", "192.168.0.38", 1234);
        PeerInformations p2 = new PeerInformations("Jee", "192.168.0.46", 1234);

        Node n1 = new Node(p1);
        Node n2 = new Node(p2);


    }

}
