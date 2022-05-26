module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires io.netty.transport;
    requires io.netty.codec;
    requires io.netty.buffer;
    requires java.logging;
    requires java.sql;

    exports org.example.client;
    opens org.example.client to javafx.fxml;

}