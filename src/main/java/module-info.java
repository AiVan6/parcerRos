module org.example.parserrosreestrkad {
    requires playwright;
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens org.example.parserrosreestrkad to javafx.fxml;
    exports org.example.parserrosreestrkad;
}