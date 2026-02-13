package edu.upb.chatupb_v2;

import edu.upb.chatupb_v2.bl.server.ChatServer;
import edu.upb.chatupb_v2.bl.server.SocketClient;
import edu.upb.chatupb_v2.repository.comands.*;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class JUi extends javax.swing.JFrame {

    ChatServer server;
    SocketClient socketClient;
    Scanner scan = new Scanner(System.in);

    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(JUi.class.getName());

    // Instancia A: myPort=1900, targetPort=1901
    // Instancia B: myPort=1901, targetPort=1900
    private int myPort = 1900;
    private int targetPort = 1901;
    private DefaultListModel<ContactItem> contactsModel = new DefaultListModel<>();
    private JList<ContactItem> contactsList = new JList<>(contactsModel);

    private JTextPane chatPane = new JTextPane();
    private JScrollPane chatScroll;
    private final Map<String, ContactItem> contactsIndex = new LinkedHashMap<>();

    public JUi() {
        initComponents();
        buildModernUI();
        try {
            server = new ChatServer(myPort, connectionListener);
            appendChat("Sistema", "Servidor escuchando en puerto " + myPort);
        } catch (IOException e) {
            e.printStackTrace();
            appendChat("Sistema", "ERROR: No se pudo abrir el puerto " + myPort + " (" + e.getMessage() + ")");
        }
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        jLabelIP = new javax.swing.JLabel("Dirección IP");
        jLabelUser = new javax.swing.JLabel("Usuario");

        jIP = new javax.swing.JTextField();
        jTextUserName = new javax.swing.JTextField();
        jbConectar = new javax.swing.JButton("Conectar");

        jTextMensaje = new javax.swing.JTextField();
        jbEnviar = new javax.swing.JButton("Enviar");

        jOnline = new javax.swing.JLabel("Status: Offline");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jbEnviar.addActionListener(evt -> jbEnviarActionPerformed(evt));
        jbConectar.addActionListener(evt -> jbConectarActionPerformed(evt));
    }
    //UI Chat izq
    private void buildModernUI() {

        setTitle("Chat UPB");
        setSize(980, 560);
        setLocationRelativeTo(null);

        // ===== Panel Izquierdo (Contactos) =====
        JPanel left = new JPanel(new BorderLayout(10, 10));
        left.setBackground(new Color(24, 26, 32));
        left.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel titleContacts = new JLabel("Contactos");
        titleContacts.setForeground(Color.WHITE);
        titleContacts.setFont(new Font("Segoe UI", Font.BOLD, 16));

        contactsList.setCellRenderer(new ContactRenderer());
        contactsList.setBackground(new Color(24, 26, 32));
        contactsList.setSelectionBackground(new Color(44, 48, 60));
        contactsList.setSelectionForeground(Color.WHITE);
        contactsList.setFixedCellHeight(44);

        JScrollPane contactsScroll = new JScrollPane(contactsList);
        contactsScroll.setBorder(null);
        contactsScroll.getViewport().setBackground(new Color(24, 26, 32));

        left.add(titleContacts, BorderLayout.NORTH);
        left.add(contactsScroll, BorderLayout.CENTER);

        // Panel Derecho
        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(new Color(245, 246, 248));
        right.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        // campo de conexion
        JPanel header = new JPanel(new BorderLayout(10, 10));
        header.setOpaque(false);

        JPanel connectRow = new JPanel(new GridLayout(2, 3, 10, 6));
        connectRow.setOpaque(false);

        styleLabel(jLabelIP);
        styleLabel(jLabelUser);

        styleField(jIP);
        styleField(jTextUserName);

        stylePrimaryButton(jbConectar);

        connectRow.add(jLabelIP);
        connectRow.add(jLabelUser);
        connectRow.add(new JLabel(""));
        connectRow.add(jIP);
        connectRow.add(jTextUserName);
        connectRow.add(jbConectar);

        styleStatus(jOnline);

        header.add(connectRow, BorderLayout.CENTER);
        header.add(jOnline, BorderLayout.SOUTH);

        // Chat area
        chatPane.setEditable(false);
        chatPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatPane.setBackground(Color.WHITE);
        chatPane.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        chatScroll = new JScrollPane(chatPane);
        chatScroll.setBorder(new SoftRoundBorder());
        chatScroll.getViewport().setBackground(Color.WHITE);

        // Input bottom
        JPanel input = new JPanel(new BorderLayout(10, 0));
        input.setOpaque(false);

        styleField(jTextMensaje);
        jTextMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        stylePrimaryButton(jbEnviar);
        jbEnviar.setText("Enviar");
        jbEnviar.setPreferredSize(new Dimension(110, 38));

        input.add(jTextMensaje, BorderLayout.CENTER);
        input.add(jbEnviar, BorderLayout.EAST);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);
        center.add(chatScroll, BorderLayout.CENTER);
        center.add(input, BorderLayout.SOUTH);

        right.add(header, BorderLayout.NORTH);
        right.add(center, BorderLayout.CENTER);

        // split
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setDividerLocation(280);
        split.setDividerSize(2);
        split.setBorder(null);

        getContentPane().removeAll();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(split, BorderLayout.CENTER);

        // contactos demo
        addOrUpdateContact("127.0.0.1", "Localhost", true);
        revalidate();
        repaint();
    }

    // =========================================================
    // Estilos
    // =========================================================
    private void styleLabel(JLabel l) {
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(new Color(70, 75, 85));
    }

    private void styleStatus(JLabel l) {
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(60, 120, 90));
    }

    private void styleField(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBackground(Color.WHITE);
        f.setBorder(new SoftRoundBorder());
        f.setPreferredSize(new Dimension(200, 36));
    }

    private void stylePrimaryButton(JButton b) {
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(0, 120, 215));
        b.setFocusPainted(false);
        b.setBorder(new SoftButtonBorder());
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(120, 36));
    }
    // Contactos con punto verde/rojo
    private void addOrUpdateContact(String key, String displayName, boolean online) {
        ContactItem existing = contactsIndex.get(key);
        if (existing == null) {
            ContactItem item = new ContactItem(key, displayName, online);
            contactsIndex.put(key, item);
            contactsModel.addElement(item);
        } else {
            existing.displayName = displayName;
            existing.online = online;
            contactsList.repaint();
        }
    }

    private void setContactOnline(String key, boolean online) {
        ContactItem existing = contactsIndex.get(key);
        if (existing != null) {
            existing.online = online;
            contactsList.repaint();
        }
    }

    private void appendChat(String who, String msg) {
        SwingUtilities.invokeLater(() -> {
            String current = chatPane.getText();
            chatPane.setText(current + who + ": " + msg + "\n\n");
            JScrollBar v = chatScroll.getVerticalScrollBar();
            v.setValue(v.getMaximum());
        });
    }
    // Listener de SocketClient
    private SocketClient.SocketListener connectionListener = new SocketClient.SocketListener() {

        @Override
        public void onInvitationReceived(Invitation invitation) {
            SwingUtilities.invokeLater(() -> {
                int respuesta = JOptionPane.showConfirmDialog(JUi.this,
                        "Invitación recibida de: " + invitation.getUserName() +
                                " (ID: " + invitation.getIdUser() + "). ¿Aceptar?",
                        "Nueva Conexión Entrante",
                        JOptionPane.YES_NO_OPTION);

                String key = invitation.getIdUser();
                addOrUpdateContact(key, invitation.getUserName(), true);

                if (respuesta == JOptionPane.YES_OPTION) {
                    jOnline.setText("Status: Conectado con " + invitation.getUserName());
                    appendChat("Sistema", "Invitación aceptada: " + invitation.getUserName());
                } else {
                    jOnline.setText("Status: Rechazado");
                    setContactOnline(key, false);
                    appendChat("Sistema", "Invitación rechazada: " + invitation.getUserName());
                }
            });
        }

        @Override public void onAcceptReceived(Accept accept) { }
        @Override public void onDeclineReceived(Decline decline) { }
        @Override public void onHelloReceived(Hello hello) { }
        @Override public void onAcceptHelloReceived(AcceptHello acceptHello) { }
        @Override public void onDeclineHelloReceived(DeclineHello declineHello) { }

        @Override
        public void onChatReceived(Chat chat) {
            appendChat("Otro", String.valueOf(chat));
        }

        @Override public void onConfirmedReceived(ConfirmRecived confirmRecived) { }
        @Override public void onDeleteMessageReceived(DeleteMessage deleteMessage) { }
        @Override public void onBuzzingReceived(Buzzing buzzing) { }
        @Override public void onPinMessageReceived(PinMessage pinMessage) { }
        @Override public void onUniqueMessageReceived(UniqueMessage uniqueMessage) { }
        @Override public void onThemeReceived(Theme theme) { }
    };

    private void jbConectarActionPerformed(java.awt.event.ActionEvent evt) {
        String ip = jIP.getText().trim();
        String user = jTextUserName.getText().trim();
        String myUserId = String.valueOf(System.currentTimeMillis());

        if (ip.isEmpty() || user.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Completa IP y Usuario.");
            return;
        }

        addOrUpdateContact(ip, ip, false);

        new Thread(() -> {
            try {
                socketClient = new SocketClient(ip, targetPort);
                socketClient.setListener(connectionListener);
                socketClient.start();

                Invitation myInvite = new Invitation(myUserId, user);
                socketClient.send(myInvite.createFormat());

                SwingUtilities.invokeLater(() -> {
                    jOnline.setText("Status: Enviando invitación...");
                    setContactOnline(ip, true);
                    appendChat("Sistema", "Invitación enviada a " + ip + ":" + targetPort);
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage())
                );
                setContactOnline(ip, false);
            }
        }).start();
    }

    private void jbEnviarActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            String msg = jTextMensaje.getText().trim();
            if (msg.isEmpty()) return;

            if (server != null) {
                server.enviarMensaje(msg);
                appendChat("Yo", msg);
                jTextMensaje.setText("");
            } else {
                appendChat("Sistema", "No hay cliente conectado para enviar.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> new JUi().setVisible(true));
    }

    private javax.swing.JTextField jIP;
    private javax.swing.JLabel jOnline;
    private javax.swing.JLabel jLabelIP;
    private javax.swing.JLabel jLabelUser;
    private javax.swing.JTextField jTextMensaje;
    private javax.swing.JTextField jTextUserName;
    private javax.swing.JButton jbConectar;
    private javax.swing.JButton jbEnviar;


    private static class ContactItem {
        String key;
        String displayName;
        boolean online;

        ContactItem(String key, String displayName, boolean online) {
            this.key = key;
            this.displayName = displayName;
            this.online = online;
        }
    }

    private static class ContactRenderer extends JPanel implements ListCellRenderer<ContactItem> {
        private final JLabel name = new JLabel();
        private ContactItem current;

        ContactRenderer() {
            setLayout(new BorderLayout());
            setOpaque(true);
            name.setForeground(Color.WHITE);
            name.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            add(name, BorderLayout.CENTER);
            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ContactItem> list, ContactItem value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            current = value;
            name.setText(value.displayName + "   (" + value.key + ")");
            setBackground(isSelected ? new Color(44, 48, 60) : new Color(24, 26, 32));
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (current == null) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int dot = 10;
            int x = 10;
            int y = (getHeight() - dot) / 2;

            g2.setColor(current.online ? new Color(0, 200, 90) : new Color(220, 50, 60));
            g2.fillOval(x, y, dot, dot);

            g2.setColor(new Color(0, 0, 0, 80));
            g2.drawOval(x, y, dot, dot);

            g2.dispose();
        }
    }

    private static class SoftRoundBorder extends AbstractBorder {
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 30));
            g2.drawRoundRect(x, y, width - 1, height - 1, 14, 14);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(10, 12, 10, 12);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = 12; insets.right = 12; insets.top = 10; insets.bottom = 10;
            return insets;
        }
    }

    private static class SoftButtonBorder extends AbstractBorder {
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 40));
            g2.drawRoundRect(x, y, width - 1, height - 1, 14, 14);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(8, 14, 8, 14);
        }
    }
}
