package ua.nure.ai.common;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MessageSerializer {

    private final ObjectMapper objectMapper;

    public MessageSerializer() {
        this.objectMapper = new ObjectMapper();
    }

    public String serialize(Message message) throws Exception {
        return objectMapper.writeValueAsString(message);
    }

    public Message deserialize(String json) throws Exception {
        return objectMapper.readValue(json, Message.class);
    }
}
