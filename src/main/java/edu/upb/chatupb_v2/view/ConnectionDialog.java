package edu.upb.chatupb_v2;

import edu.upb.chatupb_v2.controller.ChatService;

import javax.swing.*;
import java.awt.*;

public class ConnectionDialog extends JDialog {

    public ConnectionDialog(JFrame parent, ChatService chatService) {
        super(parent, "Nueva Conexión", true);

        setSize(400, 200);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(50, 30));

        JLabel lblIP = new JLabel("Dirección IP:");
        JLabel lblUser = new JLabel("Usuario:");

        JTextField txtIP = new JTextField();
        JTextField txtUser = new JTextField();

        JButton btnConnect = new JButton("Conectar");

        JPanel centerPanel = new JPanel(new GridLayout(2, 2, 10, 20));
        centerPanel.add(lblIP);
        centerPanel.add(txtIP);
        centerPanel.add(lblUser);
        centerPanel.add(txtUser);

        add(centerPanel, BorderLayout.CENTER);
        add(btnConnect, BorderLayout.SOUTH);

        btnConnect.addActionListener(e -> {
            chatService.connect(txtIP.getText());
            dispose();
        });
    }
}