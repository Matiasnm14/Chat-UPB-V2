/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package edu.upb.chatupb_v2;

import edu.upb.chatupb_v2.bl.server.ChatService;
import edu.upb.chatupb_v2.bl.server.IChatView;
import edu.upb.chatupb_v2.bl.server.Mediator;
import edu.upb.chatupb_v2.bl.server.SocketClient;
import edu.upb.chatupb_v2.repository.Contact;
import edu.upb.chatupb_v2.repository.ContactDao;
import edu.upb.chatupb_v2.repository.Message;
import edu.upb.chatupb_v2.repository.MessageDAO;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 *
 * @author USER 1
 */
@Getter
public class JUi extends IChatView {
    //YA NO HAY CHAT SERVER!
//    ChatServer server;
    private ChatService chatService;
//    SocketClient socketClient;
    private final String username = "Ciro";
    private final UUID userId = UUID.randomUUID();
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(JUi.class.getName());
    private static final Color BG_APP = new Color(0xF0F2F5);
    private static final Color BG_PANEL = new Color(0xFFFFFF);
    private static final Color CHAT_BG = new Color(0xE9EDF1);
    private static final Color ACCENT = new Color(0x25D366);
    private static final Color TEXT_PRIMARY = new Color(0x111B21);
    private static final Color TEXT_MUTED = new Color(0x667781);
    private static final Color BORDER = new Color(0xE1E7EC);
    private static final Color BUBBLE_OUT = new Color(0xDCF8C6);
    private static final Color BUBBLE_IN = new Color(0xFFFFFF);
    private static final Color TIME_TEXT = new Color(0x667781);
    private static final int MESSAGE_MAX_WIDTH = 360;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public JUi() {
        initComponents();
        this.chatService = new ChatService(this, username, userId.toString());
        Mediator.getInstance().addUi(this);
        loadContacts();
    }

    private void initComponents() {
        setTitle("Chat UPB");
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jIP = new javax.swing.JTextField();
        jTextUserName = new JTextField();
        jTextMensaje = new javax.swing.JTextField();

        jbConectar = new JButton("Conectar");
        jbEnviar = new JButton("Enviar");
        jBforBuzzing = new JButton("Buzz");

        statusDot = new JLabel();
        statusIcon = new DotIcon(new Color(0xB0B6BB), 10);
        statusDot.setIcon(statusIcon);
        jOnline = new javax.swing.JLabel("Offline");
        updateStatusIndicator(jOnline.getText());
//jlist
        contactListModel = new DefaultListModel<>();
        contactList = new JList<>(contactListModel);
        contactList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactList.setCellRenderer(new ContactRenderer());
        contactList.setFixedCellHeight(44);
        contactList.setBackground(BG_PANEL);
        contactList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                ContactListItem selected = contactList.getSelectedValue();
                if (selected != null) {
                    selectedContactCode = selected.getCode();
                    loadMessagesForContact(selected);
                }
            }
        });

        JScrollPane contactScrollPane = new JScrollPane(contactList);
        contactScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        contactScrollPane.setBorder(BorderFactory.createEmptyBorder());
        contactScrollPane.getViewport().setBackground(BG_PANEL);

        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(CHAT_BG);

        messagesScrollPane = new JScrollPane(messagesPanel);
        messagesScrollPane.setBorder(BorderFactory.createEmptyBorder());
        messagesScrollPane.getViewport().setBackground(CHAT_BG);
        messagesScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        styleButtonPrimary(jbConectar);
        styleButtonPrimary(jbEnviar);
        styleButtonGhost(jBforBuzzing);

        jbConectar.addActionListener(evt -> chatService.connect(jIP.getText()));
        jbEnviar.addActionListener(evt -> {
            String text = jTextMensaje.getText() == null ? "" : jTextMensaje.getText().trim();
            if (text.isEmpty()) {
                return;
            }
            String senderName = jTextUserName.getText();
            if (senderName == null || senderName.isBlank()) {
                senderName = username;
            }
            addChatMessage(text, true, senderName);
            chatService.sendMessage(text);
            jTextMensaje.setText("");
        });
        jBforBuzzing.addActionListener(evt -> chatService.sendBuzz());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_APP);
        setContentPane(root);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(BG_PANEL);
        leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER));
        leftPanel.setPreferredSize(new Dimension(280, 0));

        JPanel leftHeader = new JPanel();
        leftHeader.setBackground(BG_PANEL);
        leftHeader.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));
        leftHeader.setLayout(new BoxLayout(leftHeader, BoxLayout.Y_AXIS));

        JLabel contactsTitle = new JLabel("Contactos");
        contactsTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        contactsTitle.setForeground(TEXT_PRIMARY);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        statusPanel.setOpaque(false);
        jOnline.setFont(new Font("SansSerif", Font.PLAIN, 12));
        jOnline.setForeground(TEXT_MUTED);
        statusPanel.add(statusDot);
        statusPanel.add(jOnline);

        jbEliminar = new JButton("Eliminar");
        styleButtonDanger(jbEliminar);
        jbEliminar.setAlignmentX(Component.LEFT_ALIGNMENT);
        jbEliminar.addActionListener(evt -> deleteSelectedContact());

        leftHeader.add(contactsTitle);
        leftHeader.add(Box.createVerticalStrut(6));
        leftHeader.add(statusPanel);
        leftHeader.add(Box.createVerticalStrut(10));
        leftHeader.add(jbEliminar);

        leftPanel.add(leftHeader, BorderLayout.NORTH);
        leftPanel.add(contactScrollPane, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(BG_APP);

        JPanel topBar = new JPanel(new GridBagLayout());
        topBar.setBackground(BG_PANEL);
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        topBar.setPreferredSize(new Dimension(0, 72));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 12, 6, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;

        gbc.gridx = 0;
        gbc.weightx = 0;
        topBar.add(makeFieldLabel("IP"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        topBar.add(jIP, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        topBar.add(makeFieldLabel("Usuario"), gbc);

        gbc.gridx = 3;
        gbc.weightx = 1;
        topBar.add(jTextUserName, gbc);

        gbc.gridx = 4;
        gbc.weightx = 0;
        topBar.add(jbConectar, gbc);

        gbc.gridx = 5;
        topBar.add(jBforBuzzing, gbc);

        JPanel inputBar = new JPanel(new BorderLayout(8, 8));
        inputBar.setBackground(BG_PANEL);
        inputBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
        inputBar.add(jTextMensaje, BorderLayout.CENTER);
        inputBar.add(jbEnviar, BorderLayout.EAST);

        rightPanel.add(topBar, BorderLayout.NORTH);
        rightPanel.add(messagesScrollPane, BorderLayout.CENTER);
        rightPanel.add(inputBar, BorderLayout.SOUTH);

        root.add(leftPanel, BorderLayout.WEST);
        root.add(rightPanel, BorderLayout.CENTER);

        setMinimumSize(new Dimension(900, 600));
        pack();
        setLocationRelativeTo(null);
    }
    public void init() {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> this.setVisible(true));
    }

    private javax.swing.JTextField jIP;
    private javax.swing.JTextField jTextUserName;
    private javax.swing.JTextField jTextMensaje;
    private javax.swing.JLabel jOnline;
    private javax.swing.JLabel statusDot;
    private JPanel messagesPanel;
    private JScrollPane messagesScrollPane;
    private JButton jbConectar;
    private JButton jbEnviar;
    private JButton jBforBuzzing;
    private JButton jbEliminar;
    private DotIcon statusIcon;
    private DefaultListModel<ContactListItem> contactListModel;
    private JList<ContactListItem> contactList;
    private String selectedContactCode;

    private void loadContacts() {
        contactListModel.clear();
        ContactDao contactDao = new ContactDao();
        try {
            for (Contact contact : contactDao.findAll()) {
                String name = contact.getName() != null ? contact.getName() : "(Sin nombre)";
                String ip = contact.getIp() != null ? contact.getIp() : "";
                ContactListItem item = new ContactListItem(name, ip, contact.getCode(), false);
                contactListModel.addElement(item);
            }
            refreshContactPresence();
            selectFirstContact();
        } catch (Exception e) {
            showError("No se pudieron cargar los contactos: " + e.getMessage());
        }
    }

    private void selectFirstContact() {
        if (contactListModel.size() > 0 && contactList.getSelectedIndex() < 0) {
            contactList.setSelectedIndex(0);
            ContactListItem selected = contactList.getSelectedValue();
            if (selected != null) {
                selectedContactCode = selected.getCode();
            }
        }
    }

    private void loadMessagesForContact(ContactListItem contact) {
        if (contact == null) {
            return;
        }
        messagesPanel.removeAll();
        MessageDAO messageDAO = new MessageDAO();
        try {
            java.util.List<Message> messages = new java.util.ArrayList<>();
            if (contact.getCode() != null) {
                messages = messageDAO.findByParticipants(userId.toString(), contact.getCode());
            }
            if (messages.isEmpty() && contact.getIp() != null && !contact.getIp().isBlank()) {
                messages = messageDAO.findByRoomCode(contact.getIp());
            }
            for (Message message : messages) {
                if (message.getMessage() == null || message.getMessage().isBlank()) {
                    continue;
                }
                boolean outgoing = userId.toString().equals(message.getSenderCode());
                String senderName = outgoing ? username : contact.getName();
                String time = extractTime(message.getCreatedDate());
                addChatMessageWithTime(message.getMessage(), outgoing, senderName, time);
            }
            messagesPanel.revalidate();
            messagesPanel.repaint();
        } catch (Exception e) {
            showError("No se pudieron cargar los mensajes: " + e.getMessage());
        }
    }

    private String extractTime(String dateValue) {
        if (dateValue == null || dateValue.isBlank()) {
            return LocalTime.now().format(TIME_FORMAT);
        }
        try {
            return java.time.LocalDateTime.parse(dateValue, DATE_FORMAT).toLocalTime().format(TIME_FORMAT);
        } catch (Exception e) {
            return LocalTime.now().format(TIME_FORMAT);
        }
    }

    private void refreshContactPresence() {
        Map<String, SocketClient> clients = Mediator.getInstance().getClients();
        for (int i = 0; i < contactListModel.size(); i++) {
            ContactListItem item = contactListModel.get(i);
            item.setOnline(isContactOnline(item, clients));
        }
        contactList.repaint();
    }

    public void reloadContacts() {
        String currentCode = selectedContactCode;
        loadContacts();
        if (currentCode != null) {
            for (int i = 0; i < contactListModel.size(); i++) {
                if (currentCode.equals(contactListModel.get(i).getCode())) {
                    contactList.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private boolean isContactOnline(ContactListItem item, Map<String, SocketClient> clients) {
        if (item.getCode() != null && clients.containsKey(item.getCode())) {
            return true;
        }
        if (item.getIp() == null || item.getIp().isBlank()) {
            return false;
        }
        for (SocketClient client : clients.values()) {
            if (item.getIp().equals(client.getIp())) {
                return true;
            }
        }
        return false;
    }

    private void updateStatusIndicator(String status) {
        String lower = status == null ? "" : status.toLowerCase(Locale.ROOT);
        if (lower.contains("online")) {
            statusIcon.setColor(ACCENT);
        } else if (lower.contains("enviando")) {
            statusIcon.setColor(new Color(0xF44242));
        } else if (lower.contains("rejected") || lower.contains("rechaz")) {
            statusIcon.setColor(new Color(0xE74C3C));
        } else {
            statusIcon.setColor(new Color(0xE74C3C));
        }
        statusDot.repaint();
    }

    private JLabel makeFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_MUTED);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return label;
    }

    private void styleButtonPrimary(JButton button) {
        button.setBackground(ACCENT);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
    }

    private void styleButtonGhost(JButton button) {
        button.setBackground(BG_PANEL);
        button.setForeground(TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(BORDER));
    }

    private void styleButtonDanger(JButton button) {
        button.setBackground(BG_PANEL);
        button.setForeground(new Color(0xE74C3C));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(0xE74C3C)));
    }

    @Override
    public void updateStatus(String status) {
        jOnline.setText(status);
        updateStatusIndicator(status);
        refreshContactPresence();
    }

    @Override
    public void addChatMessage(String message, boolean outgoing, String senderName) {
        addChatMessageWithTime(message, outgoing, senderName, LocalTime.now().format(TIME_FORMAT));
    }

    private void addChatMessageWithTime(String message, boolean outgoing, String senderName, String time) {
        Runnable task = () -> {
            MessageRow row = new MessageRow(message, outgoing, senderName, time);
            messagesPanel.add(row);
            messagesPanel.add(Box.createVerticalStrut(8));
            messagesPanel.revalidate();
            messagesPanel.repaint();
            JScrollBar bar = messagesScrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        };
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            SwingUtilities.invokeLater(task);
        }
    }

    @Override
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Informaci\u00f3n", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void showError(String error) {
        JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public boolean showInvitationDialog(String userName, String id) {
        int respuesta = JOptionPane.showConfirmDialog(this,
                "Invitaci\u00f3n recibida de: " + userName + " (ID: " + id + "). \u00bfAceptar?",
                "Nueva Conexi\u00f3n Entrante",
                JOptionPane.YES_NO_OPTION);
        return respuesta == JOptionPane.YES_OPTION;
    }

    @Override
    public void showBuzzNotification(String senderName) {
        JOptionPane.showMessageDialog(null, senderName + " Te ha enviado un zumbido", "Zumbido", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void showClientOffline(String senderName) {
        JOptionPane.showMessageDialog(null, senderName + " se ha desconectado", "Desconectado", JOptionPane.INFORMATION_MESSAGE);
        refreshContactPresence();
    }

    private void deleteSelectedContact() {
        ContactListItem selected = contactList.getSelectedValue();
        if (selected == null || selected.getCode() == null) {
            showMessage("Selecciona un contacto para eliminar.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "\u00bfEliminar a " + selected.getName() + "?",
                "Eliminar contacto",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            new ContactDao().deleteByCode(selected.getCode());
            selectedContactCode = null;
            messagesPanel.removeAll();
            messagesPanel.revalidate();
            messagesPanel.repaint();
            reloadContacts();
        } catch (Exception e) {
            showError("No se pudo eliminar el contacto: " + e.getMessage());
        }
    }

    private final class MessageRow extends JPanel {
        private MessageRow(String message, boolean outgoing, String senderName, String time) {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

            AvatarView avatar = new AvatarView(senderName, outgoing);
            BubblePanel bubble = new BubblePanel(message, outgoing, time);
            bubble.setAlignmentY(Component.TOP_ALIGNMENT);
            avatar.setAlignmentY(Component.TOP_ALIGNMENT);

            if (outgoing) {
                add(Box.createHorizontalGlue());
                add(bubble);
                add(Box.createHorizontalStrut(8));
                add(avatar);
            } else {
                add(avatar);
                add(Box.createHorizontalStrut(8));
                add(bubble);
                add(Box.createHorizontalGlue());
            }
        }
    }

    private final class BubblePanel extends JPanel {
        private final Color bubbleColor;

        private BubblePanel(String message, boolean outgoing, String time) {
            this.bubbleColor = outgoing ? BUBBLE_OUT : BUBBLE_IN;
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(8, 10, 6, 10));
            setMaximumSize(new Dimension(MESSAGE_MAX_WIDTH, Integer.MAX_VALUE));

            JTextArea messageArea = new JTextArea(message == null ? "" : message);
            messageArea.setLineWrap(true);
            messageArea.setWrapStyleWord(true);
            messageArea.setEditable(false);
            messageArea.setOpaque(false);
            messageArea.setBorder(BorderFactory.createEmptyBorder());
            messageArea.setMargin(new Insets(0, 0, 0, 0));
            messageArea.setFocusable(false);
            messageArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
            messageArea.setForeground(TEXT_PRIMARY);
            messageArea.setColumns(24);

            JLabel timeLabel = new JLabel(time == null || time.isBlank() ? LocalTime.now().format(TIME_FORMAT) : time);
            timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
            timeLabel.setForeground(TIME_TEXT);

            JPanel timePanel = new JPanel(new BorderLayout());
            timePanel.setOpaque(false);
            timePanel.add(timeLabel, BorderLayout.EAST);

            add(messageArea, BorderLayout.CENTER);
            add(timePanel, BorderLayout.SOUTH);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bubbleColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private final class AvatarView extends JComponent {
        private final String label;
        private final Color Color;

        private AvatarView(String name, boolean outgoing) {
            String trimmed = name == null ? "" : name.trim();
            this.label = trimmed.isEmpty() ? "?" : trimmed.substring(0, 1).toUpperCase(Locale.ROOT);
            this.Color = outgoing ? new Color(0xA0AEC0) : pickAvatarColor(trimmed);
            Dimension size = new Dimension(28, 28);
            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color);
            g2.fillOval(0, 0, getWidth(), getHeight());
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(label)) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(label, x, y);
            g2.dispose();
        }
    }

    private Color pickAvatarColor(String seed) {
        int[] palette = new int[]{0x1ABC9C, 0x3498DB, 0x9B59B6, 0xE67E22, 0xE74C3C, 0x2ECC71};
        int index = Math.abs((seed == null ? 0 : seed.hashCode())) % palette.length;
        return new Color(palette[index]);
    }

    private static final class ContactListItem {
        private final String name;
        private final String ip;
        private final String code;
        private boolean online;

        private ContactListItem(String name, String ip, String code, boolean online) {
            this.name = name;
            this.ip = ip;
            this.code = code;
            this.online = online;
        }

        public String getName() {
            return name;
        }

        public String getIp() {
            return ip;
        }

        public String getCode() {
            return code;
        }

        public boolean isOnline() {
            return online;
        }

        public void setOnline(boolean online) {
            this.online = online;
        }
    }

    private static final class DotIcon implements Icon {
        private Color color;
        private final int size;

        private DotIcon(Color color, int size) {
            this.color = color;
            this.size = size;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(x, y, size, size);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }

    private final class ContactRenderer extends JPanel implements ListCellRenderer<ContactListItem> {
        private final JLabel dotLabel = new JLabel();
        private final JLabel nameLabel = new JLabel();
        private final JLabel ipLabel = new JLabel();

        private ContactRenderer() {
            setLayout(new BorderLayout(10, 0));
            setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            setOpaque(true);

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);

            nameLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
            nameLabel.setForeground(TEXT_PRIMARY);
            ipLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
            ipLabel.setForeground(TEXT_MUTED);

            textPanel.add(nameLabel);
            textPanel.add(ipLabel);

            add(dotLabel, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends ContactListItem> list,
                ContactListItem value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            if (value != null) {
                nameLabel.setText(value.getName());
                ipLabel.setText(value.getIp() == null || value.getIp().isBlank() ? "" : value.getIp());
                DotIcon icon = new DotIcon(value.isOnline() ? ACCENT : new Color(0xE74C3C), 10);
                dotLabel.setIcon(icon);
            }
            setBackground(isSelected ? new Color(0xEAF5EF) : BG_PANEL);
            return this;
        }
    }
}
