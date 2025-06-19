module ua.nure.ai.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires ch.qos.logback.classic;
    requires org.slf4j;
    requires ua.nure.ai.common;

    opens ua.nure.ai.client to javafx.fxml;
    exports ua.nure.ai.client;
}
