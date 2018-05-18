package main;

import Node.FileSharingNode;
import Node.Node;
import User.Group;
import User.Person;
import handler.*;
import message.MessageType;
import peer.PeerConnection;
import peer.PeerInformations;
import peer.PeerMessage;
import util.CipherUtil;
import util.JSONUtil;

import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Enumeration;

public class Client {
    private static final String IP_SERVER = "192.168.0.214";//"206.189.49.105";
    private static final int PORT_SERVER = 8080;
    private static final int LOCAL_PORT = 4444;

    private static Socket clientSocketToServerPublic;
    private static BufferedInputStream in;
    private static BufferedOutputStream out;

    private static FileSharingNode n;
    private static boolean nodeIsRunning = true;
    private static String myPseudo;
    private static String localIP;

    private static String response = null;


    public static void main(String[] args) {

        //Check si première ouverture de l'app
        //Si première ouverture -> demander pseudo

        //Sinon, on peut le pseudo dans le fichier de config
        myPseudo = "WESHWESH";

        //Get local IP used
        localIP = null;
        try {
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();
                while (ee.hasMoreElements()) {
                    Inet4Address i = null;
                    try {
                        i = (Inet4Address) ee.nextElement();
                        if (!i.getHostAddress().endsWith(".1") && !i.getHostAddress().endsWith(".255")) {
                            localIP = i.getHostAddress();
                        }
                    } catch (ClassCastException ex) {
                    }
                }
            }
        } catch (SocketException e) {
        }

        System.out.println("Your local ip: " + localIP);


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
            clientSocketToServerPublic = new Socket(ip, port);
            System.out.println("Connected to server");
            in = new BufferedInputStream(clientSocketToServerPublic.getInputStream());
            out = new BufferedOutputStream(clientSocketToServerPublic.getOutputStream());

            //Greetings to server, receivinig response
            PeerMessage greetings = new PeerMessage(MessageType.HELO, "XXXXXX", myPseudo, "XXXXXX", 0, localIP.getBytes());
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
    private class ConnectionServer implements Runnable {
        public ConnectionServer() {
        }

        public void run() {
            System.out.println("Reading from server start");
            new Thread(new ReadFromServer()).start();
            System.out.println("writing to server start");
            new Thread(new WriteToServer()).start();
        }

    }

    //Classe permettant de threader la lecture des packets server
    private class ReadFromServer implements Runnable {
        public void run() {
            int read;
            byte[] buffer = new byte[4096];

            System.out.println("Start reading in main.Client.ReadFromServer");
            try {
                while ((read = in.read(buffer)) != -1) {

                    PeerMessage pm = new PeerMessage(buffer);
                    //String type = pm.getType();

                    if (pm.getType().equals(MessageType.INFO)) {
                        System.out.println("Received info, writing in response static");
                        response = new String(CipherUtil.erasePadding(pm.getMessageContent(), PeerMessage.PADDING_START));
                    } else {
                        redirectToHandler(pm, n, new PeerConnection(clientSocketToServerPublic));
                    }

                }
                System.out.println("End of reading in main.Client.ReadFromServer");

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
    public class WriteToServer implements Runnable {

        //TODO faire les trucs de l'inteface graphique ici
        public void run() {

            /*Scanner scanner = new Scanner(System.in);
            while(nodeIsRunning) {
                System.out.println("Scanner read next line");
                String resp = scanner.nextLine();
                if(resp.)
            }*/
            //PeerMessage p = new PeerMessage(MessageType.INFO, myPseudo, myPseudo, "XXXXXXX", "".getBytes());
            System.out.println("Asking for info about myself...");
            String a = askForInfos(myPseudo);
            System.out.println("Info reveived: " + a);

            System.out.println("SMESIINNGG");


            PeerMessage smeshing = new PeerMessage(MessageType.SMES, "XXXXXX", myPseudo, myPseudo, "coucou".getBytes());
            try {
                out.write(smeshing.getFormattedMessage());
                out.flush();
            } catch (IOException e) {

            }

        }
    }

    private void redirectToHandler(PeerMessage message, Node node, PeerConnection connection) {
        RSAHandler RSA;

        //handle message
        node.getMapMessage().get(message.getType()).handleMessage(connection, message); //gerer erreur possible
    }

   /* private void sendFileToPeer(dest, file) {
        sendToserver(send to file dest);
        recupip();
        try send to this ip;
        try catch() {send through server socket}
    }*/

    private static String askForInfos(String pseudo) {
        PeerMessage askInfo = new PeerMessage(MessageType.INFO, "XXXXXX", myPseudo, myPseudo, "".getBytes());
        try {
            out.write(askInfo.getFormattedMessage());
            out.flush();
            while (response == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
            String p = response;
            response = null;
            return p;

        } catch (IOException e) {
            return null;
        }
    }

    public static void uploadJSON(String filenameJSON, String groupID, String idFrom) {
        //TODO tester validité des paramètres

        PeerMessage uploadMessage = new PeerMessage(MessageType.UPLO, groupID, idFrom, idFrom, groupID.getBytes());

        try {
            // Averti le serveur qu'un upload va être effectué
            out.write(uploadMessage.getFormattedMessage());
            out.flush();

            // Récupère et chiffre de fichier config.json
            RandomAccessFile configFile = new RandomAccessFile(filenameJSON, "r");
            byte[] configFileByte = new byte[(int) configFile.length()];
            configFile.readFully(configFileByte);

            byte[] cipherConfig = CipherUtil.AESEncrypt(configFileByte, n.getKey(groupID));

            // Upload le config.json chiffré au serveur
            out.write(cipherConfig);
            out.flush();

            Group group = JSONUtil.parseJson(new String(configFileByte), Group.class);
            for (Person person : group.getMembers()) {
                PeerMessage pm = new PeerMessage(MessageType.UPDT, groupID, idFrom, person.getID(), "".getBytes());
                try {
                    Socket localConnection = new Socket(askForInfos(person.getID()), 4444);
                    BufferedOutputStream o = new BufferedOutputStream(localConnection.getOutputStream());
                    o.write(pm.getFormattedMessage());
                    o.flush();
                    o.close();
                    localConnection.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    if (e.getMessage().equals("Connection refused")) {
                        //Si la connexion en locale échoue, on utilise le server relais
                        out.write(pm.getFormattedMessage());
                        out.flush();
                    }
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String downloadJSON(String group) {

        return null;
    }

}
