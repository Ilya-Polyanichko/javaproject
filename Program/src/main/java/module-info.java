module org.program.program {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires jaudiotagger;
    requires java.sql;

    opens org.program.program to javafx.fxml;
    exports org.program.program;
}