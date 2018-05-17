package views;

import java.io.File;
import java.net.URL;
import java.util.*;

import User.Person;
import User.Group;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import main.Client;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import peer.PeerMessage;

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
        if (!PeerMessage.isValidIdFormat(groupNameField.getText(), PeerMessage.ID_GROUP_MIN_LENGTH, PeerMessage.ID_GROUP_MAX_LENGTH)) {
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
                    System.out.println(mapFile.toString());
                    System.out.println(groupName);
                    if (mapFile.containsKey(groupName)) {
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

    public void fillFileMap(List<Group> groups) {
        for (Group g : groups) {
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

    @FXML
    private void handleInvite() {
        mainApp.showInviteDialog();
    }

    @FXML
    private void handleQuit() {
        System.exit(0);
    }

    @FXML
    private void handleOk() {

    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    public void initialize(URL url, ResourceBundle rb) {
    }
}