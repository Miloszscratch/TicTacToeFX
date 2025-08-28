module com.milosz.podsiadly.tictactoefx {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.milosz.podsiadly.tictactoefx to javafx.fxml;
    exports com.milosz.podsiadly.tictactoefx;
}
