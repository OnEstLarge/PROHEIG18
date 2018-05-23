package main;

import Node.Node;
import User.Person;
import handler.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import message.MessageType;
import org.bouncycastle.crypto.InvalidCipherTextException;
import peer.PeerConnection;
import peer.PeerInformations;
import peer.PeerMessage;
import util.CipherUtil;
import util.Constant;
import util.InterfaceUtil;
import util.JSONUtil;
import views.AcceptInviteDialogController;
import views.InviteDialogController;
import views.UsernameDialogController;
import views.RootLayoutController;
import User.Group;

import static java.lang.System.exit;


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
        this.primaryStage.setOnCloseRequest(event -> Platform.runLater(() ->
        {
            connectMyself(false);
            exit(0);
        }));
        initRootLayout();
    }

    public static RootLayoutController getController() {
        return controller;
    }

    /**
     * Vérifie dans le fichier '.userInfo' si l'utilisateur possède déjà un nom d'utilisateur.
     *
     * @return le nom de l'utilisateur.
     * null, si l'utilisateur ne possède pas encore de nom.
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
            boolean ok = showUsernameDialog();
            while (!ok) { // Demande un nom jusqu'à ce qu'il soit correct
                ok = showUsernameDialog();
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
    public boolean showUsernameDialog() {
        try {
            // Charge le fichier .fxml et crée la nouvelle scene pour la fenêtre d'invitation
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Client.class.getResource("/views/UsernameDialog.fxml"));
            AnchorPane page = loader.load();

            // Crée la scène de dialogue
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Choisissez votre nom");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Défini le controleur de la fenêtre username
            UsernameDialogController controller = loader.getController();
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

    public void setUsername(String username) {
        myUsername = username;
    }

    public List<Group> getGroups() {
        return groups;
    }




    private static final String IP_SERVER = "206.189.49.105";

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
    private static String myUsername = null;
    private static String localIP;

    private static String response = null;

    private static List<Group> groups = new ArrayList();
    public static Person myself;// Initialisé dans getUsername

    public static void main(String[] args) {

        new Thread(() -> {
            getLocalIP();
            // initConnections établie la connexion avec le serveur, récupere le nom de l'utilisateur et se connecte avec
            initConnections(IP_SERVER, PORT_SERVER);

            // Se met à l'écoute de connexions
            try {
                n.acceptingConnections();
            } catch (NullPointerException e) {
                //Le server n'est pas actif
                exit(0);
            }

        }).start();

        launch(args);
    }


    private static void waitForUsername() {

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

    /**
     * verifie la disponibilité d'un nom d'utilisateur
     * @param username nom d'utilisateur demandé
     * @return true si le nom est disponible, false sinon
     */
    public static boolean usernameValidation(String username) {

        isUsernameAvailaible = -1;

        while (!communicationReady) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //une fois que la connection avec le serveur est établie, il faut demander si le pseudo entré est déjà utilisé
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

                byte[] buffer = new byte[PeerMessage.BLOCK_SIZE];
                in.read(buffer);
                PeerMessage pm = new PeerMessage(buffer);
                String resp = new String(CipherUtil.erasePadding(pm.getMessageContent(), PeerMessage.PADDING_START));
                isUsernameAvailaible = resp.equals("true") ? 1 : 0;
                if (isUsernameAvailaible != 1) {
                    Thread.sleep(1000);
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
        // Récupération de l'adresse Ip locale utilisée
        localIP = null;
        try {
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();
                while (ee.hasMoreElements()) {
                    Inet4Address i;
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
    }

    /**
     * initialise le node, en ajoutant tous les handlers qui lui sont liés
     */
    private static void initNode() {
        PeerInformations myInfos = new PeerInformations(myUsername, localIP, LOCAL_PORT);
        n = new Node(myInfos);
        // Ajouter tous les handlers
        n.addMessageHandler(MessageType.INVI, new INVIHandler());
        n.addMessageHandler(MessageType.DISC, new DISCHandler());
        n.addMessageHandler(MessageType.NFIL, new NFILHandler());
        n.addMessageHandler(MessageType.RFIL, new RFILHandler());
        n.addMessageHandler(MessageType.PGET, new PGETHandler());
        n.addMessageHandler(MessageType.SFIL, new SFILHandler());
        n.addMessageHandler(MessageType.UPDT, new UPDTHandler());
        n.addMessageHandler(MessageType.INVK, new INVKHandler());
        n.addMessageHandler(MessageType.DHS1, new DHS1Handler());
        n.addMessageHandler(MessageType.DHS2, new DHS2Handler());
        n.addMessageHandler(MessageType.DHR1, new DHR1Handler());

    }

    // Initialise la connection avec le serveur et lance le server d'écoute du client

    /**
     * initialisation de la connection avec le serveur et mise à l'écoute du client
     * @param ip ip du serveur
     * @param port port du serveur
     */
    public static void initConnections(String ip, int port) {
        try {
            clientSocketToServerPublic = new Socket(ip, port);
            in = new BufferedInputStream(clientSocketToServerPublic.getInputStream());
            out = new BufferedOutputStream(clientSocketToServerPublic.getOutputStream());
            communicationReady = true;
            //récupération du pseudo
            waitForUsername();

            // Salutation au serveur, reception de la réponse
            PeerMessage greetings = new PeerMessage(MessageType.HELO, "XXXXXX", myUsername, "XXXXXX", 0, localIP.getBytes());
            synchronized (out) {
                out.write(greetings.getFormattedMessage());
                out.flush();
            }


        } catch (IOException e) {
            System.err.println("Could not connect to the server");
            return;
        }

        new Thread(new ReadFromServer()).start();


        // Initialise le noeud du client
        initNode();

        // Restore les groupes existants
        for (String groupID : scanGroups()) {
            downloadJSON(groupID);

            // Lecture du fichier de configuration
            try {
                RandomAccessFile configFile = new RandomAccessFile(Constant.ROOT_GROUPS_DIRECTORY + "/" + groupID + "/" + Constant.CONFIG_FILENAME, "r");
                byte[] configFileByte = new byte[(int) configFile.length()];
                configFile.readFully(configFileByte);
                String configJson = new String(CipherUtil.AESDecrypt(configFileByte, n.getKey(groupID)));

                groups.add(JSONUtil.parseJson(configJson, Group.class));

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidCipherTextException e) {
                e.printStackTrace();
            }
        }

        connectMyself(true);

        groupsNotInialized = false;
        Platform.runLater(() -> controller.updateGroupsAndFiles());
    }

    public static void connectMyself(boolean connectMyself) {
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
            uploadJSON(Constant.ROOT_GROUPS_DIRECTORY + "/" + group.getID() + "/" + Constant.CONFIG_FILENAME, group.getID(), myUsername);
        }
        if(!connectMyself) {
            PeerMessage bye = new PeerMessage(MessageType.EXIT, "XXXXXX", myUsername, myUsername, "".getBytes());
            sendPM(bye);
        }
    }

    /**
     * fonction qui permet de lire les messages envoyés par le serveur et de les traiter
     */
    private static class ReadFromServer implements Runnable {
        public void run() {
            byte[] buffer = new byte[PeerMessage.BLOCK_SIZE];

            try {
                synchronized (in) {
                    while(true) {
                        int read = 0;
                        while(read != PeerMessage.BLOCK_SIZE) {
                            read += in.read(buffer, read, buffer.length - read);
                        }

                        PeerMessage pm = new PeerMessage(buffer);

                        if (pm.getType().equals(MessageType.INFO)) {
                            response = new String(CipherUtil.erasePadding(pm.getMessageContent(), PeerMessage.PADDING_START));
                        } else if (pm.getType().equals(MessageType.NEWG)) {
                            String resp = new String(CipherUtil.erasePadding(pm.getMessageContent(), PeerMessage.PADDING_START));
                            validationGroup = resp.equals("true") ? true : false;
                            waitingForGroupValidation = false;
                        } else if (pm.getType().equals(MessageType.DOWN)) {
                            saveReceivedJson(pm);
                            waitingJsonFromServer = false;
                        } else {
                            final PeerMessage redirectPM = new PeerMessage(pm);
                            new Thread(() -> redirectToHandler(redirectPM, n, new PeerConnection(clientSocketToServerPublic))).start();
                        }
                        //ralentissement de lecture suite à des problèmes de lecture
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            System.err.println(e.getMessage());
                        }
                    }

                }
            }catch (IOException e) {
                System.err.println(e.getMessage());
            } finally {
                System.err.println("ERROR STOPPED LISTENING TO SERVER PUBLIC");
                try {
                    clientSocketToServerPublic.close();
                    in.close();
                    out.close();
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
        }
    }

    /**
     * fonction permettant de transmettre les messages aux bons handlers
     * @param message message recu
     * @param node noeud ayant recu le message
     * @param connection connection depuis laquelle le message a été recu
     */
    private static void redirectToHandler(PeerMessage message, Node node, PeerConnection connection) {
        // handle message
        try {
            node.getMapMessage().get(message.getType()).handleMessage(node, connection, message); // Gerer erreur possible
        } catch (NullPointerException e) {
            System.err.println("Altered packet, will be ignored");
        }
    }

    /**
     * fonction utilisé pour les connections directs via un TCP pushing ou en local
     * @param username id de la personne dont on souhaite connaitre les informations
     * @return ip de l'utilisateur passé en paramétre
     */
    private static String askForInfos(String username) {
        PeerMessage askInfo = new PeerMessage(MessageType.INFO, "XXXXXX", myUsername, username, "".getBytes());
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

    /**
     * fonction permettant de créer un groupe
     * @param groupID nom du groupe à créer
     * @return true si le groupe a pu être créé, false sinon
     */
    public static boolean createGroup(String groupID) {
        waitingForGroupValidation = true;
        //on vérifie que ce groupe n'existe pas déjà
        Group group = InterfaceUtil.createGroup(groupID, myUsername, n);
        if (group != null) {
            if(group.getMember(myUsername) == null) {
                group.addMember(myself);
            }
            groups.add(group);
        }
        return group != null;
    }

    /**
     * Cette fonction permet de stocker un fichier de config sur le serveur et d'avertir les autre
     * membre du groupe concerné que le fichier a changé
     * @param filenameJSON nom du fichier
     * @param groupID nom du groupe concerné
     * @param idFrom nom de l'utilisateur effectuant la modification
     */
    public static void uploadJSON(String filenameJSON, String groupID, String idFrom) {
        PeerMessage uploadMessage;

        try {
            //Récupèration du fichier config chiffré
            RandomAccessFile configFile = new RandomAccessFile(filenameJSON, "r");
            byte[] configFileByte = new byte[(int) configFile.length()];
            configFile.readFully(configFileByte);
            uploadMessage = new PeerMessage(MessageType.UPLO, groupID, idFrom, idFrom, configFileByte);
            synchronized (out) {
                out.write(uploadMessage.getFormattedMessage());
                out.flush();
            }

            //avertie les autres membres du groupe
            broadcastUpdate(idFrom, groupID);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * permet de recuperer un groupe à partir de son nom
     * @param id nom du group
     * @return le groupe portant ce nom
     */
    public static Group getGroupById(String id) {
        Group group = null;
        for (Group g : groups) {
            if (g.getID().equals(id)) {
                group = g;
            }
        }
        return group;
    }

    /**
     * fonction prevenant les utilisateurs d'un groupe que le fichier de configuration a changé
     * @param idFrom nom du membre ayant effectué la modification du fichier
     * @param groupID npm du group concerné
     */
    public static void broadcastUpdate(String idFrom, String groupID) {
        Group group = getGroupById(groupID);
        if (group != null) {
            for (Person person : group.getMembers()) {
                if (!person.getID().equals(myUsername)) {
                    PeerMessage pm = new PeerMessage(MessageType.UPDT, group.getID(), idFrom, person.getID(), "".getBytes());
                    try {
                        synchronized (out) {
                            out.write(pm.getFormattedMessage());
                            out.flush();
                        }
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }

            }
        }
    }

    /**
     * fonction permettant de récuperer le fichier de configuration d'un groupe depuis le serveur
     * @param groupID nom du groupe concerné
     */
    public static void downloadJSON(String groupID) {
        PeerMessage downloadMessage = new PeerMessage(MessageType.DOWN, groupID, myUsername, myUsername, "".getBytes());
        try {
            waitingJsonFromServer = true;
            while(waitingJsonFromServer) {
                // Averti le serveur que le client désire avoir le fichier 'config'
                synchronized (out) {
                    out.write(downloadMessage.getFormattedMessage());
                    out.flush();
                }
                int count = 0;
                int maxTry = 5;

                //on telecharge le fichier de config, et on redemande le download toute les deux secondes si rien n'a été envoyé
                while (waitingJsonFromServer && count < 20 && maxTry-- > 0) {
                    try {
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

        Platform.runLater(() -> controller.updateGroupsAndFiles());
    }

    private static void saveReceivedJson(PeerMessage pm) {
        byte[] buffer = pm.getMessageContent();
        int size = pm.getMessageContent().length;
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(new File(Constant.ROOT_GROUPS_DIRECTORY + "/" + pm.getIdGroup() + "/" + Constant.CONFIG_FILENAME));
            fOut.write(buffer, 0, size);
            fOut.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fOut != null) {
                try {
                    fOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * fonction permettant de connaitre tous les groupes de l'utilisateur
     * @return la list des groupes de l'utilisateur
     */
    private static String[] scanGroups() {
        File directory = new File(Constant.ROOT_GROUPS_DIRECTORY);
        String[] groups = directory.list((file, name) -> new File(file, name).isDirectory());
        return groups;
    }

    /**
     * fonction permmettant d'inviter une nouvelle personne dans un groupe
     * @param username personne à inviter
     * @param groupID groupe concerné
     */
    public static void inviteNewMember(String username, String groupID) {

        if (getGroupById(groupID).getMember(username) == null) {

            PeerMessage invitePM = new PeerMessage(MessageType.INVI, groupID, myUsername, username, "".getBytes());

            try {
                synchronized (out) {
                    out.write(invitePM.getFormattedMessage());
                    out.flush();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * fonction appelé lors de l'acceptation du invitation à rejoindre un groupe
     * @param usernameFrom nom de la personne ayant envoyé l'invitation
     * @param groupID nom du groupe concerné
     */
    public static void acceptInvite(String usernameFrom, String groupID) {
        PeerMessage acceptInvitePM = new PeerMessage(MessageType.INVK, groupID, myUsername, usernameFrom, "".getBytes());

        try {
            synchronized (out) {
                out.write(acceptInvitePM.getFormattedMessage());
                out.flush();
            }

            // Crée le groupe localement
            String dir = Constant.ROOT_GROUPS_DIRECTORY + "/" + groupID;
            File file = new File(dir);
            if (!file.exists() || !file.isDirectory()) {
                file.mkdirs();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * fonction permettant de mettre à jour le fichier de configraion d'un groupe
     * que l'on vient de rejoindre
     * @param groupID groupe concerné
     */
    public static void updateJsonAfterInvitation(String groupID) {
        //Téléchargement json du groupe
        downloadJSON(groupID);

        // Ajoute le group dans sa liste groupe
        RandomAccessFile configFile;
        try {

            configFile = new RandomAccessFile(Constant.ROOT_GROUPS_DIRECTORY + "/" + groupID + "/" + Constant.CONFIG_FILENAME, "r");
            byte[] configFileByte = new byte[(int) configFile.length()];
            configFile.readFully(configFileByte);

            byte[] plainConfig = CipherUtil.AESDecrypt(configFileByte, n.getKey(groupID));

            Group group = JSONUtil.parseJson(new String(plainConfig), Group.class);
            group.addMember(myself);
            groups.add(group);

            for (Person p : group.getMembers())
                System.out.println(p.getID());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
        }


        // Update le json en s'ajoutant dans le groupe
        JSONUtil.updateConfig(Client.getGroupById(groupID));


        // Upload le nouveau json sur le serveur
        Client.uploadJSON(Constant.ROOT_GROUPS_DIRECTORY + "/" + groupID + "/" + Constant.CONFIG_FILENAME, groupID, myUsername);

        Platform.runLater(() -> {
            controller.enableButtons();
            controller.updateGroupsAndFiles();
        });
    }

    /**
     * fonction permettant d'envoyé un peerMessage au serveur relai
     * @param pm message à envoyer
     */
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
        Platform.runLater(() -> controller.updateGroupsAndFiles());
    }

    /**
     * Permet de mettre à jour un groupe à partir d'un fichier de configuration
     * @param groupID
     */
    private static void updateGroupsWithJson(String groupID) {
        int index = groups.indexOf(getGroupById(groupID));
        if (index >= 0) {
            RandomAccessFile configFile;
            try {
                //récupération du fichier de configuration
                configFile = new RandomAccessFile(Constant.ROOT_GROUPS_DIRECTORY + "/" + groupID + "/" + Constant.CONFIG_FILENAME, "r");
                byte[] configFileByte = new byte[(int) configFile.length()];
                configFile.readFully(configFileByte);

                byte[] plainConfig = CipherUtil.AESDecrypt(configFileByte, n.getKey(groupID));

                //modification du groupe
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

    /**
     * Permet d'envoyer un requete pour obtenir un fichier
     * @param file fichier désiré
     * @param group groupe concerné par la requete
     */
    public static void requestFile(String file, String group) {
        n.requestFile(file, group);
    }

    /**
     * permet de mettre à jour la barre de progression du download
     * @param value valeur entre 0 et 1
     */
    public static void updateDownloadBar(double value) {
        final double v = value;
        Platform.runLater(() -> {
            controller.updateDownloadBar(v);
            if(Math.abs(v - 1.0) < 0.001) {
                controller.enableDownLoad();
            }
        });

    }

    /**
     * permet de mettre à jour la barre de progression d'upload
     * @param value valeur entre 0 et 1
     */
    public static void updateUploadBar(double value) {
        final double v = value;
        Platform.runLater(() -> controller.updateUploadBar(v));
    }

    /**
     * permet de reset la barre d'upload
     */
    public static void clearUploadBar() {
        Platform.runLater(() -> controller.clearUploadBar());
    }
}