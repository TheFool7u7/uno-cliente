package com.uno.client.vista;

import com.uno.client.controlador.ConexionServidor;
import com.uno.client.modelo.EstadoJuego;
import com.uno.server.util.Mensaje;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PantallaConexion {
    private Stage primaryStage;
    private Scene scene;
    private TextField txtNombre;
    private TextField txtHost;
    private TextField txtPuerto;
    private Button btnConectar;
    
    public PantallaConexion(Stage primaryStage) {
        this.primaryStage = primaryStage;
        crearInterfaz();
    }
    
    private void crearInterfaz() {
        // Grid para formulario
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        
        // Título
        Label titulo = new Label("Conexión al Servidor UNO");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        grid.add(titulo, 0, 0, 2, 1);
        
        // Campo de nombre
        grid.add(new Label("Nombre:"), 0, 1);
        txtNombre = new TextField();
        txtNombre.setPromptText("Ingresa tu nombre");
        grid.add(txtNombre, 1, 1);
        
        // Campo de host
        grid.add(new Label("Host:"), 0, 2);
        txtHost = new TextField("localhost");
        grid.add(txtHost, 1, 2);
        
        // Campo de puerto
        grid.add(new Label("Puerto:"), 0, 3);
        txtPuerto = new TextField("5000");
        grid.add(txtPuerto, 1, 3);
        
        // Botón de conexión
        btnConectar = new Button("Conectar");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btnConectar);
        grid.add(hbBtn, 1, 4);
        
        // Acción del botón conectar
        btnConectar.setOnAction(e -> {
            conectarAlServidor();
        });
        
        // Layout principal
        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().add(grid);
        
        // Crear la escena
        scene = new Scene(vbox, 400, 300);
        
        // Agregar estilos CSS
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
    }
    
    private void conectarAlServidor() {
        String nombre = txtNombre.getText().trim();
        String host = txtHost.getText().trim();
        String puertoStr = txtPuerto.getText().trim();
        
        if (nombre.isEmpty()) {
            mostrarAlerta("Error", "Debes ingresar un nombre");
            return;
        }
        
        try {
            int puerto = Integer.parseInt(puertoStr);
            
            // Crear conexión con el servidor
            ConexionServidor conexion = new ConexionServidor(host, puerto, mensaje -> {
                Platform.runLater(() -> {
                    // Manejar mensajes recibidos del servidor
                    if (mensaje.getTipo() == Mensaje.Tipo.CHAT) {
                        // La pantalla de juego manejará esto después
                    }
                });
            });
            
            // Intentar conectar
            boolean exito = conexion.conectar(nombre);
            
            if (exito) {
                // Guardar información del jugador
                EstadoJuego.getInstancia().setMiId(0); // Será actualizado por el primer mensaje del servidor
                EstadoJuego.getInstancia().setMiNombre(nombre);
                
                // Cambiar a la pantalla de juego
                PantallaJuego pantallaJuego = new PantallaJuego(primaryStage, conexion);
                pantallaJuego.mostrar();
            } else {
                mostrarAlerta("Error", "No se pudo conectar al servidor");
            }
            
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "El puerto debe ser un número");
        }
    }
    
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    public void mostrar() {
        primaryStage.setTitle("UNO - Conexión");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}