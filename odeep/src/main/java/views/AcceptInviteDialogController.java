package views;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import main.Client;

public class AcceptInviteDialogController {

    private Stage dialogStage;
    private boolean okClicked = false;

    private String groupID;
    private String idFrom;

    @FXML
    private Label messageLabel;

    public Label getMessageLabel() {
        return messageLabel;
    }

    @FXML
    private void initialize() {}

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public void setIdFrom(String idFrom) {
        this.idFrom = idFrom;
    }

    /**
     * Défini la scene de cette fenête de dialogue et change le comportement
     * de la fenêtre lors d'une demande de fermeture.Celle-ci
     * doit lancer le handle comme si l'invitation a été refusée.
     *
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
        dialogStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                handleRefuse();
            }
        });
    }

    /**
     * Désactive les boutons de la fenêtre initiale.
     *
     * @return true si jamais le bouton OK a été appuyé, false sinon.
     */
    public boolean isOkClicked() {
        Client.getController().disableButtons();
        return okClicked;
    }

    /**
     * Handler appelé lorsque le bouton refusé est appuyé ce qui ferme la fenêtre.
     */
    @FXML
    private void handleRefuse() {
        dialogStage.close();
    }

    /**
     * Appellé quand le bouton accepté est appuyé, change la valeur du boolean okClicked, notifie
     * l'utilisateur étant à la source de l'invitation puis ferme la fenêtre.
     */
    @FXML
    private void handleOk() {
        okClicked = true;
        Client.acceptInvite(idFrom, groupID);
        dialogStage.close();

    }
}