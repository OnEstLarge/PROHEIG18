package views;

import javafx.fxml.FXML;
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
    public boolean isOkClicked() { return okClicked; }

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
        System.out.println(groupeIDField.getText());
        System.out.println(newUserIPField.getText());
        okClicked = true;
        dialogStage.close();
    }
}

