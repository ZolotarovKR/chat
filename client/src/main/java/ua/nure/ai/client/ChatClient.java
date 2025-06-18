package ua.nure.ai.client;

import ua.nure.ai.common.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatClient {

    private static final Logger logger = LoggerFactory.getLogger(ChatClient.class);

    private final String username;
    private final String serverAddress;
    private final int serverPort;
    private final List<String> connectedUsers;
    private final List<TextMessage> messages;
    private Socket socket;
    private BufferedReader input;
    private BufferedWriter output;
    private final MessageSerializer messageSerializer;

    public ChatClient(String username, String serverAddress, int serverPort) {
        this.username = username;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.connectedUsers = new ArrayList<>();
        this.messages = new ArrayList<>();
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
                    connectedUsers.clear();
                    connectedUsers.addAll(connectedUsersMessage.getUsernames());
                    logger.info("Updated connected users: {}", connectedUsers);

                }
                case JoinMessage joinMessage -> {
                    connectedUsers.add(joinMessage.getUsername());
                    logger.info("User joined: {}", joinMessage.getUsername());

                }
                case TextMessage textMessage -> {
                    messages.add(textMessage);
                    logger.info("Received message from {}: {}", textMessage.getSender(), textMessage.getText());
                }
                default -> {
                }
            }

        } catch (Exception e) {
            logger.error("Failed to process incoming message", e);
        }
    }

    public List<String> getConnectedUsers() {
        return new ArrayList<>(connectedUsers);
    }

    public List<TextMessage> getMessages() {
        return new ArrayList<>(messages);
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
