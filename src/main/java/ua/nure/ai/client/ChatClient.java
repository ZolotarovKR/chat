package ua.nure.ai.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ua.nure.ai.common.ConnectedUsersMessage;
import ua.nure.ai.common.JoinMessage;
import ua.nure.ai.common.Message;
import ua.nure.ai.common.MessageSerializer;
import ua.nure.ai.common.TextMessage;

public class ChatClient {

    private static final Logger logger = LoggerFactory.getLogger(ChatClient.class);

    private final String username;
    private final String serverAddress;
    private final int serverPort;
    private final ObservableList<String> connectedUsers;
    private final ObservableList<TextMessage> messages;
    private Socket socket;
    private BufferedReader input;
    private BufferedWriter output;
    private final MessageSerializer messageSerializer;

    public ChatClient(String username, String serverAddress, int serverPort) {
        this.username = username;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.connectedUsers = FXCollections.observableArrayList();
        this.messages = FXCollections.observableArrayList();
        this.messageSerializer = new MessageSerializer();
    }

    public void connect() throws IOException {
        socket = new Socket(serverAddress, serverPort);
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        sendJoinMessage();

        new Thread(this::listenForMessages).start();
    }

    private void sendJoinMessage() {
        try {
            JoinMessage joinMessage = new JoinMessage(username);
            String json = messageSerializer.serialize(joinMessage);
            sendMessage(json);
        } catch (Exception e) {
            logger.error("Failed to send join message", e);
        }
    }

    public void sendTextMessage(String recipient, String text) {
        try {
            TextMessage textMessage = new TextMessage(username, recipient, text);
            String json = messageSerializer.serialize(textMessage);
            sendMessage(json);
        } catch (Exception e) {
            logger.error("Failed to send text message", e);
        }
    }

    private void sendMessage(String message) throws IOException {
        output.write(message);
        output.newLine();
        output.flush();
    }

    private void listenForMessages() {
        try {
            String line;
            while ((line = input.readLine()) != null) {
                processIncomingMessage(line);
            }
        } catch (IOException e) {
            logger.error("Error while listening for messages", e);
        }
    }

    private void processIncomingMessage(String json) {
        try {
            Message message = messageSerializer.deserialize(json);

            switch (message) {
                case ConnectedUsersMessage connectedUsersMessage -> {
                    Platform.runLater(() -> {
                        connectedUsers.clear();
                        connectedUsers.addAll(connectedUsersMessage.getUsernames());
                    });
                    logger.info("Updated connected users: {}", connectedUsers);

                }
                case JoinMessage joinMessage -> {
                    Platform.runLater(() -> {
                        connectedUsers.add(joinMessage.getUsername());
                    });
                    logger.info("User joined: {}", joinMessage.getUsername());

                }
                case TextMessage textMessage -> {
                    Platform.runLater(() -> {
                        messages.add(textMessage);
                    });
                    logger.info("Received message from {}: {}", textMessage.getSender(), textMessage.getText());
                }
                default -> {
                }
            }

        } catch (Exception e) {
            logger.error("Failed to process incoming message", e);
        }
    }

    public ObservableList<String> getConnectedUsers() {
        return connectedUsers;
    }

    public ObservableList<TextMessage> getMessages() {
        return messages;
    }

    public String getUsername() {
        return username;
    }

    public void disconnect() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            logger.error("Failed to disconnect", e);
        }
    }
}
