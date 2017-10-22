package barrera;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.util.concurrent.Semaphore;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * Ejemplo que hace uso de un semáforo.
 * Simula la cola de un cajero automático.
 * 1.- Cada persona tarda una cantidad de tiempo al azar para llegar a la cola.
 * 2.- Cuando la persona llega a la cola y espera su turno (según su lugar de llegada).
 * 3.- Los turnos del cajero son controlados por un semáforo.
 * @author Equipo 4.
 */
public class Ejecutable {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("Cajero automático");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setSize(300, 300);
        frame.add(new Ventana());
        frame.setVisible(true);
    }
}

class Ventana extends JPanel {
    //La duración de cada fotograma, en milisegundos.
    static final byte FOTOGRAMA = 33;
    //Posicon del cajero auromático.
    static final short cajero_posX = 100, cajero_posY = 40;
    //Posicon de la persona que usa el cajero.
    static short personaAtendida_posX, personaAtendida_posY;
    
    //Generador de números aleatorios.
    Random random;
    //Imágenes.
    BufferedImage imgPersona, imgCajero;
    //Una etiqueta que muestra el orden de llegada.
    JLabel ordenDeLlegada;
    //Las personas que van a ser atendidas.
    Persona[] personas;
    //El semáforo que controla del acceso al cajero.
    Semaphore semaforo;
    
    public Ventana() {
        //Cargamos las imágenes.
        try {
            imgPersona = ImageIO.read(getClass().getResource("Monigote.png"));
            imgCajero = ImageIO.read(getClass().getResource("cajero.png"));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        
        JButton btnAcercaDe = new JButton("Acerca de...");
        btnAcercaDe.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "UP Chiapas\nProgramación concurrente - 7°\n\nEJEMPLO DE SEMÁFOROS\nCajero automático con llegada de clientes al azar\n\nJavier Alberto Argüello Tello - 153217\nJosé Julián Molina Ocaña - 153169\nFrancisco Javier de la Cruz Jiménez - 153181\nMauricio Armando Pérez Hernández - 153188\nJaime Francisco Ruiz López - 153189\nMónica Alejandra Peña Robles - 153209");
            }
        });
        
        ordenDeLlegada = new JLabel("");
        add(ordenDeLlegada);
        add(btnAcercaDe);
        personaAtendida_posX = (short)(cajero_posX + imgPersona.getWidth());
        personaAtendida_posY = (short)(cajero_posY + imgCajero.getHeight() - imgPersona.getHeight());
        
        semaforo = new Semaphore(1); //El cajero sólo atiende una persona a la vez.
        personas = new Persona[10];
        random = new Random();
        setSize(300, 300);
        
        //Creamos a todas las personas que se van a reunir.
        short posX = 0;
        for(byte i = 0; i < personas.length; i++){
            personas[i] = new Persona(posX, getHeight(), posX, (getHeight() - imgPersona.getHeight()), (char)(i + 65));
            posX += imgPersona.getWidth();
        }
        
        //Iniciamos los hilos de todas las personas.
        for(Persona aux : personas){
            aux.setDaemon(true);
            aux.start();
        }
        
        Thread renderizar = new Thread(() -> {
            boolean todosLosHiloaAcabaron = false;
            
            while(true){
                //Renderizamos un fotograma.
                repaint();
                
                //Checamos si todas las personas usaron el cajero y se fueron.
                todosLosHiloaAcabaron = true;
                for(Persona aux : personas)
                    if(aux.getState() != Thread.State.TERMINATED){
                        todosLosHiloaAcabaron = false;
                        break;
                    }
                //De ser así, cerramos el programa.
                if(todosLosHiloaAcabaron)
                    System.exit(0);
                
                try {
                    Thread.sleep(FOTOGRAMA);
                } catch (Exception ex) {
                    break;
                }
            }
        });
        renderizar.setDaemon(true);
        renderizar.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        //Obtenemos el objeto que nos permitirá pintar la pantalla.
        Graphics2D lienzo = (Graphics2D) g;
        lienzo.setPaint(Color.WHITE);
        
        //Mostramos el cajero.
        lienzo.drawImage(imgCajero, cajero_posX, cajero_posY, null);
        //Mostramos a cada persona en pantalla
        for(Persona aux : personas)
            aux.renderizar(lienzo);
    }
    
    /*
    * Clase que representa a una persona.
    * Alguien que aparece en una posición, se mueve hacia su posición de espera,
    * espera su turno según el semáforo, va a la posición del cajero por un par de segundos y
    * se sale de la pantalla.
    */
    class Persona extends Thread {
        //Posición actual.
        short posX, posY;

        //Posición donde esta persona espera a sus compañeros.
        short objX, objY;
        
        //El texto que la persona tiene en el pecho al ser renderizada.
        String nombre;
        
        /*
        * Constructor.
        * iX e iY son la posición inicial. eX y eY son las posiciones de espera.
        * "nombre" es una letra que muestra en el pecho.
        */
        Persona(int iX, int iY, int eX, int eY, char nombre){
            posX = (short)iX;
            posY = (short)iY;
            objX = (short)eX;
            objY = (short)eY;
            this.nombre = Character.toString(nombre);
        }

        @Override
        public void run(){
            byte movHorizontal, movVertical;
            
            try {
                //La persona tarda un tiempo (al azar) en llegar al banco.
                Thread.sleep(random.nextInt(20000));
                
                //La persona se aparece en en lugar, ahora se mueve a su posición de espera.
                movHorizontal = (byte)(posX < objX ? 1 : -1);
                movVertical = (byte)(posY < objY ? 1 : -1);
                while(posX != objX || posY != objY){
                    if(posX != objX)
                        posX += movHorizontal;
                    if(posY != objY)
                        posY += movVertical;

                    Thread.sleep(FOTOGRAMA);
                }
                synchronized(ordenDeLlegada){
                    ordenDeLlegada.setText(ordenDeLlegada.getText() + "\n" + nombre);
                }
                //La persona va y ocupa el cajero.
                semaforo.acquire();
                movHorizontal = (byte)(posX < personaAtendida_posX ? 1 : -1);
                movVertical = (byte)(posY < personaAtendida_posY ? 1 : -1);
                while(posX != personaAtendida_posX || posY != personaAtendida_posY){
                    if(posX != personaAtendida_posX)
                        posX += movHorizontal;
                    if(posY != personaAtendida_posY)
                        posY += movVertical;

                    Thread.sleep(FOTOGRAMA);
                }
                //La persona es atendida.
                Thread.sleep(2000);
                //La persona libera el cajero.
                semaforo.release();
                
                //La persona se sale de la pantalla.
                while(posX > -imgPersona.getWidth()){
                    posX--;
                    Thread.sleep(FOTOGRAMA);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }

        void renderizar(Graphics2D lienzo){
            lienzo.drawImage(imgPersona, posX, posY, null);
            lienzo.drawString(nombre, posX + (imgPersona.getWidth() / 2) - 4, posY + (imgPersona.getHeight() / 2));
        }
    }
}