package sample;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;

public class Controller implements Initializable {
    static int groupeID = 1;

    @FXML
    private Accordion accordion;

    @FXML
    private void handleCreateButtonAction(ActionEvent event) {
        TitledPane bonjour = new TitledPane();
        ListView view = new ListView();
        bonjour.setText("Groupe " + groupeID++);
        bonjour.setContent(view);
        bonjour.setCollapsible(true);

        accordion.getPanes().add(bonjour);

    }


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }
}