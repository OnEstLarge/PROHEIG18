package views;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;

public class RootLayoutController implements Initializable {

    private views.Main mainApp;
    static int groupeID = 1;

    public void setMainApp(views.Main mainApp){
        this.mainApp = mainApp;
    }


    @FXML
    private Accordion accordion;

    @FXML
    private void handleCreateButtonAction() {
        TitledPane bonjour = new TitledPane();
        ListView view = new ListView();
        bonjour.setText("Groupe " + groupeID++);
        bonjour.setContent(view);
        bonjour.setCollapsible(true);
        accordion.getPanes().add(bonjour);
    }

    @FXML
    private void handleInvite(){
        // TODO: get the groupe in which we add a member
        String groupe = "";
        boolean okClicked = mainApp.showInviteDialog(groupe);
    }

    @FXML
    private void handleQuit(){
        System.exit(0);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }
}