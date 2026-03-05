package edu.upb.chatupb_v2.VIews;

import edu.upb.chatupb_v2.Controller.AccountController;
import edu.upb.chatupb_v2.Controller.exceptions.DatabaseException;
import edu.upb.chatupb_v2.Model.entities.Account;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class ConnectionDialog extends JDialog {

    private Account selectedAccount = null;

    public static Account showAccountPicker(JFrame parent) {
        ConnectionDialog dlg = new ConnectionDialog(parent);
        dlg.setVisible(true);
        return dlg.selectedAccount;
    }

    private ConnectionDialog(JFrame parent) {
        super(parent, "Seleccionar cuenta", true);
        setSize(380, 240);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        AccountController accountController = new AccountController();

        List<Account> accounts;
        try {
            accounts = accountController.getAccounts();
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error de base de datos",
                    JOptionPane.ERROR_MESSAGE);
            accounts = List.of();
        }

        // ---- Header ----
        JLabel title = new JLabel("Bienvenido a ChatUPB", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setBorder(new EmptyBorder(16, 0, 4, 0));
        add(title, BorderLayout.NORTH);

        // ---- Center: CardLayout ----
        JPanel center = new JPanel(new CardLayout());
        center.setBorder(new EmptyBorder(0, 24, 0, 24));

        // --- Card 1: account picker ---
        JPanel pickCard = new JPanel(new BorderLayout(6, 8));
        JLabel pickLabel = new JLabel("Selecciona una cuenta:");
        pickLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        DefaultComboBoxModel<Account> comboModel = new DefaultComboBoxModel<>();
        for (Account a : accounts) comboModel.addElement(a);
        JComboBox<Account> combo = new JComboBox<>(comboModel);
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Account a) setText(a.getNombre());
                return this;
            }
        });

        JButton btnNew = new JButton("+ Crear nueva cuenta");
        btnNew.setForeground(new Color(0, 102, 204));
        btnNew.setBorderPainted(false);
        btnNew.setContentAreaFilled(false);
        btnNew.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnNew.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel pickTop = new JPanel(new BorderLayout(4, 6));
        pickTop.add(pickLabel, BorderLayout.NORTH);
        pickTop.add(combo, BorderLayout.CENTER);
        pickCard.add(pickTop, BorderLayout.CENTER);
        pickCard.add(btnNew, BorderLayout.SOUTH);

        // --- Card 2: new account form ---
        JPanel newCard = new JPanel(new BorderLayout(6, 8));
        JLabel newLabel = new JLabel("Nombre de la nueva cuenta:");
        newLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JTextField txtName = new JTextField();

        JButton btnBack = new JButton("← Volver");
        btnBack.setForeground(new Color(0, 102, 204));
        btnBack.setBorderPainted(false);
        btnBack.setContentAreaFilled(false);
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel newTop = new JPanel(new BorderLayout(4, 6));
        newTop.add(newLabel, BorderLayout.NORTH);
        newTop.add(txtName, BorderLayout.CENTER);
        newCard.add(newTop, BorderLayout.CENTER);
        newCard.add(btnBack, BorderLayout.SOUTH);

        center.add(pickCard, "PICK");
        center.add(newCard, "NEW");
        add(center, BorderLayout.CENTER);

        // ---- Bottom: confirm button ----
        JButton btnOk = new JButton("Entrar");
        btnOk.setPreferredSize(new Dimension(100, 34));
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBorder(new EmptyBorder(0, 24, 12, 24));
        bottomPanel.add(btnOk);
        add(bottomPanel, BorderLayout.SOUTH);

        // ---- Card switches ----
        CardLayout cl = (CardLayout) center.getLayout();
        btnNew.addActionListener(e -> cl.show(center, "NEW"));
        btnBack.addActionListener(e -> cl.show(center, "PICK"));

        btnOk.addActionListener(e -> {
            boolean isNewCard = newCard.isShowing();

            if (isNewCard) {
                String name = txtName.getText().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Ingresa un nombre para la cuenta.", "Campo requerido",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (name.length() >= 60) {
                    JOptionPane.showMessageDialog(this,
                            "No se puede registrar un nombre tan largo.", "Error de Entrada",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                try {
                    selectedAccount = accountController.createAccount(name);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            ex.getMessage(), "Cuenta duplicada",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } else {
                Account picked = (Account) combo.getSelectedItem();
                if (picked == null) {
                    JOptionPane.showMessageDialog(this,
                            "No hay cuentas. Crea una nueva.", "Sin cuentas",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                selectedAccount = picked;
            }
            dispose();
        });

        getRootPane().setDefaultButton(btnOk);
    }
}