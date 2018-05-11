package views;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import peer.PeerMessage;

public class AcceptInviteDialogController {

    private Stage dialogStage;
    private boolean okClicked = false;

    @FXML
    private Label messageLabel;

    public Label getMessageLabel() {
        return messageLabel;
    }

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
    private void handleRefuse() {
        dialogStage.close();
    }

    /**
     * Called when the user clicks ok.
     */
    @FXML
    private void handleOk() {
        // TODO: ajouter le groupe sur l'interface et récuperer les fichiers.
        okClicked = true;
        dialogStage.close();

    }
}