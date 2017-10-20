package barrera;

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
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JButton;
import javax.swing.JOptionPane;

/**
 * Ejemplo que hace uso de un semáforo.
 * Simula una reunión de 10 personas.
 * 1.- Cada uno tarda una cantidad de tiempo al azar para llegar al punto de reunión.
 * 2.- Cuando la persona llega al punto de reunión se queda esperado al resto.
 * 3.- Cuando todas las pesonas están juntas, se van.
 * 4.- Cada persona es manejada por un hilo y todos los hilos se coordinan con un semáforo.
 * @author Javier Alberto Argüello Tello
 */
public class Ejecutable {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("Reunión de personas");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setSize(400, 300);
        frame.add(new Ventana());
        frame.setVisible(true);
    }
}

class Ventana extends JPanel {
    //La duración de cada fotograma, en milisegundos.
    static final byte FOTOGRAMA = 33;
    
    //Generador de números aleatorios.
    Random random;
    BufferedImage imgPersona;
    //Las personas que se van a reunir.
    Persona[] personas;
    Semaphore semaforo;
    //La cantidad de personas que ya llegaron al punto de reunión o espera.
    AtomicInteger personasQueYaLlegaron;
    
    public Ventana() {
        try {
            imgPersona = ImageIO.read(getClass().getResource("Monigote.png"));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        
        JButton btnAcercaDe = new JButton("Acerca de...");
        btnAcercaDe.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "UP Chiapas\nProgramación concurrente - 7°\n\nEJEMPLO DE SEMÁFOROS\nReunión de personas\n\nJavier Alberto Argüello Tello - 153217\nJosé Julián Molina Ocaña - 153169\nFrancisco Javier de la Cruz Jiménez - 153181\nMauricio Armando Pérez Hernández - 153188\nJaime Francisco Ruiz López - 153189\nMónica Alejandra Peña Robles - 153209");
            }
        });
        
        add(btnAcercaDe);
        semaforo = new Semaphore(0);
        personas = new Persona[10];
        random = new Random();
        personasQueYaLlegaron = new AtomicInteger();
        setSize(400, 300);
        
        //Creamos a todas las personas que se van a reunir.
        personas[0] = new Persona(-imgPersona.getWidth(), random.nextInt(getHeight()), 105, 60);
        personas[1] = new Persona(-imgPersona.getWidth(), random.nextInt(getHeight()), 145, 60);
        personas[2] = new Persona(getWidth(), random.nextInt(getHeight()), 182, 60);
        personas[3] = new Persona(getWidth(), random.nextInt(getHeight()), 220, 60);
        personas[4] = new Persona(random.nextInt(getWidth()), -imgPersona.getHeight(), 260, 60);
        personas[5] = new Persona(random.nextInt(getWidth()), -imgPersona.getHeight(), 105, 135);
        personas[6] = new Persona(random.nextInt(getWidth()), -imgPersona.getHeight(), 145, 135);
        personas[7] = new Persona(random.nextInt(getWidth()), getHeight(), 182, 135);
        personas[8] = new Persona(random.nextInt(getWidth()), getHeight(), 220, 135);
        personas[9] = new Persona(random.nextInt(getWidth()), getHeight(), 260, 135);
        
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
                
                //Checamos si todas las personas ya se reunieron y se fueron
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
        
        //Mostramos a cada persona en pantalla
        for(Persona aux : personas)
            aux.renderizar(lienzo);
    }
    
    /*
    * Clase que representa a una persona.
    * Alguien que aparece en una posición, se mueve hacia otra, y se detiene a esperar hasta que
    * el resto de personas del arreglo "personas" en "Ventana" lleguen a sus respectivas posiciónes
    * de espera. Finalmente, se va por el borde inferior de la ventana.
    */
    class Persona extends Thread {
        //Posición actual.
        short posX, posY;

        //Posición donde esta persona espera a sus compañeros.
        final short objX, objY;
        
        /*
        * Constructor.
        * iX e iY son la posición inicial. eX y eY son las posiciones de espera. 
        */
        Persona(int iX, int iY, int eX, int eY){
            posX = (short)iX;
            posY = (short)iY;
            objX = (short)eX;
            objY = (short)eY;
        }

        @Override
        public void run(){
            byte movHorizontal, movVertical;
            movHorizontal = (byte)(posX < objX ? 1 : -1);
            movVertical = (byte)(posY < objY ? 1 : -1);
            
            try {
                //La persona tarda un tiempo (al azar) en llegar al lugar del encuentro.
                Thread.sleep(random.nextInt(20) * 1000);
                
                //La persona se aparece en en lugar, ahora se mueve a su posición de espera.
                while(posX != objX || posY != objY){
                    if(posX != objX)
                        posX += movHorizontal;
                    if(posY != objY)
                        posY += movVertical;

                    Thread.sleep(FOTOGRAMA);
                }
                
                //Aumentamos nuestro contador de personas que ya llegaron al punto de espera.
                personasQueYaLlegaron.incrementAndGet();
                
                //Si ya llegaron todas las personas, el semáforo les da permiso de ejecutar sus últimos pasos (salirse de la pantalla).
                if(personasQueYaLlegaron.get() == personas.length){
                    semaforo.release(personas.length);
                }
                
                //Si todavía no llegan todas las personas, el semáforo detiene a la persona actual.
                semaforo.acquire();
                
                //La persona se sale de la pantalla cuando ya llegaron todos.
                while(posY < getWidth()){
                    posY++;
                    Thread.sleep(FOTOGRAMA);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }

        void renderizar(Graphics2D lienzo){
            lienzo.drawImage(imgPersona, posX, posY, null);
        }
    }
}