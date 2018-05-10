package views;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import main.Main;
import peer.PeerMessage;

public class PseudoDialogController {

    private Stage dialogStage;
    private boolean nameOK = false;
    private Main mainApp;

    @FXML
    private TextField userPseudoField;

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
        dialogStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.exit(0);
            }
        });
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Returns true if the user clicked OK, false otherwise.
     *
     * @return
     */
    public boolean isNameOK() {
        return nameOK;
    }

    /**
     * Called when the user clicks ok.
     */
    @FXML
    private void handleOk() {

        if(isInputValid()){
            mainApp.setUserPseudo(userPseudoField.getText());
            nameOK = true;
        }

        dialogStage.close();

        // TODO: créer les dossiers nécessaire
    }

    private boolean isInputValid(){
        String errorMessage = "";
        String pseudo = userPseudoField.getText();

        if(!PeerMessage.isValidIdFormat(pseudo, PeerMessage.ID_MIN_LENGTH, PeerMessage.ID_MAX_LENGTH)){
            errorMessage += "Pseudo format invalid. It must be between " + PeerMessage.ID_MIN_LENGTH + " and " + PeerMessage.ID_MAX_LENGTH + " characters long.\n";
        }
        // TODO: verifier si jamais le pseudo est deja pris

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
