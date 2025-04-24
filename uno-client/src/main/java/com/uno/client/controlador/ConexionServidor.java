package com.uno.client.controlador;

import com.uno.client.modelo.EstadoJuego;
import com.uno.server.modelo.Carta;
import com.uno.server.util.Mensaje;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Consumer;

public class ConexionServidor {
    private String host;
    private int puerto;
    private Socket socket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private boolean conectado;
    private Thread hiloRecepcion;
    
    // Callback para actualizar la interfaz
    private Consumer<Mensaje> manejadorMensajes;
    
    public ConexionServidor(String host, int puerto, Consumer<Mensaje> manejadorMensajes) {
        this.host = host;
        this.puerto = puerto;
        this.manejadorMensajes = manejadorMensajes;
        this.conectado = false;
    }
    
    public boolean conectar(String nombreJugador) {
        try {
            socket = new Socket(host, puerto);
            
            // Crear streams
            salida = new ObjectOutputStream(socket.getOutputStream());
            entrada = new ObjectInputStream(socket.getInputStream());
            
            conectado = true;
            
            // Enviar mensaje de conexión con el nombre del jugador
            enviarMensaje(new Mensaje(Mensaje.Tipo.CONEXION, nombreJugador));
            
            // Iniciar hilo de recepción
            hiloRecepcion = new Thread(() -> recibirMensajes());
            hiloRecepcion.setDaemon(true);
            hiloRecepcion.start();
            
            return true;
        } catch (IOException e) {
            System.err.println("Error al conectar con el servidor: " + e.getMessage());
            return false;
        }
    }
    
    private void recibirMensajes() {
        try {
            while (conectado) {
                Mensaje mensaje = (Mensaje) entrada.readObject();
                
                // Procesar el mensaje recibido
                procesarMensaje(mensaje);
                
                // Notificar a la interfaz gráfica
                manejadorMensajes.accept(mensaje);
            }
        } catch (IOException | ClassNotFoundException e) {
            if (conectado) {
                System.err.println("Error en la conexión con el servidor: " + e.getMessage());
                desconectar();
            }
        }
    }
    
    private void procesarMensaje(Mensaje mensaje) {
        switch (mensaje.getTipo()) {
            case ACTUALIZAR_ESTADO:
                // Actualizar el estado del juego en el cliente
                EstadoJuego.getInstancia().actualizarEstado(
                    mensaje.getJugadores(),
                    mensaje.getUltimaCarta(),
                    mensaje.getJugadorActual(),
                    mensaje.getColorActual(),
                    mensaje.isSentidoHorario()
                );
                break;
            default:
                // Los demás mensajes serán manejados por la interfaz gráfica
                break;
        }
    }
    
    public void enviarMensaje(Mensaje mensaje) {
        try {
            if (conectado) {
                salida.writeObject(mensaje);
                salida.flush();
            }
        } catch (IOException e) {
            System.err.println("Error al enviar mensaje al servidor: " + e.getMessage());
            desconectar();
        }
    }
    
    public void jugarCarta(int indiceJugador, int indiceCarta, Carta.Color colorElegido) {
        Mensaje mensaje = new Mensaje(Mensaje.Tipo.JUGAR_CARTA, indiceJugador, indiceCarta);
        if (colorElegido != null) {
            mensaje = new Mensaje(Mensaje.Tipo.JUGAR_CARTA, indiceJugador, indiceCarta);
            // Aquí habría que asignar el color elegido, pero hay un error en la definición de Mensaje
            // Ajustarlo según sea necesario
        }
        enviarMensaje(mensaje);
    }
    
    public void robarCarta(int indiceJugador) {
        Mensaje mensaje = new Mensaje(Mensaje.Tipo.ROBAR_CARTA);
        // Asignar el índice del jugador
        enviarMensaje(mensaje);
    }
    
    public void gritarUNO(int indiceJugador) {
        Mensaje mensaje = new Mensaje(Mensaje.Tipo.GRITAR_UNO);
        // Asignar el índice del jugador
        enviarMensaje(mensaje);
    }
    
    public void elegirColor(Carta.Color color) {
        enviarMensaje(new Mensaje(Mensaje.Tipo.ELEGIR_COLOR, color));
    }
    
    public void enviarMensajeChat(String contenido) {
        enviarMensaje(new Mensaje(
            Mensaje.Tipo.CHAT, 
            contenido, 
            EstadoJuego.getInstancia().getMiNombre()
        ));
    }
    
    public void iniciarJuego() {
        enviarMensaje(new Mensaje(Mensaje.Tipo.INICIAR_JUEGO));
    }
    
    public void desconectar() {
        conectado = false;
        
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            
            if (hiloRecepcion != null && hiloRecepcion.isAlive()) {
                hiloRecepcion.interrupt();
            }
        } catch (IOException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }
    
    public boolean isConectado() {
        return conectado;
    }
}