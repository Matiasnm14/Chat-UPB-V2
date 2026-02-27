package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.controller.Controller;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.NumberFormat;
import java.text.ParseException;

public class ConnectionDialog extends JDialog {

    public ConnectionDialog(JFrame parent) {
        super(parent, "Nueva Conexión", true);

        setSize(600, 150);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 30));


        JLabel lblIP = new JLabel("Dirección IP:");
        JLabel lblUser = new JLabel("Usuario:");

        NumberFormat format = NumberFormat.getIntegerInstance();
        NumberFormatter formater = new NumberFormatter(format) {
            @Override
            public Object stringToValue(String text) throws ParseException{
                if (text == null || text.isEmpty()){
                    return null;
                }
                return super.stringToValue(text);
            }
        };
        formater.setValueClass(Integer.class);

        formater.setMaximum(999);
        formater.setAllowsInvalid(false);
        formater.setCommitsOnValidEdit(true);

        JTextField txtIP_1 = new JFormattedTextField(formater);
        JTextField txtIP_2 = new JFormattedTextField(formater);
        JTextField txtIP_3 = new JFormattedTextField(formater);
        JTextField txtIP_4 = new JFormattedTextField(formater);


        JButton btnConnect = new JButton("Conectar");

        JPanel ipPanel = new JPanel(new GridLayout(1, 5, 10, 10));
        JPanel upPanel = new JPanel(new GridLayout(1, 5, 10, 10));
        ipPanel.add(lblIP);
        ipPanel.add(txtIP_1);
        ipPanel.add(txtIP_2);
        ipPanel.add(txtIP_3);
        ipPanel.add(txtIP_4);



        add(upPanel, BorderLayout.NORTH);
        add(ipPanel, BorderLayout.CENTER);

        add(btnConnect, BorderLayout.SOUTH);

        btnConnect.addActionListener(e -> {
            String ip = txtIP_1 + "." + txtIP_2 + "." + txtIP_3 + "." + txtIP_4;
            if (ip.split(".").length == 4) {
                Controller.getInstance().connect(ip);
                dispose();
            }
        });
    }
}