package views;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import main.Client;

public class AcceptInviteDialogController {

    private Stage dialogStage;
    private boolean okClicked = false;

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public void setIdFrom(String idFrom) {
        this.idFrom = idFrom;
    }

    private String groupID;
    private String idFrom;



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
        dialogStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                handleRefuse();
            }
        });
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
        System.out.println("refusé");
        dialogStage.close();
    }

    /**
     * Called when the user clicks ok.
     */
    @FXML
    private void handleOk() {
        // TODO: ajouter le groupe sur l'interface et récuperer les fichiers.
        okClicked = true;
        System.out.println("accepté");
        Client.acceptInvite(idFrom, groupID);
        dialogStage.close();

    }
}