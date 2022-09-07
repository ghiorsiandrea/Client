package org.ghiorsi.chat.client;

import PaqueteEnvio.PaqueteEnvio;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Client {
    public static final int PORT = 9999;

    public static void main(String[] args) {
        MarcoCliente miMarco = new MarcoCliente();
        miMarco.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static class MarcoCliente extends JFrame {
        public MarcoCliente() {
            setBounds(600, 300, 280, 350);
            LaminaMarcoCliente milamina = new LaminaMarcoCliente();
            add(milamina);
            setVisible(true);
        }
    }

    static class LaminaMarcoCliente extends JPanel  implements Runnable{
        private JTextField campo1, nick, ip;

        private JTextArea campochat;
        private JButton miboton;

        public LaminaMarcoCliente() {

            nick = new JTextField(5);
            add(nick);
            JLabel texto = new JLabel(" - CHAT - ");
            add(texto);
            ip = new JTextField(8);
            add(ip);
            campochat = new JTextArea(12, 20);
            add(campochat);
            campo1 = new JTextField(20);
            add(campo1);
            miboton = new JButton("Send");
            EnviaTexto mievento = new EnviaTexto();
            miboton.addActionListener(mievento);
            add(miboton);

            // Que el cliente este a la escuha permanentemente (9090)y pueda enviar y recibir informacion (server socket)
            Thread mihilo = new Thread(this);
            mihilo.start();
        }

        @Override
        public void run() {
            try {
                ServerSocket servidor_cliente = new ServerSocket(9090);
                Socket cliente;
                PaqueteEnvio paqueteRecibido;
                while (true) {
                    cliente = servidor_cliente.accept();
                    ObjectInputStream flujoentrada = new ObjectInputStream(cliente.getInputStream());
                    paqueteRecibido = (PaqueteEnvio) flujoentrada.readObject();
                    campochat.append("\n" + paqueteRecibido.getNick() + ": " + paqueteRecibido.getMensaje());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private class EnviaTexto implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {

                System.out.println(campo1.getText());
                try {
                    Socket misocket = new Socket("127.0.0.1", Client.PORT);
                    PaqueteEnvio datos = new PaqueteEnvio();
                    datos.setNick(nick.getText());
                    datos.setIp(ip.getText());
                    datos.setMensaje(campo1.getText());

//                  Ex. Two
                    ObjectOutputStream paquete_datos = new ObjectOutputStream(misocket.getOutputStream());
                    paquete_datos.writeObject(datos);
                    paquete_datos.close();

//                   Ex. One
//                   DataOutputStream flujo_salida = new DataOutputStream(misocket.getOutputStream());
//                   flujo_salida.writeUTF(campo1.getText());
//                   flujo_salida.close();

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}

