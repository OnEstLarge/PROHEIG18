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

    private Main mainApp;
    static int groupeID = 1;
    //public static List<TitledPane> listPanes = new ArrayList<>();
    public static List<ListView> listView = new ArrayList<>();

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }


    @FXML
    private Accordion accordion;

    @FXML
    private void handleCreateButtonAction() {
        TitledPane pane = new TitledPane();
        //listPanes.add(pane);
        ListView view = new ListView();
        listView.add(view);
        Label label = new Label();
        label.setText("Test ip");
        listView.get(groupeID-1).setId("groupe" + groupeID);
        listView.get(groupeID-1).getItems().add(label);
        pane.setText("Groupe " + groupeID);
        pane.setContent(view);
        pane.setCollapsible(true);
        accordion.getPanes().add(pane);
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