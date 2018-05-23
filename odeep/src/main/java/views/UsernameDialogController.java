package views;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import main.Client;
import peer.PeerMessage;

import java.io.File;

public class UsernameDialogController {

    private Stage dialogStage;
    private boolean nameOK = false;
    private Client mainApp;

    @FXML
    private TextField usernameField;

    @FXML
    private void initialize() {}

    /**
     * Défini la scene de cette fenête de dialogue et ajoute le logo de l'application
     * à la fenêtre.
     * Change le fonctionnement de la fenêtre pour quitter l'application en cas de fermeture de la
     * fenêtre.
     *
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage, Image image) {
        this.dialogStage = dialogStage;
        this.dialogStage.getIcons().add(image);
        dialogStage.setOnCloseRequest(we -> System.exit(0));
    }

    public void setMainApp(Client mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * @return true si jamais le nom est correct, false sinon.
     */
    public boolean isNameOK() {
        return nameOK;
    }

    /**
     * Handler appellé quand l'utilisateur appuye sur le boutton OK.
     * Vérifie si jamais le nom de l'utilisateur est correct en appellant la fonction isInputValid().
     * Si jamais elles sont correctes, récupere le nom d'utilisateur, crée l'arborésence si
     * jamais elle n'existe pas et ferme la fenêtre.
     *
     */
    @FXML
    private void handleOk() {

        if(isInputValid()){
            mainApp.setUsername(usernameField.getText());
            nameOK = true;
        }

        dialogStage.close();

        String dir = "./shared_files";
        File file = new File(dir);

        if (!file.exists() || !file.isDirectory()) {
            file.mkdir();
        }
    }

    /**
     * Vérifie si le nom de l'utilisateur est correct. La longueur de celui-ci est défini dans PeerMessage.
     * Si jamais le nom n'est pas valide, affiche une fenêtre d'erreur.
     *
     * @return true si jamais le nom est correct, false sinon.
     */
    private boolean isInputValid(){
        String errorMessage = "";
        String pseudo = usernameField.getText();

        if(!PeerMessage.isValidIdFormat(pseudo, PeerMessage.ID_MIN_LENGTH, PeerMessage.ID_MAX_LENGTH) || !Client.usernameValidation(pseudo)){
            errorMessage += "Nom d'utilisateur invalide. Il doit contenir entre " + PeerMessage.ID_MIN_LENGTH + " et " + PeerMessage.ID_MAX_LENGTH + " caractères.\n";
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Champ invalide");
            alert.setHeaderText("Veuillez corriger les champs invalides!");
            alert.setContentText(errorMessage);

            alert.showAndWait();

            return false;
        }
    }
}
