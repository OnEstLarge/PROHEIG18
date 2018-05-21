import java.io.File;
import java.io.IOException;

import java.util.*;

import User.Group;
import User.Person;

import java.util.HashMap;
import java.util.Scanner;

import handler.*;

import message.MessageHandler;
import message.MessageType;
import peer.PeerConnection;
import peer.PeerInformations;
import peer.PeerMessage;

import util.JSONUtil;

import util.CipherUtil;
import Node.Node;


public class Test {

    public static void main(String[] args) {

        final String idGroup = "group1";

        final PeerInformations schurch = new PeerInformations("schurch", "192.168.1.110", 4444);
        final PeerInformations lionel = new PeerInformations("lionel", "192.168.1.119", 4444);
        final PeerInformations florent = new PeerInformations("florent", "192.168.0.214", 4444);
        final PeerInformations romain = new PeerInformations("romain", "192.168.0.248", 4444);
        final PeerInformations olivier = new PeerInformations("olivier", "192.168.0.249", 4444);
        final PeerInformations mathieu = new PeerInformations("mathieu", "10.192.91.89", 4444);

        final PeerInformations myInfo = olivier;

        final HashMap<String, PeerInformations> users = new HashMap<String, PeerInformations>();
        users.put("schurch", schurch);
        users.put("lionel", lionel);
        users.put("florent", florent);
        users.put("romain", romain);
        users.put("olivier", olivier);
        users.put("mathieu", mathieu);

        final Node n = new Node(myInfo);

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
        n.addMessageHandler(MessageType.DHS1, new DHS1Handler());
        n.addMessageHandler(MessageType.DHS2, new DHS2Handler());
        n.addMessageHandler(MessageType.DHR1, new DHR1Handler());
        n.addMessageHandler(MessageType.RFIL, new RFILHandler());
        n.addMessageHandler(MessageType.SFIL, new SFILHandler());
        n.addMessageHandler(MessageType.NFIL, new NFILHandler());
        n.addMessageHandler(MessageType.PGET, new PGETHandler());


        ////////////////////GENERATE CONFIG FILE////////////
        Group group1 = new Group("group1", new Person(mathieu.getID()), new Person("FrouzDu78"), new Person("PussySlayer69"), new Person("Pierre-André"));
        Group group2 = new Group("group2", new Person(mathieu.getID()), new Person("LionelSuceur44"));
        Person temp = new Person("florent");
        temp.addFile("JEE.pdf");
        temp.addFile("Fear.mkv");
        group1.addMember(temp);
        Person temp2 = new Person("olivier");
        temp2.addFile("Fear.mkv");
        temp2.addFile("test.mp4");
        group1.addMember(temp2);
        Person temp3 = new Person("romain");
        group1.addMember(temp3);

        List<Group> groups = new ArrayList<Group>();
        groups.add(group1);
        groups.add(group2);

        String dir = "./shared_files/" + group1.getID();
        File file = new File(dir);
        file.mkdirs();

        JSONUtil.updateConfig(group1.getID(), JSONUtil.toJson(group1).getBytes());

        //InterfaceUtil.addFile("testFile", "PussySlayer69", group1);
        //InterfaceUtil.addFile("testFil2e", "FrouzDu78", group1);
        System.out.println("testFile added");

        ////////////////////////////////////////////////


        //lancer un client qui lit stdin, simule l'interface graphique
        class Client implements Runnable {

            private Thread activity;

            public Client() {
                activity = new Thread(this);
                activity.start();
            }

            public void run() {

                Scanner scanner = new Scanner(System.in);
                while (true) {

                    String mess = scanner.nextLine();

                    String[] a = mess.split(" ");
                    String type = a[0];
                    String group = a[1];
                    String pseudo = a[2];
                    String content = a[3];

                    if (type.equals(MessageType.SMES)) {
                        PeerMessage m = new PeerMessage(type, idGroup, myInfo.getID(), pseudo, content.getBytes());
                        try {

                            //ICI on crée le peerInfo avec ce que nous renvoie le serveur relay
                            //ça sera tout le temps fait dans le client j'imagine

                            PeerConnection p = new PeerConnection(users.get(pseudo));
                            p.sendMessage(m);
                            p.close();
                        } catch (IOException e) {
                            System.out.println("error main");
                        }
                    } else if (type.equals(MessageType.SFIL)) {
                        File file = new File(content);
                        try {
                            n.sendFileToPeer(file, idGroup, users.get(pseudo).getID());
                        } catch (IOException e) {
                        }
                    } else if (type.equals(MessageType.DHS1)) {
                        try {
                            n.setKey(CipherUtil.generateKey(), group);

                            RSAInfo RSA = new RSAInfo();
                            RSA.setKeys();
                            n.setTempRSAInfo(RSA);

                            System.out.println("key is : " + new String(n.getKey(group)));


                            PeerConnection p = new PeerConnection(users.get(pseudo));
                            p.sendMessage(new PeerMessage(type, idGroup, myInfo.getID(), pseudo, n.getTempRSAInfo().getPublicKey()));
                            p.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else if (type.equals(MessageType.RFIL)) {
                        n.requestFile(content, group);
                    }
                }
            }
        }

        Client client = new Client();

        n.acceptingConnections();

    }

}
