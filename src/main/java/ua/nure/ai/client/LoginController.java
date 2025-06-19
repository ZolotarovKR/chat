package ua.nure.ai.client;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField addressTextField;

    @FXML
    private TextField portTextField;

    @FXML
    private TextField usernameTextField;

    @FXML
    private Button connectButton;

    @FXML
    private void connect() {
        try {
            ChatClient client = new ChatClient(
                    usernameTextField.getText(),
                    addressTextField.getText(),
                    Integer.parseInt(portTextField.getText())
            );
            client.connect();
            App.openChat(client);
        } catch (IOException | NumberFormatException e) {
        }
    }

}
