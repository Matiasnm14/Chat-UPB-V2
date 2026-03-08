package edu.upb.chatupb_v2.view;

import javax.swing.*;
import java.awt.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.DocumentFilter.FilterBypass;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class LoginDialog extends JDialog {
    private static final int MAX_NAME_LENGTH = 60;
    private static final String USERNAME_FILE = System.getProperty("user.home")
            + java.io.File.separator + ".chatupb_username";

    private static final String FONT_UI = "Segoe UI";
    private static final Color BG_GRADIENT_TOP = new Color(0xF6FBFF);
    private static final Color BG_GRADIENT_BOTTOM = new Color(0xE6F4F1);
    private static final Color CARD_BG = new Color(0xFFFFFF);
    private static final Color ACCENT = new Color(0x1FA97A);
    private static final Color ACCENT_DARK = new Color(0x15825E);
    private static final Color TEXT_PRIMARY = new Color(0x1D2B36);
    private static final Color TEXT_MUTED = new Color(0x5B6B78);
    private static final Color FIELD_BG = new Color(0xF5F7FA);
    private static final Color FIELD_BORDER = new Color(0xD7E0E8);

    private final JTextField nameField = new JTextField(20);
    private boolean confirmed = false;

    private LoginDialog() {
        super((Frame) null, "Inicio de sesion", true);
        initUi();
        // loadLastUsername();
    }

    public static LoginResult showDialog() {
        if (SwingUtilities.isEventDispatchThread()) {
            return new LoginDialog().showAndGet();
        }
        final LoginResult[] result = new LoginResult[1];
        try {
            SwingUtilities.invokeAndWait(() -> result[0] = new LoginDialog().showAndGet());
        } catch (Exception e) {
            return null;
        }
        return result[0];
    }

    private LoginResult showAndGet() {
        setVisible(true);
        if (!confirmed) {
            return null;
        }
        return new LoginResult(nameField.getText().trim());
    }

    private void initUi() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        GradientPanel root = new GradientPanel(BG_GRADIENT_TOP, BG_GRADIENT_BOTTOM);
        root.setLayout(new GridBagLayout());

        CardPanel card = new CardPanel(CARD_BG);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgeRow.setOpaque(false);
        badgeRow.add(new Badge("UPB", ACCENT));

        JLabel title = new JLabel("Bienvenido");
        title.setFont(new Font(FONT_UI, Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Inicia sesion para entrar al chat");
        subtitle.setFont(new Font(FONT_UI, Font.PLAIN, 12));
        subtitle.setForeground(TEXT_MUTED);

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.add(badgeRow);
        header.add(Box.createVerticalStrut(12));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);

        JLabel nameLabel = new JLabel("Nombre de usuario");
        nameLabel.setFont(new Font(FONT_UI, Font.PLAIN, 12));
        nameLabel.setForeground(TEXT_MUTED);

        nameField.setFont(new Font(FONT_UI, Font.PLAIN, 14));
        nameField.setBackground(FIELD_BG);
        nameField.setForeground(TEXT_PRIMARY);
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        ((AbstractDocument) nameField.getDocument()).setDocumentFilter(new LengthFilter(MAX_NAME_LENGTH));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.add(nameLabel);
        form.add(Box.createVerticalStrut(6));
        form.add(nameField);

        JButton btnEnter = new JButton("Entrar");
        stylePrimaryButton(btnEnter);
        JButton btnExit = new JButton("Salir");
        styleGhostButton(btnExit);
        btnEnter.addActionListener(e -> onConfirm());
        btnExit.addActionListener(e -> onCancel());
        getRootPane().setDefaultButton(btnEnter);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(btnExit);
        actions.add(btnEnter);

        card.add(header);
        card.add(Box.createVerticalStrut(18));
        card.add(form);
        card.add(Box.createVerticalStrut(18));
        card.add(actions);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 20, 20, 20);
        root.add(card, gbc);

        setContentPane(root);
        setPreferredSize(new Dimension(520, 360));
        pack();
        setLocationRelativeTo(null);
    }

    private void onConfirm() {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        if (name.isBlank()) {
            JOptionPane.showMessageDialog(this, "Ingresa tu nombre.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (name.length() > MAX_NAME_LENGTH) {
            JOptionPane.showMessageDialog(
                    this,
                    "El nombre no puede tener mas de " + MAX_NAME_LENGTH + " caracteres.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        confirmed = true;
        // persistUsername(name);
        dispose();
    }

    private void onCancel() {
        confirmed = false;
        dispose();
    }

    public static String loadSavedUsername() {
        Path path = Paths.get(USERNAME_FILE);
        if (!Files.exists(path)) {
            return null;
        }
        try {
            String raw = Files.readString(path);
            if (raw != null && !raw.trim().isBlank()) {
                return raw.trim();
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    private void loadLastUsername() {
        String saved = loadSavedUsername();
        if (saved != null) {
            nameField.setText(saved);
        }
    }

    private void persistUsername(String username) {
        try {
            Files.writeString(Paths.get(USERNAME_FILE), username);
        } catch (IOException ignored) {
        }
    }

    private static final class LengthFilter extends DocumentFilter {
        private final int maxLength;

        private LengthFilter(int maxLength) {
            this.maxLength = maxLength;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, javax.swing.text.AttributeSet attr)
                throws BadLocationException {
            if (string == null) {
                return;
            }
            if (fb.getDocument().getLength() + string.length() <= maxLength) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs)
                throws BadLocationException {
            if (text == null) {
                return;
            }
            int newLength = fb.getDocument().getLength() - length + text.length();
            if (newLength <= maxLength) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }

    private void stylePrimaryButton(JButton button) {
        button.setFont(new Font(FONT_UI, Font.BOLD, 12));
        button.setBackground(ACCENT);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        button.addChangeListener(e -> {
            if (button.getModel().isPressed()) {
                button.setBackground(ACCENT_DARK);
            } else {
                button.setBackground(ACCENT);
            }
        });
    }

    private void styleGhostButton(JButton button) {
        button.setFont(new Font(FONT_UI, Font.PLAIN, 12));
        button.setBackground(new Color(0xFFFFFF));
        button.setForeground(TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(FIELD_BORDER));
    }

    private static final class GradientPanel extends JPanel {
        private final Color start;
        private final Color end;

        private GradientPanel(Color start, Color end) {
            this.start = start;
            this.end = end;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            GradientPaint paint = new GradientPaint(0, 0, start, 0, h, end);
            g2.setPaint(paint);
            g2.fillRect(0, 0, w, h);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static final class CardPanel extends JPanel {
        private final Color background;

        private CardPanel(Color background) {
            this.background = background;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = 22;
            int shadow = 10;
            int x = shadow;
            int y = shadow;
            int w = getWidth() - (shadow * 2);
            int h = getHeight() - (shadow * 2);
            g2.setColor(new Color(0, 0, 0, 18));
            g2.fillRoundRect(x, y + 2, w, h, arc, arc);
            g2.setColor(background);
            g2.fillRoundRect(x, y, w, h, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static final class Badge extends JComponent {
        private final String text;
        private final Color color;

        private Badge(String text, Color color) {
            this.text = text;
            this.color = color;
            setPreferredSize(new Dimension(48, 48));
            setMinimumSize(new Dimension(48, 48));
            setMaximumSize(new Dimension(48, 48));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(0, 0, getWidth(), getHeight());
            g2.setColor(Color.WHITE);
            g2.setFont(new Font(FONT_UI, Font.BOLD, 14));
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(text, x, y);
            g2.dispose();
        }
    }

    public static final class LoginResult {
        private final String username;

        private LoginResult(String username) {
            this.username = username;
        }

        public String getUsername() {
            return username;
        }
    }
}
