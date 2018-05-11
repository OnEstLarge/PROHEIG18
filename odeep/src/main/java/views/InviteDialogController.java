package views;

import User.Person;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import javafx.scene.control.TextField;
import peer.PeerMessage;
import sun.dc.path.PathError;

public class InviteDialogController {

    private Stage dialogStage;
    private boolean okClicked = false;

    @FXML
    private TextField groupeIDField;

    @FXML
    private TextField newUserPseudoField;

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
        if (isInputValid()) {
            Label newPseudo = new Label();
            newPseudo.setText(newUserPseudoField.getText());
            for (int i = 0; i < RootLayoutController.getListView().size(); ++i) {
                if (groupeIDField.getText().equals(RootLayoutController.getListView().get(i).getId())) {
                    RootLayoutController.getListView().get(i).getItems().add(newPseudo);
                    break;
                }
            }

            okClicked = true;
            dialogStage.close();
        }
    }

    private boolean isInputValid() {
        String errorMessage = "";
        String pseudo = groupeIDField.getText();
        boolean found = false;
        for (ListView view : RootLayoutController.getListView()) {
            if (pseudo.equals(view.getId())) {
                found = true;
                break;
            }
        }

        if (found != true) {
            errorMessage += "Group ID invalid. No group with this id.\n";
        }

        if (!PeerMessage.isValidIdFormat(newUserPseudoField.getText(), PeerMessage.ID_MIN_LENGTH, PeerMessage.ID_MAX_LENGTH)) {
            errorMessage += "Pseudo invalid format. Pseudo must be between " + PeerMessage.ID_MIN_LENGTH + " and " + PeerMessage.ID_MAX_LENGTH + "characters long.\n";
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);

            alert.showAndWait();

            return false;
        }
    }
}