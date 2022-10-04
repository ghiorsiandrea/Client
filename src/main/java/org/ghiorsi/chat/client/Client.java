package org.ghiorsi.chat.client;

import org.ghiorsi.chat.protocol.ShippingPackage;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Set;

public class Client {
    public static final int PORT = Integer.parseInt(System.getenv("PORT"));
    public static final String ONLINE = " Online";

    public static void main(String[] args) {
        String userNick = JOptionPane.showInputDialog("Nick: ");
        MarcoCliente miMarco = new MarcoCliente(userNick);
        miMarco.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static class MarcoCliente extends JFrame {
        public MarcoCliente(String userNick) {
            setBounds(600, 300, 280, 350);
            addWindowListener(new OnlineSender(userNick));
            LaminaMarcoCliente milamina = new LaminaMarcoCliente(userNick);
            add(milamina);
            setVisible(true);
        }
    }

    /**
     * Online signal Sender
     */
    static class OnlineSender extends WindowAdapter {


        private String userNick;

        public OnlineSender(String userNick) {
            this.userNick = userNick;
        }

        @Override
        public void windowOpened(WindowEvent e) {

            try {
                Socket misocket = new Socket("127.0.0.1", PORT);
                ShippingPackage datos = new ShippingPackage();
                datos.setMensaje(ONLINE);
                datos.setNick(userNick);
                ObjectOutputStream paquete_datos = new ObjectOutputStream(misocket.getOutputStream());
                paquete_datos.writeObject(datos);
                paquete_datos.close();
            } catch (Exception ex) {

            }
        }
    }

    static class LaminaMarcoCliente extends JPanel implements Runnable {
        private JTextField campo1;
        private JComboBox nicks;
        private JLabel nick;
        private JTextArea campochat;
        private JButton sendButton;

        public LaminaMarcoCliente(String userNick) {
            nick = new JLabel();
            nick.setText(userNick);
            add(nick);
            JLabel texto = new JLabel(" - Online - ");
            add(texto);
            nicks = new JComboBox();

            add(nicks);
            campochat = new JTextArea(12, 20);
            add(campochat);
            campo1 = new JTextField(20);
            add(campo1);
            sendButton = new JButton("Send");
            EnviaTexto mievento = new EnviaTexto();
            sendButton.addActionListener(mievento);
            add(sendButton);

            // That the client is permanently listening (9090) and can send and receive information (server socket)
            Thread mihilo = new Thread(this);
            mihilo.start();
        }

        @Override
        public void run() {
            try {
                ServerSocket servidor_cliente = new ServerSocket(9090);
                Socket cliente;
                ShippingPackage paqueteRecibido;

                while (true) {
                    cliente = servidor_cliente.accept();
                    ObjectInputStream flujoentrada = new ObjectInputStream(cliente.getInputStream());
                    paqueteRecibido = (ShippingPackage) flujoentrada.readObject();

                    if (!paqueteRecibido.getMensaje().equals(ONLINE)) {
                        campochat.append("\n" + paqueteRecibido.getNick() + ": " + paqueteRecibido.getMensaje());
                    }

                    else {
                        ArrayList<String> nicksMenu = paqueteRecibido.getNicks();
                        nicks.removeAllItems();

                        for (String z : nicksMenu) {
                            nicks.addItem(z);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private class EnviaTexto implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(campo1.getText());
                campochat.append("\n" + campo1.getText());
                try {
                    Socket misocket = new Socket("192.168.1.14", Client.PORT);
                    ShippingPackage datos = new ShippingPackage();
                    datos.setNick(nick.getText());
                    datos.setMensaje(campo1.getText());

                    ObjectOutputStream paquete_datos = new ObjectOutputStream(misocket.getOutputStream());
                    paquete_datos.writeObject(datos);
                    paquete_datos.close();

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}