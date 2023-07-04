package gb.safronov.client_160622;

import gb.safronov.client_160622.controllers.ChatController;
import gb.safronov.client_160622.controllers.SignController;
import gb.safronov.client_160622.models.Network;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class StartClient extends Application {

    private Network network;
    private Stage primaryStage;
    private Stage authStage;
    private ChatController chatController;
    private SignController signController;
    public long a = System.currentTimeMillis();


    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        network = new Network();
        network.setStartClient(this);
        network.connect();
        openAuthDialog(a);
        createChatDialog(a);

    }

    private void openAuthDialog(long a) throws IOException {
            FXMLLoader authLoader = new FXMLLoader(StartClient.class.getResource("auth-view.fxml"));
            authStage = new Stage();
            Scene scene = new Scene(authLoader.load(), 603, 400);

            authStage.setScene(scene);
            authStage.initModality(Modality.WINDOW_MODAL);
            authStage.initOwner(primaryStage);
            authStage.setTitle("Authentication!");
            //authStage.setY(1400);
            //authStage.setX(650);
            authStage.setAlwaysOnTop(true);
            authStage.show();
        PauseTransition delay = new PauseTransition(Duration.seconds(25));
        delay.setOnFinished(event -> {
            authStage.close();
        });
        delay.play();




            SignController signController = authLoader.getController();

            signController.setNetwork(network);
            signController.setStartClient(this);


    }



    private void createChatDialog(long a) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(StartClient.class.getResource("chat-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 603, 400);

        primaryStage.setScene(scene);
        //primaryStage.setY(1400);
        //primaryStage.setX(650);
        primaryStage.setAlwaysOnTop(true);
//        primaryStage.show();


        chatController = fxmlLoader.getController();
        chatController.setNetwork(network);
        chatController.setStartClient(this);
    }

    public void openChatDialog(String username) {
        authStage.close();
        primaryStage.show();
        primaryStage.setTitle(network.getUsername());

        network.waitMessage(chatController);
        chatController.addUserToListView(network.online);
        chatController.setUsernameTitle(username);
        /*try {
            loadHistory();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    private void loadHistory() throws IOException {
        int posHistory = 100;
        File history = new File("history.txt");
        List<String> historyList = new ArrayList<>();
        FileInputStream in = new FileInputStream(history);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

        String temp;
        while ((temp = bufferedReader.readLine()) != null) {
            historyList.add(temp);
        }

        if (historyList.size() > posHistory) {
            for (int i = historyList.size() - posHistory; i <= (historyList.size() - 1); i++) {
               // ChatController.chatHistory.appendText(historyList.get(i) + "\n");
            }
        } else {
            for (int i = 0; i < posHistory; i++) {
                System.out.println(historyList.get(i));
            }
        }
    }

    public void showErrorAlert(String title, String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(errorMessage);
        alert.show();
    }

    public void showInfoAlert(String title, String infoMessage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(infoMessage);
        alert.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public void closeAuth() {
        authStage.close();
    }
}