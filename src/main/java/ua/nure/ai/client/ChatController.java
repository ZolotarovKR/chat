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
    private void sendTextMessage() {
        String message = messageTextField.getText();
        if (message.isBlank()) {
            return;
        }
        String recipient = usersListView.getSelectionModel().getSelectedItem().toString();
        client.sendTextMessage(recipient, message);
        messageTextField.clear();
    }

    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            usersListView.setItems(client.getConnectedUsers());
        });
        messagesListView.setItems(client.getMessages());
        // usersListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
        //     if (newValue != null) {
        //         messagesListView.setItems(client.getMessages().filtered(predicate -> {
        //             TextMessage message = (TextMessage) predicate;
        //             return message.getRecipient().equals(newValue) || message.getSender().equals(newValue);
        //         }));
        //     }
        // });
    }
}
