package com.dario.clinapp.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class MainScreenController {

    @FXML
    private BorderPane mainBorderPane;

    public void initialize() {
        showPacientes();
    }

    private void loadCenter(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            mainBorderPane.setCenter(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showSesiones(){
        loadCenter("/com/dario/clinapp/view/Sesiones.fxml");
    }

    @FXML
    private void showPacientes(){
        loadCenter("/com/dario/clinapp/view/Pacientes.fxml");
    }

    @FXML
    private void showPagos(){
        loadCenter("/com/dario/clinapp/view/Pagos.fxml");
    }

    @FXML
    private void showInformes(){
        loadCenter("/com/dario/clinapp/view/Informes.fxml");
    }

}
