package views;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;

public class RootLayoutController implements Initializable {

    private views.Main mainApp;
    static int groupeID = 1;
    static List<TitledPane> listPanes = new ArrayList<>();

    public void setMainApp(views.Main mainApp) {
        this.mainApp = mainApp;
    }


    @FXML
    private Accordion accordion;

    @FXML
    private void handleCreateButtonAction() {
        TitledPane pane = new TitledPane();
        listPanes.add(pane);
        ListView view = new ListView();
        Label label = new Label();
        label.setText("Test ip");
        view.setId("groupe" + groupeID);
        view.getItems().add(label);
        listPanes.get(groupeID - 1).setText("Groupe " + groupeID);
        listPanes.get(groupeID - 1).setContent(view);
        listPanes.get(groupeID - 1).setCollapsible(true);
        accordion.getPanes().add(listPanes.get(groupeID - 1));
        ++groupeID;
    }

    @FXML
    private void handleInvite() {
        // TODO: get the groupe in which we add a member
        String groupe = "";
        boolean okClicked = mainApp.showInviteDialog(groupe);
    }

    @FXML
    private void handleQuit() {
        System.exit(0);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }
}