import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import javafx.scene.control.TextField;

public class InviteDialogController {

    private Stage dialogStage;
    private boolean okClicked = false;

    @FXML
    private TextField groupeIDField;

    @FXML
    private TextField newUserIPField;

    @FXML
    private void initialize() {
    }

    /**
     * Sets the stage of this dialog.
     *
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Returns true if the user clicked OK, false otherwise.
     *
     * @return
     */
    public boolean isOkClicked() {
        return okClicked;
    }

    /**
     * Called when the user clicks cancel.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Called when the user clicks ok.
     */
    @FXML
    private void handleOk() {
        Label newIp = new Label();
        newIp.setText(newUserIPField.getText());
        System.out.println(groupeIDField.getText());
        int group = Integer.parseInt(groupeIDField.getText())-1;
        if(group > RootLayoutController.listView.size()){
            return; // TODO: afficher une fenetre d'erreur
        }
        RootLayoutController.listView.get(group).getItems().add(newIp);
        okClicked = true;
        dialogStage.close();
    }
}

