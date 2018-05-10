package views;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import main.Main;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import peer.PeerMessage;

public class RootLayoutController implements Initializable {

    private Main mainApp;
    static int groupeID = 1;
    private static List<ListView> listView = new ArrayList();
    private Stage dialogStage;
    private boolean okClicked = false;

    @FXML
    private Accordion accordion;

    @FXML
    private TextField groupNameField;

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

    /**
     * Load the groups from the config files. Used when the application is started.
     */
    public void loadGroups() {

    }

    @FXML
    private void handleCreateButtonAction() {
        if (PeerMessage.isValidIdFormat(groupNameField.getText(), PeerMessage.ID_GROUP_MIN_LENGTH, PeerMessage.ID_GROUP_MAX_LENGTH)) {
            TitledPane pane = new TitledPane();
            ListView view = new ListView();

            // TODO: s'ajouter au groupe et mettre les fichiers dispo a jour.
            listView.add(view);
            pane.setText(groupNameField.getText());
            pane.setContent(view);
            pane.setCollapsible(true);

            // Display the files availables in the group in the middle pane when the group is selected.
            pane.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    // TODO: afficher les fichiers
                    System.out.println("testtest");
                }
            });

            groupNameField.setText("");
            accordion.getPanes().add(pane);
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