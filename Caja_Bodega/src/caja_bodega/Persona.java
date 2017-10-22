/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package caja_bodega;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

/**
 * @author jf_rl y compañia
 */
public class Persona {
    public int posFinal = 385;
    public String nombre;
    private Button persona;
    final private Runnable ir_A_caja;
    int  posX = 0;
    int  posY = 0;
    /**
     *@param root Contenedor para la visualización de los objetos.
     *@param persona Es un botón que contiene la imagen de una persona.
     *@param nombre Es el nombre de la persona que será atendida en la caja.
     */
    Persona(Pane root, Button persona, String nombre) {
    
        this.nombre = nombre;
        posX = (int) persona.getLayoutX();
        posY = (int) persona.getLayoutY();
        this.persona = persona;
        
        //mostramos en pantalla a las personas.
        root.getChildren().addAll(this.persona);
        
        ir_A_caja = () -> {
                    this.persona.setLayoutX(this.persona.getLayoutX()+1);
        };        
    }


/**
 *El metodo hace que las personas se dirijan a la caja.
 * @param velocidad La velocidad la con la cual las personas se dirijen a la caja.
 */
    void Ir_A_Caja(short velocidad) {
        Thread hilo = new Thread(() -> {
            while(this.persona.getLayoutX() < posFinal){
                Platform.runLater(ir_A_caja);
                try{
                    Thread.sleep(velocidad);
                }catch(Exception e){}
            }
            
        });
        hilo.setDaemon(true);
        hilo.start();
    }
    /**
     *Reinicia la simulación, las personas regresan a su lugar de partida.
     */
    void Reiniciar() {
        this.persona.setLayoutX(0);
    }
    /**
     * Para saber si las personas han llegado a la caja
     *@return Un boleano
     */
    boolean Llego(){
         return this.persona.getLayoutX() >= posFinal;
    }
}
