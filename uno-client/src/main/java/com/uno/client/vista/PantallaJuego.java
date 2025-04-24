package com.uno.client.vista;

import com.uno.client.controlador.ConexionServidor;
import com.uno.client.modelo.EstadoJuego;
import com.uno.server.modelo.Carta;
import com.uno.server.modelo.Jugador;
import com.uno.server.util.Mensaje;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.util.Optional;

public class PantallaJuego {

    private Stage primaryStage;
    private Scene scene;
    private ConexionServidor conexion;

    // Componentes de la interfaz
    private HBox mazosContainer;
    private HBox cartasJugadorContainer;
    private VBox infoJugadores;
    private TextArea chatArea;
    private TextField chatInput;
    private Label lblTurno;
    private Label lblColorActual;
    private Button btnRobar;
    private Button btnUNO;
    private Button btnIniciarJuego;

    // Ruta base para las imágenes (se usará un placeholder hasta que agregues las verdaderas)
    private final String IMG_PATH = "/images/";

    public PantallaJuego(Stage primaryStage, ConexionServidor conexion) {
        this.primaryStage = primaryStage;
        this.conexion = conexion; // Usa la instancia pasada como parámetro
        crearInterfaz();
    }

    private void crearInterfaz() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Panel superior: información del juego
        HBox panelSuperior = crearPanelSuperior();
        root.setTop(panelSuperior);

        // Panel central: mazos y cartas del jugador
        VBox panelCentral = crearPanelCentral();
        root.setCenter(panelCentral);

        // Panel derecho: lista de jugadores y chat
        VBox panelDerecho = crearPanelDerecho();
        root.setRight(panelDerecho);

        scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
    }

    private HBox crearPanelSuperior() {
        HBox panel = new HBox(20);
        panel.setPadding(new Insets(10));
        panel.setAlignment(Pos.CENTER_LEFT);

        lblTurno = new Label("Esperando jugadores...");
        lblColorActual = new Label("Color actual: -");
        btnIniciarJuego = new Button("Iniciar Juego");

        btnIniciarJuego.setOnAction(e -> {
            conexion.iniciarJuego();
            btnIniciarJuego.setDisable(true);
        });

        panel.getChildren().addAll(lblTurno, lblColorActual, btnIniciarJuego);
        return panel;
    }

    private VBox crearPanelCentral() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(10));
        panel.setAlignment(Pos.CENTER);

        // Contenedor para el mazo y última carta jugada
        mazosContainer = new HBox(20);
        mazosContainer.setAlignment(Pos.CENTER);

        // Crea la imagen del reverso de la carta para el mazo
        ImageView mazoImageView = new ImageView(new Image(getClass().getResourceAsStream(IMG_PATH + "reverso.png")));
        mazoImageView.setFitWidth(100);
        mazoImageView.setFitHeight(150);

        // Placeholder para la última carta jugada
        ImageView ultimaCartaImageView = new ImageView();
        ultimaCartaImageView.setFitWidth(100);
        ultimaCartaImageView.setFitHeight(150);

        mazosContainer.getChildren().addAll(mazoImageView, ultimaCartaImageView);

        // Botones de acción
        HBox botonesContainer = new HBox(20);
        botonesContainer.setAlignment(Pos.CENTER);

        btnRobar = new Button("Robar Carta");
        btnUNO = new Button("¡UNO!");

        btnRobar.setOnAction(e -> {
            conexion.robarCarta(EstadoJuego.getInstancia().getMiId());
        });

        btnUNO.setOnAction(e -> {
            conexion.gritarUNO(EstadoJuego.getInstancia().getMiId());
        });

        botonesContainer.getChildren().addAll(btnRobar, btnUNO);

        // Contenedor para las cartas del jugador
        cartasJugadorContainer = new HBox(5);
        cartasJugadorContainer.setAlignment(Pos.CENTER);
        ScrollPane scrollCartas = new ScrollPane(cartasJugadorContainer);
        scrollCartas.setFitToHeight(true);
        scrollCartas.setPrefHeight(180);
        scrollCartas.setStyle("-fx-background-color: transparent;");

        panel.getChildren().addAll(mazosContainer, botonesContainer, new Label("Tus cartas:"), scrollCartas);
        return panel;
    }

    private VBox crearPanelDerecho() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(250);

        // Lista de jugadores
        Label lblJugadores = new Label("Jugadores:");
        infoJugadores = new VBox(5);

        // Chat
        Label lblChat = new Label("Chat:");
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setPrefHeight(200);

        chatInput = new TextField();
        chatInput.setPromptText("Escribe un mensaje...");

        Button btnEnviar = new Button("Enviar");
        btnEnviar.setOnAction(e -> {
            String mensaje = chatInput.getText().trim();
            if (!mensaje.isEmpty()) {
                conexion.enviarMensajeChat(mensaje);
                chatInput.clear();
            }
        });

        HBox chatControls = new HBox(5, chatInput, btnEnviar);
        chatControls.setAlignment(Pos.CENTER_RIGHT);

        panel.getChildren().addAll(lblJugadores, infoJugadores, lblChat, chatArea, chatControls);
        return panel;
    }

    private void manejarMensaje(Mensaje mensaje) {
        Platform.runLater(() -> {
            switch (mensaje.getTipo()) {
                case ACTUALIZAR_ESTADO:
                    actualizarInterfaz();
                    break;
                case CHAT:
                    chatArea.appendText(mensaje.getContenido() + "\n");
                    break;
                default:
                    break;
            }
        });
    }

    private void actualizarInterfaz() {
        EstadoJuego estado = EstadoJuego.getInstancia();

        // Actualizar información del turno
        if (estado.getJugadores() != null && estado.getJugadorActual() >= 0
                && estado.getJugadorActual() < estado.getJugadores().size()) {
            String nombreJugadorActual = estado.getJugadores().get(estado.getJugadorActual()).getNombre();
            lblTurno.setText("Turno de: " + nombreJugadorActual);

            if (estado.esmiTurno()) {
                lblTurno.setStyle("-fx-font-weight: bold; -fx-text-fill: green;");
            } else {
                lblTurno.setStyle("-fx-font-weight: normal; -fx-text-fill: black;");
            }
        }

        // Actualizar color actual
        if (estado.getColorActual() != null) {
            lblColorActual.setText("Color actual: " + estado.getColorActual().toString());

            // Cambiar el color del texto según el color actual
            switch (estado.getColorActual()) {
                case ROJO:
                    lblColorActual.setTextFill(Color.RED);
                    break;
                case AZUL:
                    lblColorActual.setTextFill(Color.BLUE);
                    break;
                case VERDE:
                    lblColorActual.setTextFill(Color.GREEN);
                    break;
                case AMARILLO:
                    lblColorActual.setTextFill(Color.GOLD);
                    break;
                default:
                    lblColorActual.setTextFill(Color.BLACK);
                    break;
            }
        }

        // Actualizar última carta jugada
        Carta ultimaCarta = estado.getUltimaCarta();
        if (ultimaCarta != null) {
            ImageView ultimaCartaImageView = (ImageView) mazosContainer.getChildren().get(1);
            ultimaCartaImageView.setImage(new Image(getClass().getResourceAsStream(IMG_PATH + ultimaCarta.getNombreArchivo())));
        }

        // Actualizar cartas del jugador
        cartasJugadorContainer.getChildren().clear();

        Jugador miJugador = estado.getMiJugador();
        if (miJugador != null) {
            for (int i = 0; i < miJugador.getMano().size(); i++) {
                Carta carta = miJugador.getMano().get(i);
                ImageView cartaImageView = new ImageView(new Image(getClass().getResourceAsStream(IMG_PATH + carta.getNombreArchivo())));
                cartaImageView.setFitWidth(80);
                cartaImageView.setFitHeight(120);

                // Agregar evento para jugar la carta
                final int indice = i;
                cartaImageView.setOnMouseClicked(e -> {
                    if (estado.esmiTurno()) {
                        jugarCarta(indice);
                    }
                });

                cartasJugadorContainer.getChildren().add(cartaImageView);
            }
        }

        // Actualizar lista de jugadores
        infoJugadores.getChildren().clear();
        if (estado.getJugadores() != null) {
            for (int i = 0; i < estado.getJugadores().size(); i++) {
                Jugador jugador = estado.getJugadores().get(i);
                Label lblJugador = new Label(jugador.getNombre() + " - Cartas: " + jugador.cantidadCartas());

                if (i == estado.getJugadorActual()) {
                    lblJugador.setStyle("-fx-font-weight: bold;");
                }

                infoJugadores.getChildren().add(lblJugador);
            }
        }

        // Habilitar/deshabilitar botones según el turno
        btnRobar.setDisable(!estado.esmiTurno());
        btnUNO.setDisable(!estado.esmiTurno() || miJugador == null || miJugador.cantidadCartas() != 2);
    }

    private void jugarCarta(int indiceCarta) {
        EstadoJuego estado = EstadoJuego.getInstancia();
        Carta carta = estado.getMiJugador().getMano().get(indiceCarta);

        if (carta.getColor() == Carta.Color.COMODIN) {
            // Mostrar diálogo para elegir color
            mostrarDialogoElegirColor(indiceCarta);
        } else {
            // Jugar carta normal
            conexion.jugarCarta(estado.getMiId(), indiceCarta, null);
        }
    }

    private void mostrarDialogoElegirColor(int indiceCarta) {
        Dialog<Carta.Color> dialog = new Dialog<>();
        dialog.setTitle("Elegir Color");
        dialog.setHeaderText("Selecciona un color para tu carta comodín");

        ButtonType btnRojo = new ButtonType("Rojo", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnAzul = new ButtonType("Azul", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnVerde = new ButtonType("Verde", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnAmarillo = new ButtonType("Amarillo", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes().addAll(btnRojo, btnAzul, btnVerde, btnAmarillo);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnRojo) {
                return Carta.Color.ROJO;
            } else if (dialogButton == btnAzul) {
                return Carta.Color.AZUL;
            } else if (dialogButton == btnVerde) {
                return Carta.Color.VERDE;
            } else if (dialogButton == btnAmarillo) {
                return Carta.Color.AMARILLO;
            }
            return null;
        });

        Optional<Carta.Color> resultado = dialog.showAndWait();
        if (resultado.isPresent()) {
            // Enviar la carta con el color elegido
            conexion.jugarCarta(EstadoJuego.getInstancia().getMiId(), indiceCarta, resultado.get());
        }
    }

    public void mostrar() {
        primaryStage.setTitle("UNO - Juego");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            conexion.desconectar();
        });
    }
}
