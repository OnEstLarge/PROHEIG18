package views;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import main.Client;
import peer.PeerMessage;

public class InviteDialogController {

    private Stage dialogStage;
    private boolean okClicked = false;

    @FXML
    private ComboBox<String> comboBox;

    @FXML
    private TextField newUserPseudoField;

    @FXML
    private void initialize() {}

    /**
     * Défini la scene de cette fenête de dialogue et ajoute le logo de l'application
     * à la fenêtre.
     *
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage, Image image) {
        this.dialogStage = dialogStage;
        this.dialogStage.getIcons().add(image);
    }

    /**
     * @return true si jamais le bouton OK a été pressé, false sinon.
     */
    public boolean isOkClicked() {
        return okClicked;
    }

    /**
     * Handler appellé quand l'utilisateur appuye sur Annuler. Réactive les boutons et
     * ferme la fenêtre.
     */
    @FXML
    private void handleCancel() {
        Client.getController().enableButtons();
        dialogStage.close();
    }

    /**
     * Permet d'ajouter le groupe au menu déroulant.
     *
     * @param name le nom du groupe à ajouter
     */
    @FXML
    public void addGroupNameToCombo(String name) {
        comboBox.getItems().add(name);
    }

    /**
     * Efface tout le contenu du menu déroulant
     */
    @FXML
    public void clearCombo() {
        comboBox.getItems().clear();
    }

    /**
     * Handler appelé si jamais l'utilisateur appuie sur le bouton OK.
     * Appelle la fonction isInputValid() pour vérifier les entrées utilisateur.
     * Si elles sont correctes, envoie une invitation à l'utilisateur,
     * réactive les boutons et ferme la fenêtre.
     * Si jamais les informations ne sont pas valides, ne fait rien.
     */
    @FXML
    private void handleOk() {
        if (isInputValid()) {

            Client.inviteNewMember(newUserPseudoField.getText(), comboBox.getValue());

            Client.getController().enableButtons();
            okClicked = true;
            dialogStage.close();
        }
    }

    /**
     * Vérifie les informations entrées par l'utilisateur. Si jamais ce n'est pas le cas, affiche un message d'erreur.
     * Le nom de l'utilisateur doit être de bonne longueur (taille définie dans PeerMessage).
     *
     * @return True si jamais les champs sont corrects, false sinon.
     */
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