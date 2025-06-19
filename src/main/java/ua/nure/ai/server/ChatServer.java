package ua.nure.ai.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.nure.ai.common.ConnectedUsersMessage;
import ua.nure.ai.common.JoinMessage;
import ua.nure.ai.common.Message;
import ua.nure.ai.common.MessageSerializer;
import ua.nure.ai.common.TextMessage;

public class ChatServer {

    private static final Logger logger = LoggerFactory.getLogger(ChatServer.class);

    private final int port;
    private final List<TextMessage> textMessages;
    private final Map<String, List<Socket>> userSockets;
    private final MessageSerializer messageSerializer;

    public ChatServer(int port) {
        this.port = port;
        this.textMessages = new ArrayList<>();
        this.userSockets = new ConcurrentHashMap<>();
        this.messageSerializer = new MessageSerializer();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Server started on port {}", port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("New client connected: {}", clientSocket.getRemoteSocketAddress());

                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            logger.error("Error starting the server", e);
        }
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

            String line;
            while ((line = input.readLine()) != null) {
                processIncomingMessage(line, clientSocket);
            }
        } catch (IOException e) {
            logger.error("Error handling client: {}", clientSocket.getRemoteSocketAddress(), e);
        } finally {
            removeSocket(clientSocket);
        }
    }

    private void processIncomingMessage(String json, Socket clientSocket) {
        try {
            Message message = messageSerializer.deserialize(json);

            switch (message) {
                case JoinMessage joinMessage ->
                    handleJoinMessage(joinMessage, clientSocket);
                case TextMessage textMessage ->
                    handleTextMessage(textMessage);
                default -> {
                }
            }
        } catch (Exception e) {
            logger.error("Failed to process incoming message", e);
        }
    }

    private void handleJoinMessage(JoinMessage joinMessage, Socket clientSocket) {
        String username = joinMessage.getUsername();

        synchronized (userSockets) {
            boolean isNewUser = !userSockets.containsKey(username);

            if (isNewUser) {
                userSockets.put(username, new ArrayList<>());
                broadcastMessage(joinMessage);
            }

            List<Socket> sockets = userSockets.get(username);
            sockets.add(clientSocket);

            sendConnectedUsersMessage(clientSocket);
        }
    }

    private void handleTextMessage(TextMessage textMessage) {
        String sender = textMessage.getSender();
        String recipient = textMessage.getRecipient();

        textMessages.add(textMessage);

        synchronized (userSockets) {
            for (Socket socket : userSockets.get(sender)) {
                sendMessageToSocket(textMessage, socket);
            }
            for (Socket socket : userSockets.get(recipient)) {
                sendMessageToSocket(textMessage, socket);
            }
        }
    }

    private void sendConnectedUsersMessage(Socket socket) {
        try {
            List<String> usernames = new ArrayList<>(userSockets.keySet());
            ConnectedUsersMessage connectedUsersMessage = new ConnectedUsersMessage(usernames);
            sendMessageToSocket(connectedUsersMessage, socket);
        } catch (Exception e) {
            logger.error("Failed to send connected users message", e);
        }
    }

    private void sendMessageToSocket(Message message, Socket socket) {
        try {
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            String json = messageSerializer.serialize(message);
            output.write(json);
            output.newLine();
            output.flush();
        } catch (Exception e) {
            logger.error("Failed to send message to socket: {}", socket.getRemoteSocketAddress(), e);
        }
    }

    private void broadcastMessage(Message message) {
        synchronized (userSockets) {
            for (List<Socket> sockets : userSockets.values()) {
                for (Socket socket : sockets) {
                    sendMessageToSocket(message, socket);
                }
            }
        }
    }

    private void removeSocket(Socket clientSocket) {
        synchronized (userSockets) {
            for (List<Socket> sockets : userSockets.values()) {
                sockets.remove(clientSocket);
            }
        }
        logger.info("Client disconnected: {}", clientSocket.getRemoteSocketAddress());
    }
}
