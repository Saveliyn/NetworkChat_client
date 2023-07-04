package gb.safronov.client_160622.controllers;

import gb.safronov.client_160622.StartClient;
import gb.safronov.client_160622.models.Network;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;


public class ChatController {

    @FXML
    public ListView<String> usersList;

    @FXML
    private Label usernameTitle;

    @FXML
    public  TextArea chatHistory;

    @FXML
    private TextField inputField;

    @FXML
    private Button update;

    @FXML
    private TextField usernameField;

    @FXML
    private Button sendButton;
    private Network network;
    private String selectedRecipient;
    private StartClient startClient;

    @FXML
    public void initialize() {
        usersList.refresh();

        sendButton.setOnAction(event -> sendMessage());
        inputField.setOnAction(event -> sendMessage());
        update.setOnAction(event -> changeUsername());
        usernameField.setOnAction(event -> changeUsername());

        usersList.setCellFactory(lv -> {
            MultipleSelectionModel<String> selectionModel = usersList.getSelectionModel();
            ListCell<String> cell = new ListCell<>();
            
            cell.textProperty().bind(cell.itemProperty());
            cell.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                usersList.requestFocus();
                if (!cell.isEmpty()) {
                    int index = cell.getIndex();
                    if (selectionModel.getSelectedIndices().contains(index)) {
                        selectionModel.clearSelection(index);
                        selectedRecipient = null;
                    } else {
                        selectionModel.select(index);
                        selectedRecipient = cell.getItem();
                    }
                    event.consume();
                }
            });
            return cell;
        });
    }



    @FXML
    void changeUsername() {
        String message = usernameField.getText().trim();
        usernameField.clear();

        if (message.isEmpty()) {
            return;
        }

        network.changeUsernameMessage(message);
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        inputField.clear();

        if (message.isEmpty()) {
            return;
        }

//        appendMessage(message);
//        network.sendMessage(message);

        if (selectedRecipient != null) {
            network.sendPrivateMessage(selectedRecipient, message);
        } else {
            network.sendMessage(message);
        }
    }

    public void addUserToListView(ArrayList<String> username) {
        //System.out.println(username);
        usersList.setItems(FXCollections.observableArrayList(username));

    }

    public void appendMessage(String sender, String message) {
        String timeStamp = DateFormat.getInstance().format(new Date());

        chatHistory.appendText(timeStamp);
        chatHistory.appendText(System.lineSeparator());
        chatHistory.appendText(String.format("%s: %s", sender, message));
        chatHistory.appendText(System.lineSeparator());
        chatHistory.appendText(System.lineSeparator());
//        chatHistory.setText(new StringBuilder(chatHistory.getText()).insert(0, message).toString());

    }


    public void appendServerMessage( String message) {
        chatHistory.appendText(String.format("Внимание! %s", message));
        chatHistory.appendText(System.lineSeparator());
        chatHistory.appendText(System.lineSeparator());
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public void setUsernameTitle(String usernameTitleStr) {
        this.usernameTitle.setText(usernameTitleStr);
    }

    public void setStartClient(StartClient startClient) {
        this.startClient = startClient;
    }

    public StartClient getStartClient() {
        return startClient;
    }

    public void appendHistory(String message) {
        chatHistory.appendText(String.format(message));
        chatHistory.appendText(System.lineSeparator());
        chatHistory.appendText(System.lineSeparator());
    }
}
