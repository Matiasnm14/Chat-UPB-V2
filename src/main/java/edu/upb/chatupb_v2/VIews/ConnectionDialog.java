package edu.upb.chatupb_v2.VIews;

import edu.upb.chatupb_v2.Controller.AccountController;
import edu.upb.chatupb_v2.Controller.exceptions.DatabaseException;
import edu.upb.chatupb_v2.Model.entities.Account;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class ConnectionDialog extends JDialog {

    // ── Palette (matches JUi) ─────────────────────────────────
    private static final Color BG_DARK        = new Color(0xF0F2F5);
    private static final Color BG_SIDEBAR     = new Color(0xFFFFFF);
    private static final Color BG_INPUT       = new Color(0xFFFFFF);
    private static final Color ACCENT         = new Color(0x5B5BD6);
    private static final Color ACCENT_HOVER   = new Color(0x4848C0);
    private static final Color TEXT_PRIMARY   = new Color(0x111118);
    private static final Color TEXT_SECONDARY = new Color(0x666680);
    private static final Color BORDER_COLOR   = new Color(0xDDDDE8);

    // ── Fonts (matches JUi) ───────────────────────────────────
    private static final Font FONT_TITLE = new Font("SF Pro Display", Font.BOLD,  15);
    private static final Font FONT_BODY  = new Font("SF Pro Text",    Font.PLAIN, 13);
    private static final Font FONT_SMALL = new Font("SF Pro Text",    Font.PLAIN, 11);

    private Account selectedAccount = null;

    // ─────────────────────────────────────────────────────────
    public static Account showAccountPicker(JFrame parent) {
        ConnectionDialog dlg = new ConnectionDialog(parent);
        dlg.setVisible(true);
        return dlg.selectedAccount;
    }

    // ─────────────────────────────────────────────────────────
    private ConnectionDialog(JFrame parent) {
        super(parent, "ChatUPB — Iniciar sesión", true);
        setSize(400, 310);
        setMinimumSize(new Dimension(400, 310));
        setLocationRelativeTo(parent);
        setResizable(false);
        setUndecorated(false);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());

        AccountController accountController = new AccountController();

        List<Account> accounts;
        try {
            accounts = accountController.getAccounts();
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Error de base de datos", JOptionPane.ERROR_MESSAGE);
            accounts = List.of();
        }

        // ── Header ───────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_SIDEBAR);
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(20, 24, 16, 24)
        ));

        JLabel appName = new JLabel("ChatUPB");
        appName.setFont(new Font("SF Pro Display", Font.BOLD, 20));
        appName.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Selecciona o crea tu cuenta");
        subtitle.setFont(FONT_SMALL);
        subtitle.setForeground(TEXT_SECONDARY);

        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setOpaque(false);
        titleStack.add(appName);
        titleStack.add(Box.createVerticalStrut(2));
        titleStack.add(subtitle);

        // Logo circle
        JLabel logo = new JLabel("C") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getWidth(), getHeight());
                g2.setFont(new Font("SF Pro Display", Font.BOLD, 18));
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("C",
                        (getWidth()  - fm.stringWidth("C")) / 2,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        logo.setPreferredSize(new Dimension(40, 40));
        logo.setOpaque(false);

        header.add(logo,       BorderLayout.WEST);
        header.add(Box.createHorizontalStrut(12), BorderLayout.CENTER);
        header.add(titleStack, BorderLayout.EAST);

        // Re-do header with gap
        header.removeAll();
        header.setLayout(new BorderLayout(14, 0));
        header.add(logo,       BorderLayout.WEST);
        header.add(titleStack, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);

        // ── Card container ────────────────────────────────────
        JPanel cardContainer = new JPanel(new CardLayout());
        cardContainer.setBackground(BG_DARK);
        cardContainer.setBorder(new EmptyBorder(20, 24, 0, 24));

        // ── Card 1: account picker ────────────────────────────
        JPanel pickCard = new JPanel(new BorderLayout(0, 10));
        pickCard.setOpaque(false);

        JLabel pickLabel = new JLabel("Cuenta");
        pickLabel.setFont(FONT_BODY);
        pickLabel.setForeground(TEXT_SECONDARY);

        JComboBox<Account> combo = new JComboBox<>();
        DefaultComboBoxModel<Account> comboModel = new DefaultComboBoxModel<>();
        for (Account a : accounts) comboModel.addElement(a);
        combo.setModel(comboModel);
        combo.setFont(FONT_BODY);
        combo.setBackground(BG_INPUT);
        combo.setForeground(TEXT_PRIMARY);
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));
        combo.setPreferredSize(new Dimension(0, 36));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setFont(FONT_BODY);
                if (value instanceof Account a) setText(a.getNombre());
                if (isSelected) {
                    setBackground(new Color(0xEEEEFF));
                    setForeground(ACCENT);
                } else {
                    setBackground(BG_INPUT);
                    setForeground(TEXT_PRIMARY);
                }
                return this;
            }
        });

        JButton btnNew = makeLinkButton("+ Crear nueva cuenta");

        JPanel pickTop = new JPanel(new BorderLayout(0, 6));
        pickTop.setOpaque(false);
        pickTop.add(pickLabel, BorderLayout.NORTH);
        pickTop.add(combo,     BorderLayout.CENTER);
        pickCard.add(pickTop,  BorderLayout.NORTH);
        pickCard.add(btnNew,   BorderLayout.SOUTH);

        // ── Card 2: new account form ──────────────────────────
        JPanel newCard = new JPanel(new BorderLayout(0, 10));
        newCard.setOpaque(false);

        JLabel newLabel = new JLabel("Nombre de la nueva cuenta");
        newLabel.setFont(FONT_BODY);
        newLabel.setForeground(TEXT_SECONDARY);

        JTextField txtName = makeStyledTextField();

        JButton btnBack = makeLinkButton("← Volver");

        JPanel newTop = new JPanel(new BorderLayout(0, 6));
        newTop.setOpaque(false);
        newTop.add(newLabel,  BorderLayout.NORTH);
        newTop.add(txtName,   BorderLayout.CENTER);
        newCard.add(newTop,   BorderLayout.NORTH);
        newCard.add(btnBack,  BorderLayout.SOUTH);

        cardContainer.add(pickCard, "PICK");
        cardContainer.add(newCard,  "NEW");

        add(cardContainer, BorderLayout.CENTER);

        // ── Footer: confirm button ────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setBackground(BG_DARK);
        footer.setBorder(new EmptyBorder(14, 24, 20, 24));

        JButton btnOk = makeAccentButton("Entrar →");
        footer.add(btnOk);
        add(footer, BorderLayout.SOUTH);

        // ── Card switching ────────────────────────────────────
        CardLayout cl = (CardLayout) cardContainer.getLayout();
        btnNew.addActionListener(e -> { cl.show(cardContainer, "NEW"); txtName.requestFocus(); });
        btnBack.addActionListener(e -> cl.show(cardContainer, "PICK"));

        // ── Confirm action ────────────────────────────────────
        btnOk.addActionListener(e -> {
            boolean isNewCard = newCard.isShowing();

            if (isNewCard) {
                String name = txtName.getText().trim();
                if (name.isEmpty()) {
                    showWarning("Ingresa un nombre para la cuenta.", "Campo requerido");
                    return;
                }
                if (name.length() >= 60) {
                    showWarning("No se puede registrar un nombre tan largo.", "Error de entrada");
                    return;
                }
                try {
                    selectedAccount = accountController.createAccount(name);
                } catch (Exception ex) {
                    showWarning(ex.getMessage(), "Cuenta duplicada");
                    return;
                }
            } else {
                Account picked = (Account) combo.getSelectedItem();
                if (picked == null) {
                    showWarning("No hay cuentas disponibles. Crea una nueva.", "Sin cuentas");
                    return;
                }
                selectedAccount = picked;
            }
            dispose();
        });

        getRootPane().setDefaultButton(btnOk);
    }

    // ── Helpers ───────────────────────────────────────────────

    private JTextField makeStyledTextField() {
        JTextField f = new JTextField();
        f.setFont(FONT_BODY);
        f.setForeground(TEXT_PRIMARY);
        f.setBackground(BG_INPUT);
        f.setCaretColor(ACCENT);
        f.setPreferredSize(new Dimension(0, 36));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        // Highlight border on focus
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT, 1, true),
                        new EmptyBorder(4, 10, 4, 10)));
            }
            @Override public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                        new EmptyBorder(4, 10, 4, 10)));
            }
        });
        return f;
    }

    private JButton makeAccentButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()   ? ACCENT_HOVER :
                        getModel().isRollover()  ? ACCENT_HOVER : ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setFont(getFont());
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth()  - fm.stringWidth(getText())) / 2,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        btn.setFont(FONT_BODY);
        btn.setPreferredSize(new Dimension(120, 36));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton makeLinkButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_SMALL);
        btn.setForeground(ACCENT);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setForeground(ACCENT_HOVER); }
            @Override public void mouseExited (MouseEvent e) { btn.setForeground(ACCENT); }
        });
        return btn;
    }

    private void showWarning(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
    }

    // ── Compound border helper ────────────────────────────────
    private static class CompoundBorder extends javax.swing.border.AbstractBorder {
        private final javax.swing.border.Border outer, inner;
        CompoundBorder(javax.swing.border.Border outer, javax.swing.border.Border inner) {
            this.outer = outer; this.inner = inner;
        }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            outer.paintBorder(c, g, x, y, w, h);
            Insets oi = outer.getBorderInsets(c);
            inner.paintBorder(c, g, x + oi.left, y + oi.top,
                    w - oi.left - oi.right, h - oi.top - oi.bottom);
        }
        @Override public Insets getBorderInsets(Component c) {
            Insets oi = outer.getBorderInsets(c);
            Insets ii = inner.getBorderInsets(c);
            return new Insets(oi.top + ii.top, oi.left + ii.left,
                    oi.bottom + ii.bottom, oi.right + ii.right);
        }
    }
}