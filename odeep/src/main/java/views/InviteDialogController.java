package views;

import User.Person;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import main.Client;
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
    public void setDialogStage(Stage dialogStage, Image image) {
        this.dialogStage = dialogStage;
        this.dialogStage.getIcons().add(image);
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
        Client.getController().enableButtons();
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

            Client.inviteNewMember(newUserPseudoField.getText(), comboBox.getValue());

            okClicked = true;
            dialogStage.close();
        }
    }

    private boolean isInputValid() {
        if (!PeerMessage.isValidIdFormat(newUserPseudoField.getText(), PeerMessage.ID_MIN_LENGTH, PeerMessage.ID_MAX_LENGTH)) {
            String errorMessage = "Nom d'utilisateur invalide. Il doit contenir entre " + PeerMessage.ID_MIN_LENGTH + " et " + PeerMessage.ID_MAX_LENGTH + " caractères.\n";

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Champ invalide");
            alert.setHeaderText("Veuillez corriger les champs invalides!");
            alert.setContentText(errorMessage);

            alert.showAndWait();

            return false;
        }

        return true;
    }
}