package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.controller.Controller;

import javax.swing.*;
import java.awt.*;

public class ConnectionDialog extends JDialog {

    public ConnectionDialog(JFrame parent, Controller controller) {
        super(parent, "Nueva Conexión", true);

        
        setSize(600, 150);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(70, 20));

        JLabel lblIP = new JLabel("Dirección IP:");
        JLabel lblUser = new JLabel("Usuario:");

        JTextField txtIP = new JTextField();
        JTextField txtIP2 = new JTextField();
        JTextField txtIP3 = new JTextField();
        JTextField txtIP4 = new JTextField();

//        JTextField txtUser = new JTextField();

        JButton btnConnect = new JButton("Conectar");

        JPanel centerPanel = new JPanel(new GridLayout(1, 4, 20, 60));
        JPanel upPanel = new JPanel(new GridLayout(1, 4, 20, 60));
        centerPanel.add(lblIP);
        centerPanel.add(txtIP);
//        centerPanel.add(txtIP2);
//        centerPanel.add(txtIP3);
//        centerPanel.add(txtIP4);
//        centerPanel.add(lblUser);
//        centerPanel.add(txtUser);
        add(upPanel,BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(btnConnect, BorderLayout.SOUTH);

        String ip;
        btnConnect.addActionListener(e -> {
            controller.connect(txtIP.getText());
            dispose();
        });
    }
}