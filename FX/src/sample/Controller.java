package sample;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;

public class Controller implements Initializable {
    static int groupeID = 0;

    @FXML
    private Accordion accordion;

    @FXML
    private void handleCreateButtonAction(ActionEvent event) {
        AnchorPane newPanelContent = new AnchorPane();
        newPanelContent.getChildren().add(new Label("Groupe " + groupeID++));
        TitledPane pane = new TitledPane("World Pane", newPanelContent);
        System.out.println(accordion);
        accordion.getPanes().add(pane);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }
}