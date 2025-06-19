package ua.nure.ai.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class ChatController {

    private ChatClient client;

    public void setClient(ChatClient client) {
        this.client = client;
        usersListView.getItems().addAll(client.getConnectedUsers());
        messagesListView.getItems().addAll(client.getMessages());
    }

    @FXML
    private TextField messageTextField;

    @FXML
    private Button sendButton;

    @FXML
    private ListView usersListView;

    @FXML
    private ListView messagesListView;

    @FXML
    private void sendTextMessage(ActionEvent event) {
        String message = messageTextField.getText();
        if (message.isBlank()) {
            return;
        }
        System.err.println(message);
        messageTextField.clear();
    }

    @FXML
    private void initialize() {
        sendButton.setDisable(true);
        Platform.runLater(() -> {
            usersListView.setItems(client.getConnectedUsers());
        });
    }

}
