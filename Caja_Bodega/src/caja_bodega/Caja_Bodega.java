/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package caja_bodega;

import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.RectangleBuilder;
import javafx.stage.Stage;

/**
 * @author jf_rl y compañia
 */
public class Caja_Bodega extends Application {
    
    private static final Semaphore disponible = new Semaphore(1);
    private ThreadLocalRandom random;
    int posFinal = 385;
    int dimensionImagenes = 50;
    
    @Override
    public void start(Stage primaryStage) {
    
        random = ThreadLocalRandom.current();
        Button iniciar = new Button();
        Button reiniciar = new Button();
        iniciar.setText("Iniciar");
        reiniciar.setText("Reiniciar");
        
        iniciar.setLayoutY(10);
        iniciar.setLayoutX(100);
        iniciar.setPrefSize(150,20);
        
        reiniciar.setLayoutY(10);
        reiniciar.setLayoutX(270);
        reiniciar.setPrefSize(150,20);
        
        ImageView imgPer1 = new ImageView(new Image(getClass().getResourceAsStream("imagenes/cliente1.png")));
        ImageView imgPer2 = new ImageView(new Image(getClass().getResourceAsStream("imagenes/cliente2.png")));
        ImageView imgPer3 = new ImageView(new Image(getClass().getResourceAsStream("imagenes/cliente3.png")));
        ImageView imgCajero = new ImageView(new Image(getClass().getResourceAsStream("imagenes/cajero.jpg")));
            
        imgPer1.setFitWidth(dimensionImagenes);
        imgPer1.setFitHeight(dimensionImagenes);
        imgPer2.setFitWidth(dimensionImagenes);
        imgPer2.setFitHeight(dimensionImagenes);
        imgPer3.setFitWidth(dimensionImagenes);
        imgPer3.setFitHeight(dimensionImagenes);
        imgCajero.setFitWidth(dimensionImagenes);
        imgCajero.setFitHeight(dimensionImagenes);
        
        
        Button p1 = new Button("J");
        Button p2 = new Button("A");
        Button p3 = new Button("F");
        Button ca = new Button("C");
        
        p1.setGraphic(imgPer1);
        p2.setGraphic(imgPer2);
        p3.setGraphic(imgPer3);
        ca.setGraphic(imgCajero);

        p1.setLayoutY(100);
        p1.setPrefSize(20, 20);
        
        p2.setLayoutY(100);
        p2.setPrefSize(20, 20);
        
        p3.setLayoutY(100);
        p3.setPrefSize(20, 20);   
        
        ca.setLayoutY(100);
        ca.setLayoutX(455);
        ca.setPrefSize(20, 20);
        
        TextArea salida = new TextArea();
        salida.setPrefSize(410, 150);
        salida.setLayoutX(55);
        salida.setLayoutY(220);

        Pane root = new Pane();
        root.getChildren().add(RectangleBuilder.create().x(0).y(0).width(520).height(400).fill(new ImagePattern(new Image(getClass().getResourceAsStream("imagenes/fondo.jpg")), 0, 0, 64, 56, false)).build());
        root.getChildren().addAll(salida,iniciar, reiniciar, ca);       
        Scene scene = new Scene(root, 520, 400);
        
        // creamos las personas que pasaran a realizar su pago en el cajero        
        Persona persona1 = new Persona (root, p1,"Julian");
        Persona persona2 = new Persona (root, p2, "Armando mawi");
        Persona persona3 = new Persona (root, p3, "Francisco");
                        
        primaryStage.setTitle("Simulacion - Caja - Bodega - MAMA LUCHA");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // comenzamos la ejecución
         iniciar.setOnAction((ActionEvent event) -> {
             // cada persona representa un hilo y caminan hacia la caja, se les asigna una velocidad random 
            persona1.Ir_A_Caja((short)random.nextInt(10, 20));
            persona2.Ir_A_Caja((short)random.nextInt(10, 20));
            persona3.Ir_A_Caja((short)random.nextInt(10, 20));
          
            // bloqueo de los botones
            iniciar.setDisable(true);
            reiniciar.setDisable(true);
            // hilo que verifica si una persona ha llegado a la caja
            Thread gestorDeFila = new Thread(() -> {
                // permite saber si todas las personas han sido atendidas
                boolean per1 = false;
                boolean per2 = false;
                boolean per3 = false;
               //mientras no se hayan atendido a todas las personas el hilo sigue su ejecución
                while(per1 == false || per2 == false || per3 == false){
                    try{
                        // si una persona llega a la caja, se le sede un tiempo se servicio
                        if (persona1.Llego() && per1 == false){
                                // la caja se vuelve disponible para la persona que acaba de llegar, y las demás personas quedan en espera (los hilos quedan en espera)
                                disponible.acquire();
                                salida.setText(salida.getText() + "\n[ " +  persona1.nombre + " ] sera atendido por 2 segundos");
                                Thread.sleep(2000);
                                salida.setText(salida.getText() + "\n[ " +  persona1.nombre + " ] ha sido atendido");
                                // se libera despues de realizar sus pagos y las demás personas avanzan para tomar su tiempo en el cajero
                                disponible.release();         
                                per1 = true;
                        }
                         if (persona2.Llego() && per2 == false){
                                disponible.acquire();
                                salida.setText(salida.getText() + "\n[ " +  persona2.nombre + " ] sera atendido po 2 segundos");
                                Thread.sleep(2000);
                                salida.setText(salida.getText() + "\n[ " +  persona2.nombre + " ] ha sido atendido");
                                disponible.release();         
                                per2 = true;
                        }
                          if (persona3.Llego() && per3 == false){
                                disponible.acquire();
                                salida.setText(salida.getText() + "\n[ " +  persona3.nombre + " ] sera atendido po 2 segundos");
                                Thread.sleep(2000);
                                salida.setText(salida.getText() + "\n[ " +  persona3.nombre + " ] ha sido atendido");
                                disponible.release();         
                                per3 = true;
                        }
                        Thread.sleep(0);
                        }catch (InterruptedException e){}
                }           
                // si todas las persona han sido atendidas entonces se desbloquea el boton para reiniciar la simulación
                if(per1 && per2 && per3)
                    reiniciar.setDisable(false);
            });
            gestorDeFila.setDaemon(true);
            gestorDeFila.start(); 
         });
         // reiniciar simulación
        reiniciar.setOnAction((ActionEvent event) -> {
            salida.setText("");
            iniciar.setDisable(false);
            persona1.Reiniciar();
            persona2.Reiniciar();
            persona3.Reiniciar();
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}

