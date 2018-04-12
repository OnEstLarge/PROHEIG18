import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class Test {

    public static void main(String[] args) {

        final String idGroup = "group1";

        final PeerInformations myInfo = new PeerInformations("Loic", "10.192.95.152", 4444);
        PeerInformations jee = new PeerInformations("Jee", "10.192.91.55", 4444);
        PeerInformations olivier = new PeerInformations("Jee", "10.192.91.55", 4444);
        PeerInformations florent = new PeerInformations("Jee", "10.192.91.55", 4444);
        final HashMap<String, PeerInformations> users = new HashMap<String, PeerInformations>();
        users.put("jee", jee);
        users.put("olivier", olivier);
        users.put("florent", florent);

        final FileSharingNode n = new FileSharingNode(myInfo);
        MessageHandler sendMessageHandler = new SendMessageHandler();

        n.addPeer(jee, florent, olivier);
        n.addMessageHandler(MessageType.SMES, sendMessageHandler);


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
