package chatconmonitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.LinkedList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Simulación de una sala de chat, donde interactúan 2 personas.
 * La lista de mensajes enviados por todos está protegida por un monitor.
 * @author Equipo 4
 */
public class Ejecutable {
    public static void main(String[] args) {
        //Creamos nuestro monitor.
        Chat salaDeChat = new Chat();
        
        //Creamos nuestros usuarios (donde cada uno tiene su propia ventana).
        Ventana usuario1 = new Ventana("Ricky", salaDeChat);
        Ventana usuario2 = new Ventana("Marty", salaDeChat);
        usuario2.setLocation(usuario1.getWidth(), 0);
    }
}

/**
 * Ventana de chat que es usada por un usuario.
 * Está conectada a una sala de chat (el monitor que protege los mensajes enviados).
 * @author Equipo 4
 */
class Ventana extends JFrame {
    
    public Ventana(String nombreDeUsuario, Chat salaDeChat){
        //El contenedor de todos los elementos de la ventana.
        JPanel contenido = new JPanel(new BorderLayout());
        contenido.setSize(250, 220);
        
        //El área de texto donde se muestran los mensajes enviados y recibidos por el usuario.
        JTextArea cuadroDeMensajes = new JTextArea("");
        cuadroDeMensajes.setLineWrap(true);
        cuadroDeMensajes.setWrapStyleWord(true);
        cuadroDeMensajes.setEditable(false);
        cuadroDeMensajes.setRows(10);
        contenido.add(new JScrollPane(cuadroDeMensajes), BorderLayout.NORTH);
        
        //Un contenedor para la parte inferior de la pantalla, que contiene el campo de texto para los mensaje y un botón de envío.
        JPanel barritaInferior = new JPanel(new BorderLayout());
        
        //El cuadro de texto donde el usuario escribe sus mensajes.
        JTextField cuadroDeEscritura = new JTextField("");
        cuadroDeEscritura.setBackground(Color.LIGHT_GRAY);
        barritaInferior.add(cuadroDeEscritura);
        
        //Un botón de envío, que sólo está de adorno.
        JButton btnEnviar = new JButton("Enviar");
        barritaInferior.add(btnEnviar, BorderLayout.EAST);
        
        contenido.add(barritaInferior, BorderLayout.SOUTH);
        
        setTitle("<" + nombreDeUsuario + "> - Chat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(contenido.getWidth(), contenido.getHeight());
        add(contenido);
        setVisible(true);
        
        Thread chatear = new Thread(() -> {
            byte i;
            while(true){
                cuadroDeMensajes.setText(salaDeChat.leerMensajes());
                salaDeChat.enviarMensaje(nombreDeUsuario, cuadroDeEscritura);
            }
        });
        chatear.setDaemon(true);
        chatear.start();
    }
    
}

/**
 * Clase monitor que protege una lista de mensajes presentes en un chat.
 * @author Equipo 4
 */
class Chat {
    //El número máximo de mensajes que puede ser almacenado en la lista "mensajes".
    private static final byte NUM_MENSAJES_MAX = 10;
    //La cantidad de caracteres que contiene un mensaje aleatorio.
    private static final byte NUM_LETRAS_POR_MENSAJE = 6;
    //Los posibles caracteres que pueden componer un mensaje aleatorio.
    private static final String ALFABETO = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    
    private LinkedList<String> mensajes = new LinkedList<>();
    private boolean recursoOcupado = false;
    
    /**
     * Lee los últimos mensajes que se han enviado en este chat.
     * @return Cadena con los mensajes, separados por saltos de línea.
     */
    public synchronized String leerMensajes() {
        while (recursoOcupado) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }
        }
        
        recursoOcupado = true;
        
        String resultado = "";
        //Recolectamos los mensajes y los separamos con saltos de línea.
        for(String aux : mensajes)
            resultado += aux + "\n";
        //Eliminamos el último caracter (un salto de línea), si es necesario.
        if(!mensajes.isEmpty())
            resultado = resultado.substring(0, resultado.length() - 1);
        
        recursoOcupado = false;
        
        notify();
        return resultado;
    }
    
    /**
     * Agrega un nuevo mensaje a este chat.
     * @param remitente El nombre del usuario, autor del mensaje.
     * @param contenido El contenido del mensaje.
     */
    public synchronized void enviarMensaje(String remitente, String contenido) {
        while (recursoOcupado) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }
        }
        
        recursoOcupado = true;
        
        mensajes.add("[" + remitente + "]:\t" + contenido);
        //Si tenemos muchos mensajes guardado, eliminamos los más viejos.
        while(mensajes.size() > NUM_MENSAJES_MAX)
            mensajes.removeFirst();
        
        recursoOcupado = false;
        
        notify();
    }
    
    /**
     * Agrega un mensaje con un contenido aleatorio.
     * @param remitente El nombre del usuario, autor del mansaje.
     * @param cuadroDeTexto Un campo de texto, en el que se apreciará cómo se va escribiendo el mensaje, letra por letra.
     */
    public synchronized void enviarMensaje(String remitente, JTextField cuadroDeTexto) {
        while (recursoOcupado) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }
        }
        
        recursoOcupado = true;
        
        cuadroDeTexto.setBackground(Color.WHITE);
        //Se escribe una cadena aleatoria en el cuadro de texto.
        for(byte i = 0; i < NUM_LETRAS_POR_MENSAJE; i++){
            cuadroDeTexto.setText(cuadroDeTexto.getText() + Character.toString(ALFABETO.charAt( (int)(Math.random() * ALFABETO.length()) )));
            try {
                Thread.sleep(300);
            } catch (InterruptedException ex) {

            }
        }
        
        mensajes.add("[" + remitente + "]:\t" + cuadroDeTexto.getText());
        cuadroDeTexto.setText("");
        cuadroDeTexto.setBackground(Color.LIGHT_GRAY);
        //Si tenemos muchos mensajes guardado, eliminamos los más viejos.
        while(mensajes.size() > NUM_MENSAJES_MAX)
            mensajes.removeFirst();
        
        recursoOcupado = false;
        
        notify();
    }
}
