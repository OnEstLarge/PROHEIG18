package views;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import User.Person;
import User.Group;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.Client;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import peer.PeerMessage;
import util.InterfaceUtil;

public class RootLayoutController implements Initializable {

    private Client mainApp;
    private static List<ListView> listView = new ArrayList();
    private Stage dialogStage;
    private boolean okClicked = false;

    public HashMap<String, List<String>> getMapFile() {
        return mapFile;
    }

    private HashMap<String, List<String>> mapFile = new HashMap<String, List<String>>();

    @FXML
    private Accordion accordion;

    @FXML
    private TextField groupNameField;

    @FXML
    private ListView middleList;

    public void setMainApp(Client mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Return the list of view. Used to add accordion to the layout.
     *
     * @return the list of view from the root layout
     */
    public static List<ListView> getListView() {
        return listView;
    }

    @FXML
    private void handleCreateButtonAction() {
        String errorMsg = "";
        if (!Client.createGroup(groupNameField.getText()) ) {
            errorMsg += "Group name must be between " + PeerMessage.ID_GROUP_MIN_LENGTH + " and " + PeerMessage.ID_GROUP_MAX_LENGTH + " characters long.\n";
        } else {
            TitledPane pane = new TitledPane();
            ListView view = new ListView();

            // TODO: s'ajouter au groupe et mettre les fichiers dispo a jour.
            listView.add(view);
            final String groupName = groupNameField.getText();
            view.setId(groupName);
            pane.setText(groupName);
            pane.setContent(view);
            pane.setCollapsible(true);
            mapFile.put(groupName, null); // TODO: ajouter les fichiers de l'utilisateur.

            // Display the files availables in the group in the middle pane when the group is selected.
            pane.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    middleList.getItems().clear();
                    System.out.println(middleList.getItems());
                    System.out.println(mapFile.toString());
                    if (mapFile.containsKey(groupName) && mapFile.get(groupName) != null) {
                        List<String> files = mapFile.get(groupName);
                        for (String s : files) {
                            middleList.getItems().add(s);
                        }
                    }
                }
            });
            accordion.getPanes().add(pane);
        }
        if (errorMsg.length() != 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMsg);

            alert.showAndWait();
        }
        groupNameField.setText("");
    }

    private void fillFileMap() {
        for (Group g : mainApp.getGroups()) {
            List<String> files = new ArrayList<String>();
            for (Person p : g.getMembers()) {
                for (String s : p.getFiles()) {
                    if (!files.contains(s)) {
                        files.add(s);
                    }
                }
            }
            mapFile.put(g.getID(), files);
        }
    }

    // On remet à jour les groupes dans le menu déroulant et les fichiers
    public void updateGroupsAndFiles(){
        accordion.getPanes().clear();
        listView.clear();

        // Update the list of files
        fillFileMap();

        for(final Group g: mainApp.getGroups()){
            TitledPane pane = new TitledPane();
            ListView view = new ListView();

            for(Person p : g.getMembers()){
                view.getItems().add(p.getID());
            }

            listView.add(view);
            view.setId(g.getID());
            pane.setText(g.getID());
            pane.setContent(view);
            pane.setCollapsible(true);

            // Display the files availables in the group in the middle pane when the group is selected.
            pane.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    middleList.getItems().clear();
                    System.out.println(middleList.getItems());
                    System.out.println(mapFile.toString());
                    if (mapFile.containsKey(g.getID()) && mapFile.get(g.getID()) != null) {
                        List<String> files = mapFile.get(g.getID());
                        for (String s : files) {
                            middleList.getItems().add(s);
                        }
                    }
                }
            });
            accordion.getPanes().add(pane);
        }
    }

    @FXML
    private void handleInvite() {
        mainApp.showInviteDialog();
    }


    @FXML
    private void handleAdd() {
        FileChooser fileChooser = new FileChooser();

        // Set the name of the window
        fileChooser.setTitle("Choose file to add");

        // Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("All", "*.*");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show open file dialog
        File file = fileChooser.showOpenDialog(mainApp.getPrimaryStage());

        if (file != null) {
            // TODO: la faking function
            System.out.println(file.getName());
            //mainApp.loadPersonDataFromFile(file);
        }
    }

    @FXML
    private void handleRemove() {
        FileChooser fileChooser = new FileChooser();

        // Set the name of the window
        fileChooser.setTitle("Choose file to remove");

        // Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("All", "*.*");
        fileChooser.getExtensionFilters().add(extFilter);

        // Set the root directory
        //fileChooser.setInitialDirectory(".userInfo");

        // Show open file dialog
        File file = null;
        file = fileChooser.showOpenDialog(mainApp.getPrimaryStage());


        if (file != null) {
            // TODO: la faking function
            try {
                System.out.println(file.getCanonicalFile());
            } catch (IOException e) {
                e.printStackTrace();
        }
    }
    //mainApp.loadPersonDataFromFile(file);
    }

    @FXML
    private void handleQuit() {
        System.exit(1);
    }

    @FXML
    private void handleDownload(){}


    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void initialize(URL url, ResourceBundle rb) {
    }
}