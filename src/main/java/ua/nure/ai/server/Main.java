package ua.nure.ai.server;

public class Main {

    public static void main(String[] args) {
        ChatServer server = new ChatServer(8008);
        server.start();
    }
}
