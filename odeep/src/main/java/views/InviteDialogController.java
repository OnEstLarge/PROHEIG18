package views;

import User.Person;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import peer.PeerMessage;
import sun.dc.path.PathError;

public class InviteDialogController {

    private Stage dialogStage;
    private boolean okClicked = false;

    @FXML
    private ComboBox<String> comboBox;

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

    @FXML
    public void addGroupNameToCombo(String name) {
        comboBox.getItems().add(name);
    }

    @FXML
    public void clearCombo() {
        comboBox.getItems().clear();
    }

    /**
     * Called when the user clicks ok.
     */
    @FXML
    private void handleOk() {
        if (isInputValid()) {
            Label newPseudo = new Label();
            newPseudo.setText(comboBox.getValue());
            for (int i = 0; i < RootLayoutController.getListView().size(); ++i) {
                if (comboBox.getValue().equals(RootLayoutController.getListView().get(i).getId())) {
                    RootLayoutController.getListView().get(i).getItems().add(newPseudo);
                    break;
                }
            }

            okClicked = true;
            dialogStage.close();
        }
    }

    private boolean isInputValid() {
        if (!PeerMessage.isValidIdFormat(newUserPseudoField.getText(), PeerMessage.ID_MIN_LENGTH, PeerMessage.ID_MAX_LENGTH)) {
            String errorMessage = "Pseudo invalid format. Pseudo must be between " + PeerMessage.ID_MIN_LENGTH + " and " + PeerMessage.ID_MAX_LENGTH + "characters long.\n";

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);

            alert.showAndWait();

            return false;
        }

        return true;
    }
}