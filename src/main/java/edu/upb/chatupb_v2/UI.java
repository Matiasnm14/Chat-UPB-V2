package edu.upb.chatupb_v2;

import edu.upb.chatupb_v2.bl.server.Controller;
import edu.upb.chatupb_v2.bl.server.SocketClient;
import edu.upb.chatupb_v2.repository.comands.*;
import lombok.Getter;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class UI extends javax.swing.JFrame {

    // --- Logic Variables ---
    private SocketClient socketClient;
    private final String username = "Santiago";
    private final UUID userId = UUID.randomUUID();
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(UI.class.getName());

    // --- UI Components ---
    private JTextField jIP = new JTextField("127.0.0.1");
    private JTextField jTextUserName = new JTextField("Santiago");
    private JTextField jTextMensaje = new JTextField();
    private JLabel jOnline = new JLabel("Status: Offline");
    private JButton jbConectar = new JButton("Conectar");
    private JButton jbEnviar = new JButton("Enviar");
    private JButton jBforBuzzing = new JButton("Zumbido");

    private DefaultListModel<ContactItem> contactsModel = new DefaultListModel<>();
    private JList<ContactItem> contactsList = new JList<>(contactsModel);
    private JTextPane chatPane = new JTextPane();
    private JScrollPane chatScroll;
    private final Map<String, ContactItem> contactsIndex = new LinkedHashMap<>();

    public UI() {
        buildModernUI();
        setupListeners();
    }

    private void setupListeners() {
        jbConectar.addActionListener(this::jbConectarActionPerformed);
        jbEnviar.addActionListener(this::jbEnviarActionPerformed);
        jBforBuzzing.addActionListener(this::jBforBuzzingActionPerformed);
    }

    private void buildModernUI() {
        setTitle("Chat UPB");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ===== LEFT PANEL (Contacts) =====
        JPanel left = new JPanel(new BorderLayout(10, 10));
        left.setBackground(new Color(24, 26, 32));
        left.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel titleContacts = new JLabel("Contactos");
        titleContacts.setForeground(Color.WHITE);
        titleContacts.setFont(new Font("Segoe UI", Font.BOLD, 16));

        contactsList.setCellRenderer(new ContactRenderer());
        contactsList.setBackground(new Color(24, 26, 32));
        contactsList.setFixedCellHeight(50);

        JScrollPane contactsScroll = new JScrollPane(contactsList);
        contactsScroll.setBorder(null);
        contactsScroll.getViewport().setBackground(new Color(24, 26, 32));
        left.add(titleContacts, BorderLayout.NORTH);
        left.add(contactsScroll, BorderLayout.CENTER);

        // ===== RIGHT PANEL (Main Chat) =====
        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(new Color(245, 246, 248));

        // Header
        JPanel header = new JPanel(new GridBagLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        styleField(jIP);
        styleField(jTextUserName);
        stylePrimaryButton(jbConectar);
        stylePrimaryButton(jBforBuzzing);
        jBforBuzzing.setBackground(new Color(220, 80, 80));

        gbc.gridx = 0; gbc.gridy = 0; header.add(new JLabel("IP:"), gbc);
        gbc.gridx = 1; header.add(jIP, gbc);
        gbc.gridx = 2; header.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 3; header.add(jTextUserName, gbc);
        gbc.gridx = 4; header.add(jbConectar, gbc);
        gbc.gridx = 5; header.add(jBforBuzzing, gbc);

        // Chat Area
        chatPane.setEditable(false);
        chatPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatScroll = new JScrollPane(chatPane);
        chatScroll.setBorder(new SoftRoundBorder());

        // Footer
        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        styleField(jTextMensaje);
        stylePrimaryButton(jbEnviar);
        footer.add(jTextMensaje, BorderLayout.CENTER);
        footer.add(jbEnviar, BorderLayout.EAST);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(chatScroll, BorderLayout.CENTER);
        centerPanel.add(jOnline, BorderLayout.NORTH);
        jOnline.setBorder(BorderFactory.createEmptyBorder(0, 15, 5, 0));
        jOnline.setFont(new Font("Segoe UI", Font.BOLD, 12));

        right.add(header, BorderLayout.NORTH);
        right.add(centerPanel, BorderLayout.CENTER);
        right.add(footer, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setDividerLocation(250);
        split.setBorder(null);
        add(split);
    }

    // --- Action Handlers ---

    private void jbConectarActionPerformed(java.awt.event.ActionEvent evt) {
        String ip = jIP.getText().trim();
        if (ip.isEmpty()) return;

        new Thread(() -> {
            try {
                socketClient = new SocketClient(ip);
                socketClient.setListener(username, userId.toString(), connectionListener);
                Controller.addClients(socketClient);
                socketClient.start();

                Invitation myInvite = new Invitation(userId.toString(), username);
                socketClient.send(myInvite.createFormat());

                SwingUtilities.invokeLater(() -> jOnline.setText("Status: Enviando invitación..."));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Connection Error: " + e.getMessage()));
            }
        }).start();
    }

    private void jbEnviarActionPerformed(java.awt.event.ActionEvent evt) {
        String msg = jTextMensaje.getText().trim();
        if (msg.isEmpty()) return;
        try {
            Chat chat = new Chat(userId.toString(), UUID.randomUUID().toString(), msg);
            for (SocketClient sc : Controller.getClients()) {
                sc.send(chat.createFormat());
            }
            appendChat("Yo", msg);
            jTextMensaje.setText("");
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }

    private void jBforBuzzingActionPerformed(java.awt.event.ActionEvent evt) {
        for (SocketClient sc : Controller.getClients()) {
            try {
                sc.send(new Buzzing(userId.toString()).createFormat());
            } catch (IOException e) { logger.severe(e.getMessage()); }
        }
    }

    // --- Network Listener (The "Brain") ---

    @Getter
    private final SocketClient.SocketListener connectionListener = new SocketClient.SocketListener() {
        @Override
        public void onInvitationReceived(Invitation invitation) {
            SwingUtilities.invokeLater(() -> {
                int res = JOptionPane.showConfirmDialog(UI.this,
                        "Invitación de: " + invitation.getUserName(), "Nueva Conexión", JOptionPane.YES_NO_OPTION);

                if (res == JOptionPane.YES_OPTION) {
                    addOrUpdateContact(invitation.getIdUser(), invitation.getUserName(), true);
                    Accept acp = new Accept(userId.toString(), username);
                    try {
                        for (SocketClient sc : Controller.getClients()) {
                            if (sc.getUID().equals(invitation.getIdUser())) sc.send(acp.createFormat());
                        }
                    } catch (IOException e) { logger.severe(e.getMessage()); }
                } else {
                    try { socketClient.send(new Decline().createFormat()); } catch (IOException e) { logger.severe(e.getMessage()); }
                }
            });
        }

        @Override public void onAcceptReceived(Accept accept) {
            addOrUpdateContact(accept.getIdUser(), accept.getUserName(), true);
            SwingUtilities.invokeLater(() -> {
                jOnline.setText("Status: Online con " + accept.getUserName());
                JOptionPane.showMessageDialog(UI.this, "¡Conexión Aceptada!");
            });
        }

        @Override public void onChatReceived(Chat chat) {
            ContactItem sender = contactsIndex.get(chat.getIdUser());
            String senderName = (sender != null) ? sender.displayName : "Desconocido";

            appendChat(senderName, chat.getMessage());
            ConfirmRecived cr = new ConfirmRecived(chat.getIdMessage());
            Controller.getClients().forEach(c -> { try { c.send(cr.createFormat()); } catch (Exception e) {} });
        }

        @Override public void onHelloReceived(Hello hello) {
            AcceptHello ah = new AcceptHello(userId.toString());
            Controller.getClients().stream()
                    .filter(c -> c.getUID().equals(hello.getIdUser()))
                    .forEach(c -> { try { c.send(ah.createFormat()); } catch (Exception e) {} });
        }

        @Override public void onBuzzingReceived(Buzzing buzzing) {
            JOptionPane.showMessageDialog(null, "¡ZUMBIDO!", "Alerta", JOptionPane.WARNING_MESSAGE);
        }

        @Override public void onDeclineReceived(Decline d) {
            SwingUtilities.invokeLater(() -> jOnline.setText("Status: Rechazado"));
        }

        @Override public void onConfirmedReceived(ConfirmRecived cr) {
            System.out.println("Mensaje entregado");
        }
        @Override public void onAcceptHelloReceived(AcceptHello ah) {
            System.out.println("HI");
        }
        @Override public void onDeclineHelloReceived(DeclineHello dh) {}
        @Override public void onDeleteMessageReceived(DeleteMessage dm) {}
        @Override public void onPinMessageReceived(PinMessage pm) {}
        @Override public void onUniqueMessageReceived(UniqueMessage um) {}
        @Override public void onThemeReceived(Theme t) {}
    };

    // --- Helper Methods ---

    private void appendChat(String who, String msg) {
        SwingUtilities.invokeLater(() -> {
            String current = chatPane.getText();
            chatPane.setText(current + who + ": " + msg + "\n\n");
            chatPane.setCaretPosition(chatPane.getDocument().getLength());
        });
    }

    private void addOrUpdateContact(String key, String displayName, boolean online) {
        SwingUtilities.invokeLater(() -> {
            ContactItem existing = contactsIndex.get(key);
            if (existing == null) {
                ContactItem item = new ContactItem(key, displayName, online);
                contactsIndex.put(key, item);
                contactsModel.addElement(item);
            } else {
                existing.online = online;
                contactsList.repaint();
            }
        });
    }

    public void init() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        java.awt.EventQueue.invokeLater(() -> this.setVisible(true));

        // Heartbeat thread
        new Thread(() -> {
            while (true) {
                try { Thread.sleep(5000); } catch (InterruptedException e) { break; }
                Hello h = new Hello(userId.toString());
                Controller.getClients().forEach(c -> { try { c.send(h.createFormat()); } catch (Exception e) {} });
            }
        }).start();
    }

    // --- Styling and Inner Classes ---

    private void styleField(JTextField f) {
        f.setPreferredSize(new Dimension(150, 32));
        f.setBorder(new SoftRoundBorder());
    }

    private void stylePrimaryButton(JButton b) {
        b.setBackground(new Color(0, 120, 215));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
    }

    private static class ContactItem {
        String key, displayName; boolean online;
        ContactItem(String k, String d, boolean o) { key=k; displayName=d; online=o; }
    }

    private static class ContactRenderer extends JPanel implements ListCellRenderer<ContactItem> {
        JLabel lbl = new JLabel(); boolean isOnline;
        ContactRenderer() { setLayout(new BorderLayout()); add(lbl); setOpaque(true); lbl.setForeground(Color.WHITE); lbl.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0)); }
        @Override public Component getListCellRendererComponent(JList<? extends ContactItem> l, ContactItem v, int i, boolean s, boolean f) {
            lbl.setText(v.displayName); isOnline = v.online;
            setBackground(s ? new Color(44, 48, 60) : new Color(24, 26, 32));
            return this;
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isOnline ? new Color(0, 200, 90) : new Color(220, 50, 60));
            g2.fillOval(8, getHeight()/2 - 5, 10, 10);
        }
    }

    private static class SoftRoundBorder extends AbstractBorder {
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            g.setColor(new Color(200, 200, 200)); g.drawRoundRect(x, y, w-1, h-1, 10, 10);
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(5, 10, 5, 10); }
    }
}