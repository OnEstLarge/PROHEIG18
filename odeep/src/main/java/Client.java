import Node.FileSharingNode;
import Node.Node;
import handler.*;
import message.MessageType;
import peer.PeerConnection;
import peer.PeerInformations;
import peer.PeerMessage;

import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Enumeration;
import java.util.Scanner;

public class Client {
    private static final String IP_SERVER = "206.189.49.105";
    private static final int PORT_SERVER = 8080;
    private static final int LOCAL_PORT = 4444;

    private static Socket clientSocketToServerPublic;
    private static BufferedInputStream in;
    private static BufferedOutputStream out;

    private static FileSharingNode n;
    private static boolean nodeIsRunning = true;
    private static String myPseudo;

    public static void main(String[] args) {

        //Check si première ouverture de l'app
        //Si première ouverture -> demander pseudo

        //Sinon, on peut le pseudo dans le fichier de config
        myPseudo = "WESHWESH";

        //Get local IP used
        String localIP = null;
        try {
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();
                while (ee.hasMoreElements()) {
                    Inet4Address i = null;
                    try {
                        i = (Inet4Address) ee.nextElement();
                        if(!i.getHostAddress().endsWith(".1") && !i.getHostAddress().endsWith(".255")) {
                            localIP = i.getHostAddress();
                        }
                    } catch (ClassCastException ex) {}
                }
            }
        }catch (SocketException e) {}

        System.out.println("Your local ip: " +localIP);


        PeerInformations myInfos = new PeerInformations(myPseudo, localIP, LOCAL_PORT);
        System.out.println("Created myInfos");
        n = new FileSharingNode(myInfos);
        System.out.println("Created the node");
        //Ajouter tous les handlers
        n.addMessageHandler(MessageType.INVI, new INVIHandler());
        n.addMessageHandler(MessageType.DISC, new DISCHandler());
        n.addMessageHandler(MessageType.NFIL, new NFILHandler());
        n.addMessageHandler(MessageType.RFIL, new RFILHandler());
        n.addMessageHandler(MessageType.SFIL, new SFILHandler());
        n.addMessageHandler(MessageType.SMES, new SMESHandler());
        n.addMessageHandler(MessageType.UPDT, new UPDTHandler());
        System.out.println("Added the handlers");

        //connection au server publique
        Client c = new Client();
        System.out.println("Connecting to server");
        c.initConnections(IP_SERVER, PORT_SERVER);

        //listening for incoming connections
        System.out.println("Launching node listening");
        n.acceptingConnections();

    }

    //initialise la connection avec le serveur et de lancer le server d'écoute du client
    public void initConnections(String ip, int port) {
        try {
            //clientSocketToServerPublic = new PeerConnection(new Socket(ip,port));
            clientSocketToServerPublic = new Socket(ip,port);
            System.out.println("Connected to server");
            in = new BufferedInputStream(clientSocketToServerPublic.getInputStream());
            out = new BufferedOutputStream(clientSocketToServerPublic.getOutputStream());

            //Greetings to server, receivinig response
            PeerMessage greetings = new PeerMessage(MessageType.HELO, "XXXXXX", myPseudo, "XXXXXX", 0, "".getBytes());
            out.write(greetings.getFormattedMessage());
            out.flush();

        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.out.println("Could not connect to the server");
            return;
        }

        new Thread(new ConnectionServer()).start();
    }

    //initialise la connection au serveur avec une thread pour la lecture et l'autre pour l'écriture
    private class ConnectionServer implements Runnable{
        public ConnectionServer(){
        }
        public void run(){
            System.out.println("Reading from server start");
            new Thread(new ReadFromServer()).start();
            System.out.println("writing to server start");
            new Thread(new WriteToServer()).start();
        }

    }

    //Classe permettant de threader la lecture des packets server
    private class ReadFromServer implements Runnable{
        public void run(){
            int read;
            byte[] buffer = new byte[4096];

            System.out.println("Start reading in Client.ReadFromServer");
            try {
                while ((read = in.read(buffer)) != -1) {
                    PeerMessage pm = new PeerMessage(buffer);
                    //String type = pm.getType();

                    redirectToHandler(pm, n, new PeerConnection(clientSocketToServerPublic));
                }
                System.out.println("End of reading in Client.ReadFromServer");

            } catch (IOException e) {
                System.out.println(e.getMessage());
            } finally {
                try {
                    clientSocketToServerPublic.close();
                } catch (IOException e) {
                    System.out.println(e);
                }

                try {
                    in.close();
                } catch (IOException e) {
                    System.out.println(e);
                }

                try {
                    out.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }
    }

    //Classe permettant de threader l'envoie de packet au server
    public class WriteToServer implements Runnable{

        //TODO faire les trucs de l'inteface graphique ici
        public void run(){

            Scanner scanner = new Scanner(System.in);
            while(nodeIsRunning) {
                System.out.println("Scanner read next line");
                System.out.println(scanner.nextLine());
            }

        }
    }

    private void redirectToHandler(PeerMessage message, Node node, PeerConnection connection) {
        RSAHandler RSA;

        //handle message
        if(message.getType().equals(MessageType.DHR1)){
            RSA = node.getTempRSAInfo();
            if(RSA != null) {
                RSA.sendEncryptedKey(node, message);
                node.setTempRSAInfo(null);
            }
        }
        else if (message.getType().equals(MessageType.DHS1)){
            try {
                RSA = new RSAHandler();
                RSA.setKeys();
                RSA.sendRSAPublicKey(node, RSA.getPublicKey(), message);
                node.setTempRSAInfo(RSA);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            }
        }
        else if(message.getType().equals(MessageType.DHS2)){
            node.setKey(node.getTempRSAInfo().getFinalKey(message));
            node.setTempRSAInfo(null);
            System.out.println("final key is : " + new String(node.getKey()));
        }
        else {
            node.getMapMessage().get(message.getType()).handleMessage(connection, message); //gerer erreur possible
        }
    }

   /* private void sendFileToPeer(dest, file) {
        sendToserver(send to file dest);
        recupip();
        try send to this ip;
        try catch() {send through server socket}
    }*/

}
