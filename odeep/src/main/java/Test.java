
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import Node.FileSharingNode;
import handler.SFILHandler;
import handler.SMESHandler;
import message.MessageHandler;
import message.MessageType;
import peer.PeerConnection;
import peer.PeerInformations;
import peer.PeerMessage;

public class Test {

    public static void main(String[] args) {

        final String idGroup = "group1";

        final PeerInformations schurch = new PeerInformations("schurch", "10.192.95.151",4444);
        final PeerInformations lionel = new PeerInformations("lionel", "10.192.95.141", 4444);
        final PeerInformations florent = new PeerInformations("florent", "10.192.92.92", 4444);
        final PeerInformations romain = new PeerInformations("romain", "10.192.93.186", 4444);
        final PeerInformations olivier = new PeerInformations("olivier", "10.192.93.97", 4444);
        final PeerInformations mathieu = new PeerInformations("mathieu", "10.192.91.89", 4444);

        final PeerInformations myInfo = schurch;

        final HashMap<String, PeerInformations> users = new HashMap<String, PeerInformations>();
        users.put("schurch", schurch);
        users.put("lionel", lionel);
        users.put("florent", florent);
        users.put("romain",romain);
        users.put("olivier",olivier);
        users.put("mathieu",mathieu);

        final FileSharingNode n = new FileSharingNode(myInfo);

        MessageHandler sendMessageHandler = new SMESHandler();
        MessageHandler sendFileHandler = new SFILHandler();

        n.addPeer(schurch);
        n.addPeer(lionel);
        n.addPeer(florent);
        n.addPeer(romain);
        n.addPeer(olivier);
        n.addPeer(mathieu);
        n.addMessageHandler(MessageType.SMES, sendMessageHandler);
        n.addMessageHandler(MessageType.SFIL, sendFileHandler);


        //lancer un client qui lit stdin, simule l'interface graphique
        class Client implements Runnable {

            private Thread activity;

            public Client(){
                activity = new Thread(this);
                activity.start();
            }

            public void run(){

                Scanner scanner = new Scanner(System.in);
                while(true){

                    String mess = scanner.nextLine();

                    String[] a = mess.split(" ");
                    String type = a[0];
                    String pseudo = a[1];
                    String content = a[2];

                    if(type.equals(MessageType.SMES)) {
                        PeerMessage m = new PeerMessage(type, idGroup, myInfo.getID(), pseudo, content.getBytes());
                        try {
                            PeerConnection p = new PeerConnection(users.get(pseudo));
                            p.sendMessage(m);
                            p.close();
                        }catch(IOException e) {
                            System.out.println("error main");
                        }
                    } else if(type.equals(MessageType.SFIL)) {
                        File file = new File(content);
                        try{
                            n.sendFileToPeer(file, idGroup, users.get(pseudo));
                        } catch(IOException e){}
                    }
                }
            }
        }

        Client client = new Client();

        n.AcceptingConnections();

    }

}
