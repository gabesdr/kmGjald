module is.vidmot {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens is.vidmot to javafx.fxml;
    exports is.vidmot;
    exports is.vidmot.vinnsla;
    opens is.vidmot.vinnsla to javafx.fxml;
}