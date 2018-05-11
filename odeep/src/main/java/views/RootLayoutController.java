package views;

import java.io.File;
import java.net.URL;
import java.util.*;

import User.Person;
import User.Group;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import main.Main;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import peer.PeerMessage;

public class RootLayoutController implements Initializable {

    private Main mainApp;
    private static List<ListView> listView = new ArrayList();
    private Stage dialogStage;
    private boolean okClicked = false;
    private HashMap<String, List<File>> mapFile = new HashMap<String, List<File>>();

    @FXML
    private Accordion accordion;

    @FXML
    private TextField groupNameField;

    @FXML
    private ListView middleList;

    public void setMainApp(Main mainApp) {
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
        if (PeerMessage.isValidIdFormat(groupNameField.getText(), PeerMessage.ID_GROUP_MIN_LENGTH, PeerMessage.ID_GROUP_MAX_LENGTH)) {
            TitledPane pane = new TitledPane();
            ListView view = new ListView();

            // TODO: s'ajouter au groupe et mettre les fichiers dispo a jour.
            listView.add(view);
            view.setId(groupNameField.getText());
            pane.setText(groupNameField.getText());
            pane.setContent(view);
            pane.setCollapsible(true);

            // Display the files availables in the group in the middle pane when the group is selected.
            pane.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    middleList.getItems().clear();
                    middleList.getItems().add("test");
                    if(mapFile.containsKey(groupNameField.getText())) {
                        List<File> files = mapFile.get(groupNameField.getText());
                        for (File f : files) {
                            middleList.getItems().add(f.getName());
                        }
                    }
                }
            });
            accordion.getPanes().add(pane);
        }
        groupNameField.setText("");
    }

    public void fillFileMap(List<Group> groups){
        for(Group g : groups){
            List<File> files = new ArrayList<File>();
            for(Person p : g.getMembers()){
                for(File f : p.getFiles()){
                    if(!files.contains(f)){
                        files.add(f);
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