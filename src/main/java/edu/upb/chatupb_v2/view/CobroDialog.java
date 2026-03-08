package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.controller.CobroController;
import edu.upb.chatupb_v2.controller.Controller;
import edu.upb.chatupb_v2.model.repository.enums.CobroType;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.regex.Pattern;

public class CobroDialog extends JDialog {

    public CobroController cobroController;

    public CobroDialog(JFrame parent, CobroController cobroController) {

        super(parent, "Nueva Conexión", true);

        this.cobroController = cobroController;

        setSize(600, 150);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 30));


        JLabel lbImp = new JLabel("Importe: 30 Bs");
        JLabel lblPago = new JLabel("Tipo de pago:");


        JButton btnFiat = new JButton("Fiat");
        JButton btnCripto = new JButton("Cripto");

        JPanel ipPanel = new JPanel(new GridLayout(1, 5, 10, 10));
        JPanel upPanel = new JPanel(new GridLayout(1, 5, 10, 10));
        JPanel botPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        ipPanel.add(lbImp);


        add(upPanel, BorderLayout.NORTH);
        add(ipPanel, BorderLayout.CENTER);

        botPanel.add(btnFiat, BorderLayout.WEST);
        botPanel.add(btnCripto, BorderLayout.EAST);

        add(botPanel, BorderLayout.SOUTH);

        btnFiat.addActionListener(e -> {
            cobroController.cobrar(new BigDecimal(30), CobroType.BOB);
            dispose();

        });

        btnCripto.addActionListener(e -> {
            cobroController.cobrar(new BigDecimal(30), CobroType.CRIPTO);
            dispose();
        });
    }
}