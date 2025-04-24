package com.uno.client;

import com.uno.client.vista.PantallaConexion;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class UNOClientApp extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        // Configurar ventana principal
        primaryStage.setTitle("UNO - Cliente");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icono.png")));
        
        // Mostrar pantalla de conexión
        PantallaConexion pantallaConexion = new PantallaConexion(primaryStage);
        pantallaConexion.mostrar();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}