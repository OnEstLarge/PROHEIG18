package views;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import javafx.scene.control.TextField;

public class InviteDialogController {

    private Stage dialogStage;
    private boolean okClicked = false;

    @FXML
    private TextField groupeIDField;

    @FXML
    private TextField newUserIPField;

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
        if(isInputValid()){
            Label newIp = new Label();
            newIp.setText(newUserIPField.getText());
            int group = Integer.parseInt(groupeIDField.getText())-1;
            RootLayoutController.getListView().get(group).getItems().add(newIp);

            okClicked = true;
            dialogStage.close();
        }
    }

    private boolean isInputValid(){
        String errorMessage="";
        if(groupeIDField.getText() == null || groupeIDField.getText().length() == 0){
            errorMessage += "Group ID false.\n";
        }else{
            try{
                int id = Integer.parseInt(groupeIDField.getText());
                if(id -1 > RootLayoutController.getListView().size()){
                    errorMessage += "Group ID invalid. No group with this id.\n";
                }
            }catch(NumberFormatException e){
                errorMessage += "No valid format for groupe ID (must be an int)!\n";
            }
        }

        if (newUserIPField.getText() == null || newUserIPField.getText().length() == 0) {
            errorMessage += "IP false.\n";
        }else{
            String ip = newUserIPField.getText();
            if(ip == "faux"){
                errorMessage += "IP invalid format.\n";
            }
        }
        if(errorMessage.length() == 0){
            return true;
        }else{
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