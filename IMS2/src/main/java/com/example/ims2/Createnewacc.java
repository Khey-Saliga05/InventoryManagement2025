package com.example.ims2;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.*;
import java.util.Optional;

public class Createnewacc {
    @FXML
    private TextField UsernameField, fullnamefield;
    @FXML
    private PasswordField PasswordField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private PasswordField PasswordField1;
    @FXML
    private TextField passwordTextField1;
    @FXML
    private CheckBox toggleShowPassword;
    @FXML
    private Button btnReturn;

    private static final String ACCOUNTS_CSV = "accounts.csv";

    @FXML
    protected void handleCreateAcc() {
        String username = UsernameField.getText().trim();
        String fullname = fullnamefield.getText().trim();
        String password = PasswordField.isVisible() ? PasswordField.getText().trim() : passwordTextField.getText().trim();
        String confirmPassword = PasswordField1.isVisible() ? PasswordField1.getText().trim() : passwordTextField1.getText().trim();
        String role = Session.getCreatingRole(); // assume this is set before loading the scene


        if (username.isEmpty() || fullname.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showErrorAlert("Username, Full name, and password fields cannot be empty.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showErrorAlert("Passwords do not match. Please re-enter.");
            return;
        }

        if (!isPasswordStrong(password)) {
            showErrorAlert("Password must be at least 8 characters long and include a mix of uppercase, lowercase, numbers, and special characters.");
            return;
        }

        if (AccountManager.isUsernameTaken(username)) {
            showErrorAlert("Username already exists. Please choose a different one.");
            return;
        }

        if (AccountManager.saveNewAccount(username, fullname, password, role)) {
            showWelcomeAlert(username);
            proceedToLogIn();
        } else {
            showErrorAlert("Failed to create account. Please try again.");
        }
    }

    private boolean isPasswordStrong(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    }

    private void showWelcomeAlert(String username) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Account Created");
        alert.setHeaderText(null);
        alert.setContentText("Account Created Successfully :)");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    @FXML
    private void OnReturn() {
        proceedToLogIn();
    }

    private void proceedToLogIn() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/ims2/login.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) UsernameField.getScene().getWindow();
                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

                stage.setScene(new Scene(root, screenBounds.getWidth(), screenBounds.getHeight()));
                stage.setMaximized(true);
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                showErrorAlert("Failed to load the login screen.");
            }
        });
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Invalid Credentials");
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    @FXML
    protected void toggleShowPassword() {
        if (toggleShowPassword.isSelected()) {
            passwordTextField.setText(PasswordField.getText());
            passwordTextField.setVisible(true);
            PasswordField.setVisible(false);

            passwordTextField1.setText(PasswordField1.getText());
            passwordTextField1.setVisible(true);
            PasswordField1.setVisible(false);
        } else {
            PasswordField.setText(passwordTextField.getText());
            PasswordField.setVisible(true);
            passwordTextField.setVisible(false);

            PasswordField1.setText(passwordTextField1.getText());
            PasswordField1.setVisible(true);
            passwordTextField1.setVisible(false);
        }
    }
}
