package ua.nure.ai.common;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = JoinMessage.class, name = "join"),
    @JsonSubTypes.Type(value = TextMessage.class, name = "text"),
    @JsonSubTypes.Type(value = ConnectedUsersMessage.class, name = "connected_users")
})
public abstract class Message {
}
