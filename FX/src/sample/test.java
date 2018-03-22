package sample;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;

public class test implements Initializable {

    @FXML
    private Accordion accordion;

    @FXML
    private void handleButtonAction(ActionEvent event) {
        AnchorPane newPanelContent = new AnchorPane();
        newPanelContent.getChildren().add(new Label("Hello World"));
        TitledPane pane = new TitledPane("World Pane", newPanelContent);
        System.out.println(accordion);
        accordion.getPanes().add(pane);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }
}