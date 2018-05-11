package main;

import User.Person;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import views.AcceptInviteDialogController;
import views.InviteDialogController;
import views.PseudoDialogController;
import views.RootLayoutController;
import User.Group;


public class Main extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;
    private List<Group> groups = new ArrayList();
    private Person oli = new Person("Olivier", "file");
    private String userPseudo ="123";

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("ODEEP");
        initRootLayout();
    }

    public void initRootLayout() {
        if (true) {
            boolean ok = showPseudoDialog();
            while(!ok){ // Ask for a pseudo until a valid one is entered.
                ok = showPseudoDialog();
            }
        }
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/views/RootLayout.fxml"));
            rootLayout = loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);

            // Give the controller access to the main app.
            RootLayoutController controller = loader.getController();
            controller.setMainApp(this);
            controller.fillFileMap(groups);

            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean showInviteDialog() {
        try {
            // Load the FXML filer and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/views/InviteDialog.fxml"));
            AnchorPane page = loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Invite a member");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the invite controller
            InviteDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            // Show the dialog and wait  until the user closes it
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean showPseudoDialog(){
        try{
            // Load the FXML filer and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/views/PseudoDialog.fxml"));
            AnchorPane page = loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Choose your pseudo");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the invite controller
            PseudoDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setMainApp(this);

            // Show the dialog and wait  until the user closes it
            dialogStage.showAndWait();

            return controller.isNameOK();
        }catch(IOException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean showAcceptInviteDialog(String groupName){
        try{
            // Load the FXML filer and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/views/AcceptInviteDialog.fxml"));
            AnchorPane page = loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Group invitation");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the invite controller
            AcceptInviteDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.getMessageLabel().setText("Vous avez été invité dans le groupe " + groupName);

            // Show the dialog and wait  until the user closes it
            dialogStage.showAndWait();

            return controller.isOkClicked();
        }catch(IOException e){
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Returns the main stage.
     *
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public String getUserPseudo() {
        return userPseudo;
    }

    public void setUserPseudo(String pseudo) {
        this.userPseudo = pseudo;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
