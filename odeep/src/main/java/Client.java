import Node.FileSharingNode;
import Node.Node;
import handler.RSAHandler;
import handler.SFILHandler;
import handler.SMESHandler;
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

    public static void main(String[] args) {

        //Check si première ouverture de l'app
        //Si première ouverture -> demander pseudo

        //Sinon, on peut le pseudo dans le fichier de config

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

        System.out.println(localIP);


        PeerInformations myInfos = new PeerInformations("MON PSEUDO WESH", localIP, LOCAL_PORT);
        n = new FileSharingNode(myInfos);
        //Ajouter tous les handlers
        //listening for incoming connections
        n.acceptingConnections();


        //connection au server publique
        Client c = new Client();
        c.initConnections(IP_SERVER, PORT_SERVER);

    }

    //initialise la connection avec le serveur et de lancer le server d'écoute du client
    public void initConnections(String ip, int port) {
        try {
            //clientSocketToServerPublic = new PeerConnection(new Socket(ip,port));
            clientSocketToServerPublic = new Socket(ip,port);
            in = new BufferedInputStream(clientSocketToServerPublic.getInputStream());
            out = new BufferedOutputStream(clientSocketToServerPublic.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(new ConnectionServer()).start();
    }

    //initialise la connection au serveur avec une thread pour la lecture et l'autre pour l'écriture
    private class ConnectionServer implements Runnable{
        public ConnectionServer(){
        }
        public void run(){
            new Thread(new ReadFromServer()).start();
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

            while(nodeIsRunning) {
                Scanner scanner = new Scanner(System.in);
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

}
