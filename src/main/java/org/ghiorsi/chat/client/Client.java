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

public class Client {
    public static final int PORT = Integer.parseInt(System.getenv("PORT"));
    public static final String ONLINE = " Online";

    public static void main(String[] args) {
        MarcoCliente miMarco = new MarcoCliente();
        miMarco.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static class MarcoCliente extends JFrame {
        public MarcoCliente() {
            setBounds(600, 300, 280, 350);
            addWindowListener(new EnvioOnline());
            LaminaMarcoCliente milamina = new LaminaMarcoCliente();
            add(milamina);
            setVisible(true);
        }
    }

    /**
     * Sending online signal
     */
    static class EnvioOnline extends WindowAdapter {

        @Override
        public void windowOpened(WindowEvent e) {

            try {
                Socket misocket = new Socket("127.0.0.1", PORT);
                ShippingPackage datos = new ShippingPackage();
                datos.setMensaje(ONLINE);
                ObjectOutputStream paquete_datos = new ObjectOutputStream(misocket.getOutputStream());
                paquete_datos.writeObject(datos);
                paquete_datos.close();
            } catch (Exception ex) {

            }
        }
    }

    static class LaminaMarcoCliente extends JPanel implements Runnable {
        private JTextField campo1;
        private JComboBox ip;
        private JLabel nick;
        private JTextArea campochat;
        private JButton miboton;

        public LaminaMarcoCliente() {
            String nick_usuario = JOptionPane.showInputDialog("Nick: ");
            JLabel n_nick = new JLabel("Nick: ");
            nick = new JLabel();
            nick.setText(nick_usuario);
            add(nick);
            JLabel texto = new JLabel(" - Online - ");
            add(texto);
            ip = new JComboBox();

//            ip.addItem("127.0.0.1");
//            ip.addItem("192.168.1.14");
//            ip.addItem("192.168.1.10");
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
                ShippingPackage paqueteRecibido;
                while (true) {
                    cliente = servidor_cliente.accept();
                    ObjectInputStream flujoentrada = new ObjectInputStream(cliente.getInputStream());
                    paqueteRecibido = (ShippingPackage) flujoentrada.readObject();

                    if (!paqueteRecibido.getMensaje().equals(ONLINE)) {
                        campochat.append("\n" + paqueteRecibido.getNick() + ": " + paqueteRecibido.getMensaje());
                    } else {
                        //campochat.append("\n" + paqueteRecibido.getIps());
                        ArrayList<String> IpsMenu = new ArrayList<String>();
                        IpsMenu = paqueteRecibido.getIps();
                        ip.removeAllItems();
                        for (String z : IpsMenu) {
                            ip.addItem(z);
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
                    datos.setIp(ip.getSelectedItem().toString());
                    datos.setMensaje(campo1.getText());

//                  Ex. Tree
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

