package main;

import Node.Node;
import User.Person;
import handler.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.lang.reflect.Member;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javafx.stage.WindowEvent;
import message.MessageType;
import org.bouncycastle.crypto.InvalidCipherTextException;
import peer.PeerConnection;
import peer.PeerInformations;
import peer.PeerMessage;
import util.CipherUtil;
import util.InterfaceUtil;
import util.JSONUtil;
import views.AcceptInviteDialogController;
import views.InviteDialogController;
import views.PseudoDialogController;
import views.RootLayoutController;
import User.Group;


public class Client extends Application {

    private static Stage primaryStage;
    private BorderPane rootLayout;
    private static RootLayoutController controller;
    private Image image = new Image(getClass().getResourceAsStream("logo.png"));

    /**
     * Lance l'affichage de la fenêtre initiale et défini le nom de la fenêtre.
     * @param primaryStage
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Odeep");
        this.primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        System.exit(0);
                    }
                });
            }
        });
       /* Platform.runLater(new Runnable() {
            @Override
            public void run() {

                System.out.println("xxxxXXXxxxxx");
                showAcceptInviteDialog("a");
            }
        });*/
        initRootLayout();
    }

    public static RootLayoutController getController() {
        return controller;
    }

    /**
     * Vérifie dans le fichier '.userInfo' si l'utilisateur possède déjà un pseudo.
     *
     * @return le pseudo de l'utilisateur.
     * null, si l'utilisateur ne possède pas encorede pseudo.
     */
    private static String usernameExists() {
        String username = null;
        final String userFilename = ".userInfo";
        File userFile = new File("./" + userFilename);

        if (userFile.exists() && !userFile.isDirectory()) {
            username = readFromFile(userFile);
            username = username.replaceAll("[^A-Za-z0-9]", ""); //remove all non aplhanumeric character
        }

        return username;
    }

    private static String readFromFile(File file) {
        StringBuilder stringBuilder = new StringBuilder();
        FileInputStream fileInputStream = null;
        BufferedReader bufferedReader = null;
        try {
            fileInputStream = new FileInputStream(file);
            bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return stringBuilder.toString();
    }

    private static void writeToFile(File file, String data) {
        PrintWriter writer = null;
        try {

            writer = new PrintWriter(file, "UTF-8");
            writer.print(data);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            writer.close();
        }
    }

    /**
     * Initialise puis affiche la fenêtre initiale.
     * Si jamais le fichier .userInfo n'est pas trouvé, affiche la fenêtre demandant un nom d'utilisateur puis
     * crée le fichier .userInfo avec le nom reçu.
     */
    public void initRootLayout() {
        if ((myUsername = usernameExists()) == null) {
            boolean ok = showPseudoDialog();
            while (!ok) { // Demande un nom jusqu'à ce qu'il soit correct
                ok = showPseudoDialog();
            }

            // Ecrit le fichier .userInfo avec le bon nom d'utilisateur
            writeToFile(new File("./.userInfo"), myUsername);
        }
        try {
            // Charge l'interface initiale depuis un fichier .fxml
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Client.class.getResource("/views/RootLayout.fxml"));
            rootLayout = loader.load();

            // Montre la scene contenant l'interface initiale
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.getIcons().add(image);

            // Donne au controller accès à l'application principale
            controller = loader.getController();
            controller.setMainApp(this);

            while (groupsNotInialized) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            controller.updateGroupsAndFiles();

            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Affiche la fenêtre d'invitation.
     * @return true si jamais le bouton OK a été appuyé, false sinon
     */
    public boolean showInviteDialog() {
        try {
            // Charge le fichier .fxml et crée la nouvelle scene pour la fenêtre d'invitation
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Client.class.getResource("/views/InviteDialog.fxml"));
            AnchorPane page = loader.load();

            // Crée la scène de dialogue
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Inviter un membre");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Défini le controleur d'invitation
            InviteDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage, image);

            // Vide le menu déroulant et le rempli avec les noms corrects
            controller.clearCombo();
            for (Group g : groups) {
                controller.addGroupNameToCombo(g.getID());
            }
            // Affiche la fenêtre et attend qu'elle se ferme
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Affiche la fenêtre demandant à l'utilisateur de choisir un nom d'utilisateur
     * @return true si jamais le nom est correct, false sinon
     */
    public boolean showPseudoDialog() {
        try {
            // Charge le fichier .fxml et crée la nouvelle scene pour la fenêtre d'invitation
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Client.class.getResource("/views/PseudoDialog.fxml"));
            AnchorPane page = loader.load();

            // Crée la scène de dialogue
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Choisissez votre nom");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Défini le controleur de la fenêtre pseudo
            PseudoDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage, image);
            controller.setMainApp(this);

            // Affiche la fenêtre et attend qu'elle se ferme
            dialogStage.showAndWait();

            return controller.isNameOK();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Affiche un message lorsqu'une invitation à rejoindre un groupe est reçue
     * @param idFrom    Le nom de l'utilisateur étant à la source de l'invitation
     * @param groupName Le nom du groupe dans lequel on a été invité
     * @return true si jamais la fenêtre le bouton OK a été appuyé, false sinon
     */
    public static boolean showAcceptInviteDialog(String idFrom, String groupName) {
        try {
            // Charge le fichier .fxml et crée la nouvelle scene pour la fenêtre d'invitation
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Client.class.getResource("/views/AcceptInviteDialog.fxml"));
            AnchorPane page = loader.load();

            // Crée la scène de dialogue
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Invitation dans un groupe");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Défini le controleur de la fenêtre pour accépter l'invitation
            AcceptInviteDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.getMessageLabel().setText("Vous avez été invité dans le groupe " + groupName);

            controller.setGroupID(groupName);
            controller.setIdFrom(idFrom);

            // Affiche la fenêtre et attend qu'elle se ferme
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setUserPseudo(String pseudo) {
        myUsername = pseudo;
    }

    public List<Group> getGroups() {
        return groups;
    }



    private static final String IP_SERVER = "192.168.0.46";//"206.189.49.105";
    private static final int PORT_SERVER = 8080;
    private static final int LOCAL_PORT = 4444;

    private static Socket clientSocketToServerPublic;
    private static BufferedInputStream in;
    private static BufferedOutputStream out;
    private static boolean communicationReady = false;
    private static int isUsernameAvailaible = -1;
    private static boolean waitingForGroupValidation = false;
    private static boolean validationGroup = false;
    private static boolean waitingJsonFromServer = false;
    private static boolean groupsNotInialized = true;

    private static Node n;
    private static boolean nodeIsRunning = true;
    private static String myUsername = null;
    private static String localIP;

    private static String response = null;

    private static List<Group> groups = new ArrayList();
    public static Person myself;//set in getPseudo

    private static int count = 0;
    private static HashMap<Integer, Boolean> mutex = new HashMap();


    public static void main(String[] args) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                getLocalIP();
                System.out.println("Connecting to server");
                //initconnection connect to serv, get pseudo, connect with pseudo
                initConnections(IP_SERVER, PORT_SERVER);
                //for(Group g : groups)
                // InterfaceUtil.printConfig(g.getID(), n.getKey(g.getID()));
                //listening for incoming connections
                System.out.println("Launching node listening");
                n.acceptingConnections();


            }
        }).start();

        launch(args);
    }


    private static void getPseudo() {

        //Sinon, on peut le pseudo dans le fichier de config
        while (myUsername == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        myself = new Person(myUsername);
        myself.connect();
    }

    //ask the server if the entered username is available
    public static boolean usernameValidation(String username) {

        isUsernameAvailaible = -1;

        while (!communicationReady) {
            try {
                System.out.println("waiting comm ready");
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //une fois que la connection avec le serveur est établit, il faut demander si le pseudo entré est déjà utilisé
        //retourne true si le pseudo est libre, false si il est déjà utilisé

        PeerMessage availaibleUsername = new PeerMessage(MessageType.USRV, "XXXXXX", username, "XXXXXX", 0, "".getBytes());

        try {
            synchronized (out) {
                out.write(availaibleUsername.getFormattedMessage());
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (isUsernameAvailaible == -1) {
            try {

                int read;
                byte[] buffer = new byte[4096];
                read = in.read(buffer);
                PeerMessage pm = new PeerMessage(buffer);
                System.out.println("Received response for username validation");
                String resp = new String(CipherUtil.erasePadding(pm.getMessageContent(), PeerMessage.PADDING_START));
                isUsernameAvailaible = resp.equals("true") ? 1 : 0;
                if (isUsernameAvailaible != 1) {
                    Thread.sleep(1000);
                    System.out.println("isUsernameAvailaible " + isUsernameAvailaible);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return isUsernameAvailaible == 1;
    }

    public static boolean groupValidation(String groupID) {
        PeerMessage availaibleGroupID = new PeerMessage(MessageType.NEWG, groupID, myUsername, "XXXXXX", 0, "".getBytes());

        try {
            synchronized (out) {
                out.write(availaibleGroupID.getFormattedMessage());
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (waitingForGroupValidation) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return validationGroup;
    }

    private static void getLocalIP() {
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
    }

    private static void initNode() {
        PeerInformations myInfos = new PeerInformations(myUsername, localIP, LOCAL_PORT);
        System.out.println("Created myInfos");
        n = new Node(myInfos);
        System.out.println("Created the node");
        //Ajouter tous les handlers
        n.addMessageHandler(MessageType.INVI, new INVIHandler());
        n.addMessageHandler(MessageType.DISC, new DISCHandler());
        n.addMessageHandler(MessageType.NFIL, new NFILHandler());
        n.addMessageHandler(MessageType.RFIL, new RFILHandler());
        n.addMessageHandler(MessageType.PGET, new PGETHandler());
        n.addMessageHandler(MessageType.SFIL, new SFILHandler());
        n.addMessageHandler(MessageType.SMES, new SMESHandler());
        n.addMessageHandler(MessageType.UPDT, new UPDTHandler());
        n.addMessageHandler(MessageType.INVK, new INVKHandler());
        n.addMessageHandler(MessageType.DHS1, new DHS1Handler());
        n.addMessageHandler(MessageType.DHS2, new DHS2Handler());
        n.addMessageHandler(MessageType.DHR1, new DHR1Handler());

        System.out.println("Added the handlers");
    }

    //initialise la connection avec le serveur et de lancer le server d'écoute du client
    public static void initConnections(String ip, int port) {
        try {
            //clientSocketToServerPublic = new PeerConnection(new Socket(ip,port));
            clientSocketToServerPublic = new Socket(ip, port);
            System.out.println("Connected to server");
            in = new BufferedInputStream(clientSocketToServerPublic.getInputStream());
            out = new BufferedOutputStream(clientSocketToServerPublic.getOutputStream());
            communicationReady = true;
            //we have a pseudo after this
            System.out.println("get pseudo");
            getPseudo();
            System.out.println("we got the pseudo");

            //Greetings to server, receivinig response
            System.out.println("aaaa" + localIP);
            PeerMessage greetings = new PeerMessage(MessageType.HELO, "XXXXXX", myUsername, "XXXXXX", 0, localIP.getBytes());
            synchronized (out) {
                out.write(greetings.getFormattedMessage());
                out.flush();
            }


        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.out.println("Could not connect to the server");
            return;
        }

        new Thread(new ReadFromServer()).start();


        // Initialize the client node
        initNode();

        // Restore existing groups
        for (String groupID : scanGroups()) {
            downloadJSON(groupID);

            // Read config file
            try {
                RandomAccessFile configFile = new RandomAccessFile("./shared_files/" + groupID + "/config.json", "r");
                byte[] configFileByte = new byte[(int) configFile.length()];
                configFile.readFully(configFileByte);
                String configJson = new String(CipherUtil.AESDecrypt(configFileByte, n.getKey(groupID)));

                groups.add((Group) JSONUtil.parseJson(configJson, Group.class));

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidCipherTextException e) {
                e.printStackTrace();
            }
        }

        connectMyself(true);

        groupsNotInialized = false;
        //controller.updateGroupsAndFiles();
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                controller.updateGroupsAndFiles();
            }
        });
    }

    private static void connectMyself(boolean connectMyself) {
        for (Group group : groups) {
            for (Person p : group.getMembers()) {
                if (p.getID().equals(myUsername)) {
                    if(connectMyself){
                        p.connect();
                    } else {
                        p.disconnect();
                    }
                }
            }
            JSONUtil.updateConfig(group);
            uploadJSON("./shared_files/" + group.getID() + "/config.json", group.getID(), myUsername);
        }
    }

    //Classe permettant de threader la lecture des packets server
    private static class ReadFromServer implements Runnable {
        public void run() {
            //int read;
            byte[] buffer = new byte[4096];

            System.out.println("Start reading in main.Client.ReadFromServer");
            try {
                synchronized (in) {
                    while(true) {
                        int read = 0;
                        while(read != PeerMessage.BLOCK_SIZE) {
                            read += in.read(buffer, read, buffer.length - read);
                        }
                        //}
                        //while ((read = in.read(buffer, 0, 4096)) != -1) {

                        PeerMessage pm = new PeerMessage(buffer);
                        if (read != 4096)
                            System.out.println(read);
                        //System.out.println("type message received = " + pm.getType());

                        if (pm.getType().equals(MessageType.INFO)) {
                            System.out.println("Received info, writing in response static");
                            response = new String(CipherUtil.erasePadding(pm.getMessageContent(), PeerMessage.PADDING_START));
                        } else if (pm.getType().equals(MessageType.NEWG)) {
                            String resp = new String(CipherUtil.erasePadding(pm.getMessageContent(), PeerMessage.PADDING_START));
                            validationGroup = resp.equals("true") ? true : false;
                            waitingForGroupValidation = false;
                        } else if (pm.getType().equals(MessageType.DOWN)) {
                            System.out.println("i'm in");
                            saveReceivedJson(pm);
                            waitingJsonFromServer = false;
                            System.out.println("i'm out");
                        } else {
                            //System.out.println("Client redirect message " + pm.getType());
                            final PeerMessage redirectPM = new PeerMessage(pm);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    redirectToHandler(redirectPM, n, new PeerConnection(clientSocketToServerPublic));
                                }
                            }).start();
                        }
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //}
                    }
                    //System.out.println("End of reading in main.Client.ReadFromServer");

                }
            }catch (IOException e) {
                System.out.println(e.getMessage());
            } finally {
                System.out.println("ERROR STOPPED LISTENING TO SERVER PUBLIC");
                try {
                    clientSocketToServerPublic.close();
                    in.close();
                    out.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }
    }

    private static void redirectToHandler(PeerMessage message, Node node, PeerConnection connection) {
        //handle message
        try {
            node.getMapMessage().get(message.getType()).handleMessage(node, connection, message); //gerer erreur possible
        } catch (NullPointerException e) {
            System.out.println("ERREUR");
            System.out.println("type : " + message.getType() + "\ngroupe : " + message.getIdGroup() + "\nfrom : " + message.getIdFrom() + "\nto : " + message.getIdTo() + "\nNo : " + message.getNoPacket());
        }
    }

    private static String askForInfos(String pseudo) {
        PeerMessage askInfo = new PeerMessage(MessageType.INFO, "XXXXXX", myUsername, myUsername, "".getBytes());
        try {
            synchronized (out) {
                out.write(askInfo.getFormattedMessage());
                out.flush();
            }
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

    public static String getUsername() {
        return myUsername;
    }

    public static boolean createGroup(String groupID) {
        waitingForGroupValidation = true;
        Group group = InterfaceUtil.createGroup(groupID, Client.getUsername(), n);
        if (group != null) {
            groups.add(group);
        }
        return group != null;
    }


    public static void uploadJSON(String filenameJSON, String groupID, String idFrom) {
        //TODO tester validité des paramètres

        PeerMessage uploadMessage = null;

        try {
            System.out.println("UPLOADING will read file");
            // Récupère et chiffre de fichier config.json
            RandomAccessFile configFile = new RandomAccessFile(filenameJSON, "r");
            byte[] configFileByte = new byte[(int) configFile.length()];
            configFile.readFully(configFileByte);
            System.out.println("UPLOADING did read file");
            uploadMessage = new PeerMessage(MessageType.UPLO, groupID, idFrom, idFrom, configFileByte);
            System.out.println("UPLOAD " + new String(uploadMessage.getFormattedMessage()));
            synchronized (out) {
                out.write(uploadMessage.getFormattedMessage());
                out.flush();
            }

            broadcastUpdate(idFrom, groupID);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Group getGroupById(String id) {
        Group group = null;
        for (Group g : groups) {
            if (g.getID().equals(id)) {
                group = g;
            }
        }
        return group;
    }

    public static void broadcastUpdate(String idFrom, String groupID) {
        Group group = getGroupById(groupID);
        if (group != null) {
            for (Person person : group.getMembers()) {
                if (!person.getID().equals(myUsername)) {
                    System.out.println("FUCK YOU " + person.getID() + person.getID().length() + "  " + myUsername + myUsername.length());
                    PeerMessage pm = new PeerMessage(MessageType.UPDT, group.getID(), idFrom, person.getID(), "".getBytes());
                    try {
                        synchronized (out) {
                            out.write(pm.getFormattedMessage());
                            out.flush();
                        }
                        //Thread.sleep(100);
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    } /*catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                }

            }
        }
    }

    public static void downloadJSON(String groupID) {

        System.out.println("Download JSON");
        PeerMessage downloadMessage = new PeerMessage(MessageType.DOWN, groupID, myUsername, myUsername, "".getBytes());
        try {
            waitingJsonFromServer = true;
            while(waitingJsonFromServer) {
                System.out.println("I want to download");
                // Averti le serveur que le client désire avoir le fichier 'config.json'
                synchronized (out) {
                    out.write(downloadMessage.getFormattedMessage());
                    out.flush();
                }
                int count = 0;

                while (waitingJsonFromServer && count < 20) {
                    try {
                        System.out.println("waiting download");
                        Thread.sleep(100);
                        count++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (groups != null) {
            updateGroupsWithJson(groupID);
        }

        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                controller.updateGroupsAndFiles();
            }
        });
    }

    private static void saveReceivedJson(PeerMessage pm) {
        byte[] buffer = pm.getMessageContent();
        int size = pm.getMessageContent().length;
        System.out.println(size);
        FileOutputStream fout = null;
        try {
            System.out.println("I download");
            fout = new FileOutputStream(new File("./shared_files/" + pm.getIdGroup() + "/config.json"));
            fout.write(buffer, 0, size);
            fout.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String[] scanGroups() {
        File directory = new File("./shared_files");
        String[] groups = directory.list(new FilenameFilter() {

            @Override
            public boolean accept(File file, String name) {
                return new File(file, name).isDirectory();
            }
        });

        for (String group : groups) {
            System.out.println(group);
        }
        return groups;
    }

    public static void inviteNewMember(String username, String groupID) {

        if (getGroupById(groupID).getMember(username) == null) {

            PeerMessage invitePM = new PeerMessage(MessageType.INVI, groupID, myUsername, username, "".getBytes());

            try {
                System.out.println("I send an invitation for user " + username + " in group " + groupID);
                synchronized (out) {
                    out.write(invitePM.getFormattedMessage());
                    out.flush();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void acceptInvite(String usernameFrom, String groupID) {
        PeerMessage acceptInvitePM = new PeerMessage(MessageType.INVK, groupID, myUsername, usernameFrom, "".getBytes());

        try {
            System.out.println("I accept an invitation for user " + usernameFrom + " in group " + groupID);
            synchronized (out) {
                out.write(acceptInvitePM.getFormattedMessage());
                out.flush();
            }

            // Crée le groupe localement
            String dir = "./shared_files/" + groupID;
            File file = new File(dir);
            if (!file.exists() || !file.isDirectory()) {
                file.mkdirs();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateJsonAfterInvitation(String groupID) {


        System.out.println("updateJsonAfterInvitation in");

        //download json du groupe
        downloadJSON(groupID);
        //ajoute le group dans sa liste groups
        System.out.println("updateJsonAfterInvitation downloaded");

        RandomAccessFile configFile = null;
        try {
            System.out.println("updateJsonAfterInvitation will read json received");

            configFile = new RandomAccessFile("./shared_files/" + groupID + "/config.json", "r");
            byte[] configFileByte = new byte[(int) configFile.length()];
            configFile.readFully(configFileByte);

            byte[] plainConfig = CipherUtil.AESDecrypt(configFileByte, n.getKey(groupID));

            Group group = JSONUtil.parseJson(new String(plainConfig), Group.class);
            group.addMember(myself);
            groups.add(group);

            System.out.println("updateJsonAfterInvitation Group member, should have added myself ");
            for (Person p : group.getMembers())
                System.out.println(p.getID());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
        }

        System.out.println("updateJsonAfterInvitation update config");

        //update le json en s'ajoutant dans le groupe
        JSONUtil.updateConfig(Client.getGroupById(groupID));

        System.out.println("updateJsonAfterInvitation updated, uploading");

        //upload le nouveau json sur le serv
        Client.uploadJSON("./shared_files/" + groupID + "/config.json", groupID, myUsername);

        System.out.println("updateJsonAfterInvitation uploaded");

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                controller.enableButtons();
                controller.updateGroupsAndFiles();
            }
        });
    }

    public static void sendPM(PeerMessage pm) {
        try {
            synchronized (out) {
                out.write(pm.getFormattedMessage());
                out.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void refresh() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                controller.updateGroupsAndFiles();
            }
        });
    }

    private static void updateGroupsWithJson(String groupID) {
        int index = groups.indexOf(getGroupById(groupID));
        if (index >= 0) {
            RandomAccessFile configFile = null;
            try {
                configFile = new RandomAccessFile("./shared_files/" + groupID + "/config.json", "r");
                byte[] configFileByte = new byte[(int) configFile.length()];
                configFile.readFully(configFileByte);

                byte[] plainConfig = CipherUtil.AESDecrypt(configFileByte, n.getKey(groupID));

                Group group = JSONUtil.parseJson(new String(plainConfig), Group.class);
                groups.set(index, group);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidCipherTextException e) {
                e.printStackTrace();
            }
        }
    }

    public static void requestFile(String file, String group) {
        n.requestFile(file, group);
    }

    public static void updateDownloadBar(double value) {
        final double v = value;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                controller.updateDownloadBar(v);
                if(Math.abs(v - 1.0) < 0.001) {
                    controller.enableDownLoad();
                }
            }
        });

    }

    public static void updateUploadBar(double value) {
        final double v = value;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                controller.updateUploadBar(v);
            }
        });
    }

    public static void clearUploadBar() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                controller.clearUploadBar();
            }
        });
    }
}