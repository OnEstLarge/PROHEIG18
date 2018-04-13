
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

        final PeerInformations myInfo = new PeerInformations("schurch", "192.168.1.160", 4444);
        PeerInformations docker = new PeerInformations("docker", "192.168.1.134", 4444);
        PeerInformations loicpc = new PeerInformations("loicpc", "192.168.1.144", 4444);

        final HashMap<String, PeerInformations> users = new HashMap<String, PeerInformations>();
        users.put("docker", docker);
        users.put("loicpc", loicpc);

        final FileSharingNode n = new FileSharingNode(myInfo);
        MessageHandler sendMessageHandler = new SMESHandler();
        MessageHandler sendFileHandler = new SFILHandler();

        n.addPeer(docker);
        n.addPeer(loicpc);
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
