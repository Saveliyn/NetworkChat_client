package gb.safronov.client_160622.controllers;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import gb.safronov.client_160622.StartClient;
import gb.safronov.client_160622.models.Network;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SignController {

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField loginReg;

    @FXML
    private TextField passReg;

    @FXML
    private TextField usernameReg;
    private Network network;
    private StartClient startClient;


    @FXML
    void initialize() {

    }

    @FXML
    void checkAuth(ActionEvent event) {
            String login = loginField.getText().trim();
            String password = passwordField.getText().trim();

            if (login.length() == 0 || password.length() == 0) {
                System.out.println("Ошибка ввода при аутентификации");
                System.out.println();
                startClient.showErrorAlert("Ошибка ввода при аутентификации", "Поля не должны быть пустыми");
                return;
            }

            if (login.length() > 32 || password.length() > 32) {
                startClient.showErrorAlert("Ошибка ввода при аутентификации", "Длина логина и пароля должны быть не более 32 символов");
                return;
            }

            String authErrorMessage = network.sendAuthMessage(login, password);

            if (authErrorMessage == null) {
                startClient.openChatDialog(network.getUsername());
            } else {
                startClient.showErrorAlert("Ошибка аутентификации", authErrorMessage);
            }

    }



    @FXML
    void signUp(ActionEvent event) {
        String login = loginReg.getText().trim();
        String password = passReg.getText().trim();
        String username = usernameReg.getText().trim();

        String regErrorMessage = network.sendRegMessage(login, password, username);

        if(regErrorMessage == null){
            startClient.openChatDialog(username);
        } else {
            startClient.showErrorAlert("Ошибка регистрации", regErrorMessage);
        }
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public Network getNetwork() {
        return network;
    }

    public void setStartClient(StartClient startClient) {
        this.startClient = startClient;
    }

    public StartClient getStartClient() {
        return startClient;
    }
}
