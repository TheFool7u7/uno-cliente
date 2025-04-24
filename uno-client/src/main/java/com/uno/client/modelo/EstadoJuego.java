package com.uno.client.modelo;

import com.uno.server.modelo.Carta;
import com.uno.server.modelo.Jugador;
import java.util.List;

// Esta clase mantiene el estado del juego en el cliente
public class EstadoJuego {
    private List<Jugador> jugadores;
    private Carta ultimaCarta;
    private int jugadorActual;
    private Carta.Color colorActual;
    private boolean sentidoHorario;
    private int miId; // ID del jugador local
    private String miNombre; // Nombre del jugador local
    
    private static EstadoJuego instancia;
    
    private EstadoJuego() {
        // Constructor privado para singleton
    }
    
    public static EstadoJuego getInstancia() {
        if (instancia == null) {
            instancia = new EstadoJuego();
        }
        return instancia;
    }
    
    // Actualizar el estado completo
    public void actualizarEstado(List<Jugador> jugadores, Carta ultimaCarta, 
                               int jugadorActual, Carta.Color colorActual, boolean sentidoHorario) {
        this.jugadores = jugadores;
        this.ultimaCarta = ultimaCarta;
        this.jugadorActual = jugadorActual;
        this.colorActual = colorActual;
        this.sentidoHorario = sentidoHorario;
    }
    
    // Getters y setters
    public List<Jugador> getJugadores() {
        return jugadores;
    }
    
    public Jugador getMiJugador() {
        if (jugadores != null && miId >= 0 && miId < jugadores.size()) {
            return jugadores.get(miId);
        }
        return null;
    }
    
    public Carta getUltimaCarta() {
        return ultimaCarta;
    }
    
    public int getJugadorActual() {
        return jugadorActual;
    }
    
    public Carta.Color getColorActual() {
        return colorActual;
    }
    
    public boolean isSentidoHorario() {
        return sentidoHorario;
    }
    
    public int getMiId() {
        return miId;
    }
    
    public void setMiId(int miId) {
        this.miId = miId;
    }
    
    public String getMiNombre() {
        return miNombre;
    }
    
    public void setMiNombre(String miNombre) {
        this.miNombre = miNombre;
    }
    
    public boolean esmiTurno() {
        return jugadorActual == miId;
    }
}