
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.io.UnsupportedEncodingException;
import java.util.*;

import Node.FileSharingNode;
import User.Group;
import User.Person;
import com.google.gson.JsonObject;
import config.GenerateConfigFile;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;
import java.util.Scanner;
import Node.FileSharingNode;
import handler.RSAHandler;

import handler.SFILHandler;
import handler.SMESHandler;
import message.MessageHandler;
import message.MessageType;
import peer.PeerConnection;
import peer.PeerInformations;
import peer.PeerMessage;

import util.JSONUtil;

import util.CipherUtil;


public class Test {

    public static void main(String[] args) {

        final String idGroup = "group1";

        final PeerInformations schurch = new PeerInformations("schurch", "192.168.1.110",4444);
        final PeerInformations lionel = new PeerInformations("lionel", "192.168.1.119", 4444);
        final PeerInformations florent = new PeerInformations("florent", "10.192.92.92", 4444);
        final PeerInformations romain = new PeerInformations("romain", "10.192.93.186", 4444);
        final PeerInformations olivier = new PeerInformations("olivier", "10.192.93.97", 4444);
        final PeerInformations mathieu = new PeerInformations("mathieu", "10.192.91.89", 4444);

        final PeerInformations myInfo = mathieu;

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


        ////////////////////GENERATE CONFIG FILE////////////
        Group group1 = new Group("group1", new Person(mathieu.getID()),  new Person("FrouzDu78"), new Person("PussySlayer69"), new Person("Pierre-André"));
        Group group2 = new Group("group2", new Person(mathieu.getID()), new Person("LionelSuceur44"));

        List<Group> groups = new ArrayList<Group>();
        groups.add(group1);
        groups.add(group2);

        GenerateConfigFile configFile = new GenerateConfigFile("config", mathieu.getID(), groups);


        try {

            JSONUtil.updateConfig(JSONUtil.toJson(configFile));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ////////////////////////////////////////////////


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

                            //ICI on crée le peerInfo avec ce que nous renvoie le serveur relay
                            //ça sera tout le temps fait dans le client j'imagine

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
                    else if(type.equals(MessageType.DHS1)){
                        try {
                            n.setKey(CipherUtil.generateKey());
                            n.setKey("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa".getBytes());
                            RSAHandler RSA = new RSAHandler();
                            RSA.setKeys();
                            n.setTempRSAInfo(RSA);
                            System.out.println("key is : " + new String (n.getKey()));

                            PeerConnection p = new PeerConnection(users.get(pseudo));
                            p.sendMessage(new PeerMessage(type, idGroup, myInfo.getID(), pseudo,n.getTempRSAInfo().getPublicKey()));
                            p.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (NoSuchProviderException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        Client client = new Client();

        n.acceptingConnections();

    }

}
