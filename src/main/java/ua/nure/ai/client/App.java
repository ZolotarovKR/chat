package ua.nure.ai.client;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("login.fxml"));
        scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.setMinWidth(720);
        stage.setMinHeight(480);
        stage.show();
    }

    public static void openChat(ChatClient client) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("chat.fxml"));
            scene.setRoot(fxmlLoader.load());
            ChatController controller = fxmlLoader.getController();
            controller.setClient(client);
        } catch (IOException e) {
        }
    }

    public static void main(String[] args) {
        launch();
    }

}
