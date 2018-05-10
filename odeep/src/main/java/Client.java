import Node.FileSharingNode;
import handler.BYEHandler;
import handler.RSAHandler;
import handler.SFILHandler;
import handler.SMESHandler;
import message.MessageType;
import peer.PeerInformations;
import peer.PeerMessage;

import java.io.*;
import java.net.*;
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
            clientSocketToServerPublic = new Socket(ip, port);
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

            try {
                while ((read = in.read(buffer)) != -1) {
                    PeerMessage pm = new PeerMessage(buffer);
                    String type = pm.getType();


                }

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

        }
    }

}
