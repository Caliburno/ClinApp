package com.dario.clinapp;

import com.dario.clinapp.dao.database.ServiceManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Application;

public class ClinAppMain extends Application
{
    @Override
    public void start(Stage primaryStage) throws Exception {

        ServiceManager.initialize();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/dario/clinapp/view/MainScreen.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setScene(scene);
        primaryStage.setTitle("ClinApp - Login");
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main( String[] args )
    {
launch(args);
    }
}
