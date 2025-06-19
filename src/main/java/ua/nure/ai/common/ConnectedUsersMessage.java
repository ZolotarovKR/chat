package ua.nure.ai.common;

import java.util.List;

public class ConnectedUsersMessage extends Message {

    private List<String> usernames;

    public ConnectedUsersMessage() {
    }

    public ConnectedUsersMessage(List<String> usernames) {
        this.usernames = usernames;
    }

    public List<String> getUsernames() {
        return usernames;
    }

    public void setUsernames(List<String> usernames) {
        this.usernames = usernames;
    }
}
