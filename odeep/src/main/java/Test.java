
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import Node.FileSharingNode;
import handler.SFILHandler;
import handler.SMESHandler;
import message.MessageHandler;
import message.MessageType;
import peer.PeerInformations;
import peer.PeerMessage;

public class Test {

    public static void main(String[] args) {

        final String idGroup = "group1";

        final PeerInformations myInfo = new PeerInformations("schurch", "10.192.95.152", 4444);
        PeerInformations lionel = new PeerInformations("lionel", "10.192.95.98", 4444);
        final HashMap<String, PeerInformations> users = new HashMap<String, PeerInformations>();
        users.put("lionel", lionel);

        final FileSharingNode n = new FileSharingNode(myInfo);
        MessageHandler sendMessageHandler = new SMESHandler();
        MessageHandler sendFileHandler = new SFILHandler();

        n.addPeer(lionel);
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
                        System.out.println(m.getFormattedMessage());
                        n.sendToPeer(m, users.get(pseudo));
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
