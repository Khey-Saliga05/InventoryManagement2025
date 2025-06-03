package com.example.ims2;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;

public class LoginController {
    @FXML
    private TextField UsernameField;
    @FXML
    private PasswordField PasswordField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private CheckBox toggleShowPassword;

    @FXML
    protected void handleLogin() {
        String username = UsernameField.getText().trim();
        String password = PasswordField.isVisible() ? PasswordField.getText().trim() : passwordTextField.getText().trim();

        if ("admin".equals(username) && "password".equals(password)) {
            Session.setLoggedInUser(username);
            showWelcomeAlert(username);
            proceedToDashboard();
        } else if (AccountManager.validateLogin(username, password)) {
            Session.setLoggedInUser(username);
            String fullname = AccountManager.getFullnameByUsername(username);
            String role = AccountManager.getRoleByUsername(username);
            Session.setLoggedInFullname(fullname != null ? fullname : username);
            Session.setLoggedInRole(role != null ? role : "Viewer");

            AccountManager.saveLastLoggedInUser(username);
            showWelcomeAlert(username);

            // Optionally still retrieve or log the last action without displaying it
            String lastAction = LastActionManager.getLastAction(username);
            // You can log it if needed: System.out.println("Last Action: " + lastAction);

            proceedToDashboard();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Failed");
            alert.setHeaderText("Invalid Credentials");
            alert.setContentText("Please check your Username and Password.");
            alert.showAndWait();
        }
    }




    @FXML
    protected void goToCreateAcc(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Select Account Type");

        ToggleGroup toggleGroup = new ToggleGroup();
        RadioButton rbBusiness = new RadioButton("Administrator");
        rbBusiness.setToggleGroup(toggleGroup);
        RadioButton rbViewer = new RadioButton("Employee (Limited Access)");
        rbViewer.setToggleGroup(toggleGroup);

        VBox content = new VBox(10, rbBusiness, rbViewer);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() == ButtonType.CANCEL) {
            return;
        }

        if (rbBusiness.isSelected()) {
            // Custom dialog for admin password with show/hide functionality
            Dialog<String> pwdDialog = new Dialog<>();
            pwdDialog.setTitle("Admin Password Confirmation");
            pwdDialog.setHeaderText("Enter Admin password to create an Account:");

            PasswordField pwdField = new PasswordField();
            pwdField.setPromptText("Password");

            TextField pwdTextField = new TextField();
            pwdTextField.setPromptText("Password");
            pwdTextField.setManaged(false);
            pwdTextField.setVisible(false);

            CheckBox showPasswordCheckbox = new CheckBox("Show Password");
            showPasswordCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    pwdTextField.setText(pwdField.getText());
                    pwdTextField.setManaged(true);
                    pwdTextField.setVisible(true);
                    pwdField.setManaged(false);
                    pwdField.setVisible(false);
                } else {
                    pwdField.setText(pwdTextField.getText());
                    pwdField.setManaged(true);
                    pwdField.setVisible(true);
                    pwdTextField.setManaged(false);
                    pwdTextField.setVisible(false);
                }
            });

            VBox dialogContent = new VBox(10, pwdField, pwdTextField, showPasswordCheckbox);
            pwdDialog.getDialogPane().setContent(dialogContent);
            pwdDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            pwdDialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    return showPasswordCheckbox.isSelected() ? pwdTextField.getText() : pwdField.getText();
                }
                return null;
            });

            Optional<String> pwdResult = pwdDialog.showAndWait();
            if (pwdResult.isPresent()) {
                final String SYSTEM_ADMIN_PASSWORD = "admin123";
                if (pwdResult.get().equals(SYSTEM_ADMIN_PASSWORD)) {
                    Session.setCreatingRole("Admin");
                    loadCreateAccountScene(event);
                } else {
                    showErrorAlert("Incorrect Admin Password. Access denied.");
                }
            }
        } else if (rbViewer.isSelected()) {
            Session.setCreatingRole("Employee");
            loadCreateAccountScene(event);
        }
    }


    private void loadCreateAccountScene(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/ims2/" + "createnewacc.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Failed to load the Create Account screen.");
        }
    }


    private void proceedToDashboard() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/ims2/Main.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) UsernameField.getScene().getWindow();

                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                double width = screenBounds.getWidth();
                double height = screenBounds.getHeight();

                stage.setScene(new Scene(root, width, height));
                stage.setMaximized(true);
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                showErrorAlert("Failed to load the Dashboard.");
            }
        });
    }

    public void handleForgotPassword() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Forgot Password");
        dialog.setHeaderText("Find your account");
        dialog.setContentText("Enter your username:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(username -> {
            String password = findPasswordByUsername(username);
            if (password != null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Account Found");
                alert.setHeaderText("Here is your password:");
                alert.setContentText(password);  // Only okay if this is a non-secure, local app
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("User Not Found");
                alert.setHeaderText("Username not found");
                alert.setContentText("Please try again.");
                alert.showAndWait();
            }
        });
    }

    private String findPasswordByUsername(String username) {
        try (BufferedReader reader = new BufferedReader(new FileReader("accounts.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3 && parts[0].equals(username)) {
                    return parts[2];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    private void showWelcomeAlert(String username) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Login Successful");
        alert.setHeaderText(null);
        alert.setContentText("Welcome, " + username + "!");
        alert.showAndWait();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    protected void toggleShowPassword() {
        if (toggleShowPassword.isSelected()) {
            passwordTextField.setText(PasswordField.getText());
            passwordTextField.setVisible(true);
            PasswordField.setVisible(false);
        } else {
            PasswordField.setText(passwordTextField.getText());
            PasswordField.setVisible(true);
            passwordTextField.setVisible(false);
        }
    }
}
