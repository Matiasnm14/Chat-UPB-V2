package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.controller.Controller;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.regex.Pattern;

public class SendContactDialog extends JDialog {

    public SendContactDialog(JFrame parent) {
        super(parent, "Enviar Contacto", true);

        setSize(600, 150);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 30));



        JLabel lblIP = new JLabel("Nombre de Contacto:");




        JTextField txtIP_1 = new JTextField();



        JButton btnConnect = new JButton("Enviar");

        JPanel ipPanel = new JPanel(new GridLayout(1, 5, 10, 10));
        JPanel upPanel = new JPanel(new GridLayout(1, 5, 10, 10));
        ipPanel.add(lblIP);
        ipPanel.add(txtIP_1);




        add(upPanel, BorderLayout.NORTH);
        add(ipPanel, BorderLayout.CENTER);

        add(btnConnect, BorderLayout.SOUTH);

        btnConnect.addActionListener(e -> {


        });
    }
}