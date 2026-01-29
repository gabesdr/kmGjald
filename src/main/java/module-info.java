module is.vidmot {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;

    requires transitive org.controlsfx.controls;
    requires transitive org.kordamp.bootstrapfx.core;

    opens is.vidmot to javafx.fxml;
    exports is.vidmot;
    exports is.vidmot.vinnsla;
    opens is.vidmot.vinnsla to javafx.fxml;
}