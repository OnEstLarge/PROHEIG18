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
                        System.out.println("Application Closed by click to Close Button(X)");
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

    public static RootLayoutController getController(){
        return controller;
    }

    /**
     * Vérifie dans le fichier '.userInfo' si l'utilisateur possède déjà un pseudo.
     *
     * @return le pseudo de l'utilisateur.
     *         null, si l'utilisateur ne possède pas encorede pseudo.
     */
    private static String usernameExists() {
        String username = null;
        final String userFilename = ".userInfo";
        File userFile = new File("./" + userFilename);

        if(userFile.exists() && !userFile.isDirectory()) {
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

    public void initRootLayout() {
        if ((myUsername = usernameExists()) == null) {
            boolean ok = showPseudoDialog();
            while(!ok){ // Ask for a pseudo until a valid one is entered.
                ok = showPseudoDialog();
            }
            //write file .userInfo with the valid username
            writeToFile(new File("./.userInfo"), myUsername);
        }
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Client.class.getResource("/views/RootLayout.fxml"));
            rootLayout = loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.getIcons().add(image);

            // Give the controller access to the main app.
            controller = loader.getController();
            controller.setMainApp(this);

            while(groupsNotInialized) {
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

    public boolean showInviteDialog() {
        try {
            // Load the FXML filer and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Client.class.getResource("/views/InviteDialog.fxml"));
            AnchorPane page = loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Inviter un membre");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the invite controller
            InviteDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage, image);

            // Clear comboBox and put groups name
            controller.clearCombo();
            for(Group g : groups) {
                controller.addGroupNameToCombo(g.getID());
            }
            // Show the dialog and wait  until the user closes it
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean showPseudoDialog(){
        try{
            // Load the FXML filer and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Client.class.getResource("/views/PseudoDialog.fxml"));
            AnchorPane page = loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Choisissez votre nom");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the invite controller
            PseudoDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage, image);
            controller.setMainApp(this);

            // Show the dialog and wait  until the user closes it
            dialogStage.showAndWait();

            return controller.isNameOK();
        }catch(IOException e){
            e.printStackTrace();
            return false;
        }
    }


    public static boolean showAcceptInviteDialog(String idFrom, String groupName){
        try {
            // Load the FXML filer and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Client.class.getResource("/views/AcceptInviteDialog.fxml"));
            AnchorPane page = loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Invitation dans un groupe");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the invite controller
            AcceptInviteDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.getMessageLabel().setText("Vous avez été invité dans le groupe " + groupName);

            controller.setGroupID(groupName);
            controller.setIdFrom(idFrom);

            // Show the dialog and wait  until the user closes it
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }
    /**
     * Returns the main stage.
     *
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setUserPseudo(String pseudo) {
        myUsername = pseudo;
    }

    public List<Group> getGroups(){
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

        while(!communicationReady) {
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
            out.write(availaibleUsername.getFormattedMessage());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(isUsernameAvailaible == -1) {
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
            out.write(availaibleGroupID.getFormattedMessage());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while(waitingForGroupValidation) {
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
            System.out.println("aaaa" +localIP);
            PeerMessage greetings = new PeerMessage(MessageType.HELO, "XXXXXX", myUsername, "XXXXXX", 0, localIP.getBytes());
            out.write(greetings.getFormattedMessage());
            out.flush();


        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.out.println("Could not connect to the server");
            return;
        }

        new Thread(new ReadFromServer()).start();


        // Initialize the client node
        initNode();

        // Restore existing groups
        for(String groupID : scanGroups()) {
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

        for(Group group: groups){
            System.out.println("-----------------------------------------------------------------------------------------------------"+myself.isConnected());
            for(Person p: group.getMembers()) {
                System.out.println(p.getID() + "   bnbnbnbnb   "+p.isConnected());
                if(p.getID().equals(myUsername)) {
                    p.connect();
                }
                System.out.println(p.getID() + "   bnbnbnbnb   "+p.isConnected());
            }
            System.out.println("upddateeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
            JSONUtil.updateConfig(group);
            uploadJSON("./shared_files/" + group.getID() + "/config.json", group.getID(), myUsername);
        }

        groupsNotInialized = false;
        //controller.updateGroupsAndFiles();
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                controller.updateGroupsAndFiles();
            }
        });
    }

    //Classe permettant de threader la lecture des packets server
    private static class ReadFromServer implements Runnable {
        public void run() {
            int read;
            byte[] buffer = new byte[4096];

            System.out.println("Start reading in main.Client.ReadFromServer");
            try {
                while ((read = in.read(buffer)) != -1) {

                    PeerMessage pm = new PeerMessage(buffer);
                    System.out.println("type message received = " + pm.getType());

                    if (pm.getType().equals(MessageType.INFO)) {
                        System.out.println("Received info, writing in response static");
                        response = new String(CipherUtil.erasePadding(pm.getMessageContent(), PeerMessage.PADDING_START));
                    } else if(pm.getType().equals(MessageType.NEWG)) {
                        String resp = new String(CipherUtil.erasePadding(pm.getMessageContent(), PeerMessage.PADDING_START));
                        validationGroup = resp.equals("true") ? true: false;
                        waitingForGroupValidation = false;
                    } else if(pm.getType().equals(MessageType.DOWN)) {
                        System.out.println("i'm in");
                        saveReceivedJson(pm);
                        waitingJsonFromServer = false;
                        System.out.println("i'm out");
                    } else {
                        System.out.println("Client redirect message " + pm.getType());
                        final PeerMessage redirectPM = pm;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                redirectToHandler(redirectPM, n, new PeerConnection(clientSocketToServerPublic));
                            }
                        }).start();
                    }

                }
                System.out.println("End of reading in main.Client.ReadFromServer");

            } catch (IOException e) {
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
        node.getMapMessage().get(message.getType()).handleMessage(node, connection, message); //gerer erreur possible
    }

    private static String askForInfos(String pseudo) {
        PeerMessage askInfo = new PeerMessage(MessageType.INFO, "XXXXXX", myUsername, myUsername, "".getBytes());
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

    public static String getUsername() {
        return myUsername;
    }

    public static boolean createGroup(String groupID) {
        waitingForGroupValidation = true;
        Group group = InterfaceUtil.createGroup(groupID, Client.getUsername(), n);
        if(group != null) {
            groups.add(group);
        }
        return group != null;
    }


    public static void uploadJSON(String filenameJSON, String groupID, String idFrom) {
        //TODO tester validité des paramètres

        PeerMessage uploadMessage = null;

        try {

            // Récupère et chiffre de fichier config.json
            RandomAccessFile configFile = new RandomAccessFile(filenameJSON, "r");
            byte[] configFileByte = new byte[(int) configFile.length()];
            configFile.readFully(configFileByte);

            String sizeJson = "" + configFileByte.length;
            uploadMessage = new PeerMessage(MessageType.UPLO, groupID, idFrom, idFrom, sizeJson.getBytes());
            // Averti le serveur qu'un upload va être effectué
            out.write(uploadMessage.getFormattedMessage());

            // Upload le config.json chiffré au serveur
            out.write(configFileByte, 0, configFileByte.length);
            out.flush();


            //byte[] decipherConfigFile = CipherUtil.AESDecrypt(configFileByte, n.getKey(groupID));
            //System.out.println("Fichier dechiffré: " + new String(decipherConfigFile));
            //Group group = JSONUtil.parseJson(new String(decipherConfigFile), Group.class);

            broadcastUpdate(idFrom, groupID);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Group getGroupById(String id) {
        Group group = null;
        for(Group g: groups) {
            if(g.getID().equals(id)) {
                group = g;
            }
        }
        return group;
    }

    public static void broadcastUpdate(String idFrom, String groupID) {
        Group group = getGroupById(groupID);
        if(group != null) {
            for (Person person : group.getMembers()) {
                if (!person.getID().equals(myUsername)) {
                    System.out.println("FUCK YOU " +person.getID() + person.getID().length() + "  " + myUsername + myUsername.length());
                    PeerMessage pm = new PeerMessage(MessageType.UPDT, group.getID(), idFrom, person.getID(), "".getBytes());
                    try {
                        out.write(pm.getFormattedMessage());
                        out.flush();
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
            System.out.println("I want to download");
            // Averti le serveur que le client désire avoir le fichier 'config.json'
            out.write(downloadMessage.getFormattedMessage());
            out.flush();
            waitingJsonFromServer = true;

            while(waitingJsonFromServer){
                try {
                    System.out.println("waiting download");
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if(groups != null){
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
            fout.write(buffer,0,size);
            fout.flush();

        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            if(fout != null) {
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

        for(String group : groups) {
            System.out.println(group);
        }
        return groups;
    }

    public static void inviteNewMember(String username, String groupID) {

        if(getGroupById(groupID).getMember(username) == null) {

            PeerMessage invitePM = new PeerMessage(MessageType.INVI, groupID, myUsername, username, "".getBytes());

            try {
                System.out.println("I send an invitation for user " + username + " in group " + groupID);
                out.write(invitePM.getFormattedMessage());
                out.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void acceptInvite(String usernameFrom, String groupID) {
        PeerMessage acceptInvitePM = new PeerMessage(MessageType.INVK, groupID, myUsername, usernameFrom, "".getBytes());

        try {
            System.out.println("I accept an invitation for user " + usernameFrom + " in group " + groupID);
            out.write(acceptInvitePM.getFormattedMessage());
            out.flush();

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

            configFile = new RandomAccessFile("./shared_files/"+groupID+"/config.json", "r");
            byte[] configFileByte = new byte[(int) configFile.length()];
            configFile.readFully(configFileByte);

            byte[] plainConfig = CipherUtil.AESDecrypt(configFileByte, n.getKey(groupID));

            Group group = JSONUtil.parseJson(new String(plainConfig), Group.class);
            group.addMember(myself);
            groups.add(group);

            System.out.println("updateJsonAfterInvitation Group member, should have added myself ");
            for(Person p : group.getMembers())
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
            out.write(pm.getFormattedMessage());
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void refresh(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                controller.updateGroupsAndFiles();
            }
        });
    }

    private static void updateGroupsWithJson(String groupID) {
        int index = groups.indexOf(getGroupById(groupID));
        if(index >= 0) {
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

    public static void requestFile(String file, String group){
        n.requestFile(file, group);
    }
}