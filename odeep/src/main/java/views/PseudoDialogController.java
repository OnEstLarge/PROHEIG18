package views;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PseudoDialogController {

    private Stage pseudoStage;
    private boolean okClicked = false;

    @FXML
    private TextField userPseudoField;

    @FXML
    private void initialize() {
    }

    /**
     * Sets the stage of this dialog.
     *
     * @param pseudoStage
     */
    public void setPseudoStage(Stage pseudoStage) {
        this.pseudoStage = pseudoStage;
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
     * Called when the user clicks ok.
     */
    @FXML
    private void handleOk() {

        if(isInputValid()){
            // TODO: verifier si jamais le pseudo est deja pris
        }

        // TODO: créer les dossiers nécessaire
    }

    private boolean isInputValid(){
       return true;
    }
}
