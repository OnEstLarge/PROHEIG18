package views;

/*
 -----------------------------------------------------------------------------------
 Odeep
 Fichier     : handler.DHR1Handler.java
 Auteur(s)   : Burgbacher Lionel, Jee Mathieu, Kopp Olivier, Piller Florent,
               Silvestri Romain, Schürch Loïc
 Compilateur : jdk 1.8.0_144
 -----------------------------------------------------------------------------------
*/

import java.io.File;
import java.net.URL;
import java.util.*;

import User.Person;
import User.Group;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import main.Client;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import peer.PeerMessage;
import util.Constant;
import util.InterfaceUtil;

/**
 * Classe principale de l'interface utilisateur
 */
public class RootLayoutController implements Initializable {

    private Client mainApp;
    private static List<ListView> listView = new ArrayList();
    private String selectedGroup;

    private HashMap<String, List<String>> mapFile = new HashMap<>();

    @FXML
    private Accordion accordion;

    @FXML
    private TextField groupNameField;

    @FXML
    private ListView middleList;

    @FXML
    private Button downloadButton;

    @FXML
    private Button inviteButton;

    @FXML
    private Button createButton;

    @FXML
    private Button addButton;

    @FXML
    private Button removeButton;

    @FXML
    private ProgressBar downloadBar;

    @FXML
    private ProgressBar uploadBar;

    @FXML
    private Label downloadPercent;

    @FXML
    private Label uploadPercent;

    /**
     * Désactive tous les boutons de l'interface.
     */
    public void disableButtons() {
        downloadButton.setDisable(true);
        inviteButton.setDisable(true);
        createButton.setDisable(true);
        addButton.setDisable(true);
        removeButton.setDisable(true);
    }

    /**
     * Réactive tous les boutons de l'interface.
     */
    public void enableButtons() {
        downloadButton.setDisable(false);
        inviteButton.setDisable(false);
        createButton.setDisable(false);
        addButton.setDisable(false);
        removeButton.setDisable(false);
    }

    /**
     * Désactive les boutons download et remove
     */
    public void disableInviteRemove() {
        removeButton.setDisable(true);
        inviteButton.setDisable(true);
    }

    /**
     * Réactive les boutons download et remove
     */
    public void enableInviteRemove() {
        inviteButton.setDisable(false);
        removeButton.setDisable(false);
    }

    public void setMainApp(Client mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Handler appelé lorsque le bouton Créer est appuyé.
     * Crée un groupe portant le nom entré par l'utilisateur.
     * Si jamais le nom est incorrect, affiche une fenêtre d'erreur. La taille du nom est défini dans PeerMessage.
     * Demande au serveur si jamais le nom est disponible. Affiche une erreur si ce n'est pas le cas.
     * Si il est correct, ajoute le groupe à l'Accordéon sur l'interface de base et ajoute l'utilisateur au groupe.
     * Ajoute un action listener à l'élément créé afin d'afficher la liste des fichiers du groupe dans la partie
     * centrale de l'interface lorsque le groupe est selectionné.
     * Met à jour les fichiers et les groupes affichés dans l'interface à la fin de la fonction.
     */
    @FXML
    private void handleCreateButtonAction() {
        String errorMsg = "";
        if (!Client.createGroup(groupNameField.getText())) {
            errorMsg += "Le nom de groupe doit contenir entre " + PeerMessage.ID_GROUP_MIN_LENGTH + " et " + PeerMessage.ID_GROUP_MAX_LENGTH + " caractères et doit être unique.\n";
        } else {
            TitledPane pane = new TitledPane();
            final ListView view = new ListView();

            listView.add(view);
            final String groupName = groupNameField.getText();
            view.setId(groupName);
            pane.setText(groupName);
            pane.setContent(view);
            pane.setCollapsible(true);
            mapFile.put(groupName, new ArrayList<>());

            // Affiche tous les fichier du groupe dans la partie centrale de l'interface quand le groupe est selectionné.
            pane.setOnMouseClicked(event -> {
                selectedGroup = view.getId();
                middleList.getItems().clear();
                if (mapFile.containsKey(groupName) && mapFile.get(groupName) != null) {
                    List<String> files = mapFile.get(groupName);
                    for (String s : files) {
                        middleList.getItems().add(s);
                    }
                }
            });
            accordion.getPanes().add(pane);
        }
        if (errorMsg.length() != 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Champ invalide");
            alert.setHeaderText("Veuillez corriger les champs invalides!");
            alert.setContentText(errorMsg);

            alert.showAndWait();
        }
        groupNameField.setText("");
        updateGroupsAndFiles();
    }

    /**
     * Remplis la map en associant un groupe à une liste fichiers.
     */
    private void fillFileMap() {
        for (Group g : mainApp.getGroups()) {
            List<String> files = new ArrayList<>();
            for (Person p : g.getMembers()) {
                if (p.isConnected()) {
                    for (String s : p.getFiles()) {
                        if (!files.contains(s)) {
                            files.add(s);
                        }
                    }
                }
            }
            mapFile.put(g.getID(), files);
        }
    }

    /**
     * Remet à jour les groupes et les fichiers dans l'interface graphique.
     */
    public void updateGroupsAndFiles() {
        accordion.getPanes().clear();
        listView.clear();

        // Met la liste des fichiers à jour
        fillFileMap();

        for (final Group g : mainApp.getGroups()) {
            TitledPane pane = new TitledPane();
            final ListView view = new ListView();

            for (Person p : g.getMembers()) {
                if (p.isConnected()) {
                    view.getItems().add(p.getID());
                }
            }

            listView.add(view);
            view.setId(g.getID());
            pane.setText(g.getID());
            pane.setContent(view);
            pane.setCollapsible(true);

            // Affiche tous les fichier du groupe dans la partie centrale de l'interface quand le groupe est selectionné.
            pane.setOnMouseClicked(event -> {
                selectedGroup = view.getId();
                middleList.getItems().clear();
                if (mapFile.containsKey(g.getID()) && mapFile.get(g.getID()) != null) {
                    List<String> files = mapFile.get(g.getID());
                    for (String s : files) {
                        middleList.getItems().add(s);
                    }
                }
            });
            accordion.getPanes().add(pane);
        }
    }

    /**
     * Handler appellé lorsque le bouton Inviter est appuyé.
     * Désactive les boutons, affiche la fenêtre d'invitation puis réactive les boutons.
     */
    @FXML
    private void handleInvite() {
        disableButtons();
        mainApp.showInviteDialog();
        enableButtons();
    }

    /**
     * Handler appellé losque le bouton Ajouter un fichier est appuyé.
     * Affiche un explorateur de fichier permettant de choisir le fichier à ajouter.
     * Tous les types de fichiers sont acceptés. Ajoute le fichier dans le groupe sélectionné.
     */
    @FXML
    private void handleAdd() {
        FileChooser fileChooser = new FileChooser();

        // Défini le nom de la fenêtre
        fileChooser.setTitle("Choississez le fichier à ajouter");

        // Défini les extensions à traiter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Tous", "*.*");
        fileChooser.getExtensionFilters().add(extFilter);

        // Affiche la fenêtre de dialogue
        File file = fileChooser.showOpenDialog(mainApp.getPrimaryStage());

        if (file != null && mapFile.containsKey(selectedGroup)) {
            mapFile.get(selectedGroup).add(file.getName());
            middleList.getItems().add(file.getName());
            InterfaceUtil.addFile(file, Client.getUsername(), Client.getGroupById(selectedGroup));
        }
    }

    /**
     * Handler appellé quand le bouton Supprimer le fichier est appuyé.
     * Supprime le fichier selectionné.
     */
    @FXML
    private void handleRemove() {
        List<String> file = middleList.getSelectionModel().getSelectedItems();
        if(file == null){
            return;
        }
        if (mapFile.containsKey(selectedGroup)) {
            if(file.get(0) == null){
                return;
            }
            InterfaceUtil.removeFile(file.get(0), Client.getUsername(), Client.getGroupById(selectedGroup));
            mapFile.get(selectedGroup).remove(file.get(0));
            middleList.getItems().remove(file.get(0));
        }
    }

    /**
     * Handler appelé quand l'utilisateur clique sur l'option Quitter.
     * Ferme l'application.
     */
    @FXML
    private void handleQuit() {
        System.out.println("QUIT");
        Client.connectMyself(false);
        System.exit(1);
    }

    /**
     * Handler appellé lorsque le bouton Télecharger est appuyé.
     * Désactive les boutons Télécharger et Supprimer.
     * Envoie un message à la première personne du groupe demandant le fichier selectionné.
     */
    @FXML
    private void handleDownload() {
        List<String> file = middleList.getSelectionModel().getSelectedItems();
        if(file == null){
            return;
        }
        String filename = file.get(0);
        File fileToDownload = new File(Constant.ROOT_GROUPS_DIRECTORY + "/" + selectedGroup + "/" + filename);
        if (fileToDownload.exists()) {
            return;
        } else {
            disableInviteRemove();
            mainApp.requestFile(filename, selectedGroup);
            clearDownloadBar();
        }
    }

    /**
     * Remet la barre de progression pour le téléchargement à 0
     */
    public void clearDownloadBar() {
        downloadBar.setProgress(0);
    }

    /**
     * Remet la barre de progression pour l'upload à 0
     */
    public void clearUploadBar() {
        uploadBar.setProgress(0);
        downloadPercent.setText("0%");
        uploadPercent.setText("0%");

    }

    /**
     * Met à jour la barre de progression pour le téléchargement du fichier
     *
     * @param value la progression du téléchargement
     */
    public void updateDownloadBar(double value) {
        downloadBar.setProgress(value);
        downloadPercent.setText((((int) (value * 10000)) / 100.0) + "%");
    }

    /**
     * Met à jour la barre de progression pour l'upload du fichier
     *
     * @param value la progression de l'upload
     */
    public void updateUploadBar(double value) {
        uploadBar.setProgress(value);
        uploadPercent.setText((((int) (value * 10000)) / 100.0) + "%");
    }

    public void initialize(URL url, ResourceBundle rb) {
    }
}