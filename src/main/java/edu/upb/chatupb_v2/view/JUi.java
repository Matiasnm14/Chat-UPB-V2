/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.controller.ContactController;
import edu.upb.chatupb_v2.controller.MessageController;
import edu.upb.chatupb_v2.controller.Mediator;
import edu.upb.chatupb_v2.model.entities.comands.AcceptHello;
import edu.upb.chatupb_v2.model.entities.enums.TypeMessage;
import edu.upb.chatupb_v2.model.repository.MessageDAO;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.nio.file.Files;

/**
 *
 * @author USER 1
 */
@Getter
public class JUi extends JFrame implements IChatView {
    //YA NO HAY CHAT SERVER!
    private final String username;
    private final UUID userId;
    private final ContactController contactController;
    private final MessageController messageController;
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
    private static final Color PRESENCE_ONLINE = new Color(0x25D366);
    private static final int MESSAGE_MAX_WIDTH = 360;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Icon CHECK_SENT_ICON = loadIcon("/images/check_sent.png");
    private static final Icon CHECK_READ_ICON = loadIcon("/images/check_read.png");

    public JUi() {
        this(null);
    }

    public JUi(String username) {
        this.username = sanitizeUsername(username);
        this.userId = edu.upb.chatupb_v2.UserIdentity.loadOrCreateUserId();
        this.contactController = new ContactController(this);
        this.messageController = new MessageController(this);
        initComponents();
    }

    private void initComponents() {
        setTitle("Chat UPB");
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jIP = new javax.swing.JTextField();
        jTextUserName = new JTextField();
        jTextUserName.setText(username);
        jTextMensaje = new javax.swing.JTextField();

        jbConectar = new JButton("Conectar");
        jbEnviar = new JButton("Enviar");
        jbImagen = new JButton("Imagen");

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
        contactList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    ContactListItem selected = contactList.getSelectedValue();
                    if (selected != null && !selected.isOnline()) {
                        String senderName = jTextUserName.getText();
                        if (senderName == null || senderName.isBlank()) {
                            senderName = username;
                        }
                        final String finalSenderName = senderName;
                        new Thread(() -> Mediator.getInstance().connectToContact(selected.getCode(), selected.getIp(), userId.toString(), finalSenderName)).start();
                    }
                }
            }
        });
        contactList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                ContactListItem selected = contactList.getSelectedValue();
                if (selected != null) {
                    selectedContactCode = selected.getCode();
                    loadMessagesForContact(selected);
                    Mediator.getInstance().setActiveContact(selected.getCode(), selected.getIp());
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
        styleButtonGhost(jbImagen);

        jbConectar.addActionListener(evt -> {
            String senderName = jTextUserName.getText();
            if (senderName == null || senderName.isBlank()) {
                senderName = username;
            }
            String target = jIP.getText();
            if (target == null || target.isBlank()) {
                showMessage("Ingresa el contacto.");
                return;
            }
            String trimmedTarget = target.trim();
            if (looksLikeIp(trimmedTarget)) {
                Mediator.getInstance().invitacion(trimmedTarget, userId.toString(), senderName);
            } else {
                Mediator.getInstance().connectToContact(trimmedTarget, null, userId.toString(), senderName);
            }
        });
        jbEnviar.addActionListener(evt -> {
            String text = jTextMensaje.getText() == null ? "" : jTextMensaje.getText().trim();
            if (text.isEmpty()) {
                return;
            }
            String senderName = jTextUserName.getText();
            if (senderName == null || senderName.isBlank()) {
                senderName = username;
            }
            ContactListItem selected = contactList.getSelectedValue();
            if (selected == null) {
                showMessage("Selecciona un contacto para enviar el mensaje.");
                return;
            }
            String messageId = UUID.randomUUID().toString();
            addChatMessageWithTime(text, true, senderName, LocalTime.now().format(TIME_FORMAT), messageId, false);
            Mediator.getInstance().sendMessage(text, userId.toString(), messageId, selected.getCode(), selected.getIp());
            jTextMensaje.setText("");
        });
        jbImagen.addActionListener(evt -> sendImage());

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
        topBar.add(makeFieldLabel("Contacto"), gbc);

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


        JPanel inputBar = new JPanel(new BorderLayout(8, 8));
        inputBar.setBackground(BG_PANEL);
        inputBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
        inputBar.add(jTextMensaje, BorderLayout.CENTER);
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);
        actionPanel.add(jbImagen);
        actionPanel.add(jbEnviar);
        inputBar.add(actionPanel, BorderLayout.EAST);

        rightPanel.add(topBar, BorderLayout.NORTH);
        rightPanel.add(messagesScrollPane, BorderLayout.CENTER);
        rightPanel.add(inputBar, BorderLayout.SOUTH);

        root.add(leftPanel, BorderLayout.WEST);
        root.add(rightPanel, BorderLayout.CENTER);

        setMinimumSize(new Dimension(900, 600));
        pack();
        setLocationRelativeTo(null);
    }

    private static Icon loadIcon(String path) {
        java.net.URL url = JUi.class.getResource(path);
        return url != null ? new ImageIcon(url) : null;
    }

    private static String sanitizeUsername(String value) {
        if (value == null) {
            return "Usuario";
        }
        String trimmed = value.trim();
        if (trimmed.isBlank()) {
            return "Usuario";
        }
        return trimmed;
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

        contactController.unload();
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
    private JButton jbImagen;
    private JButton jbEliminar;
    private DotIcon statusIcon;
    private DefaultListModel<ContactListItem> contactListModel;
    private JList<ContactListItem> contactList;
    private String selectedContactCode;
    private final java.util.Map<String, JLabel> outgoingStatusById = new java.util.HashMap<>();
    private boolean autoHelloSent = false;

    private void loadContacts() {
        contactController.unload();
    }
    // contact controller y devolverme una lista de contactos

    private void selectFirstContact() {
        if (contactListModel.size() > 0 && contactList.getSelectedIndex() < 0) {
            contactList.setSelectedIndex(0);
            ContactListItem selected = contactList.getSelectedValue();
            if (selected != null) {
                selectedContactCode = selected.getCode();
                Mediator.getInstance().setActiveContact(selected.getCode(), selected.getIp());
            }
        }
    }

    private void loadMessagesForContact(ContactListItem contact) {
        if (contact == null) {
            return;
        }
        messageController.unloadForContact(userId.toString(), contact.getCode(), contact.getIp());
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

    @Override
    public void refreshContactPresence() {
        for (int i = 0; i < contactListModel.size(); i++) {
            ContactListItem item = contactListModel.get(i);
            item.setOnline(Mediator.getInstance().isContactOnline(item.getCode(), item.getIp()));
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

    private void updateStatusIndicator(String status) {
        String lower = status == null ? "" : status.toLowerCase(Locale.ROOT);
        if (lower.contains("online")) {
            statusIcon.setColor(PRESENCE_ONLINE);
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

    private boolean looksLikeIp(String value) {
        return value != null && value.matches("\\d{1,3}(\\.\\d{1,3}){3}");
    }

    private void styleButtonPrimary(JButton button) {
        button.setBackground(ACCENT);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
    }
// Stilo de mi interfase
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
        addChatMessageWithTime(message, outgoing, senderName, LocalTime.now().format(TIME_FORMAT), null, false);
    }

    @Override
    public void addImageMessage(String imageBase64, boolean outgoing, String senderName) {
        addImageMessageWithTime(imageBase64, outgoing, senderName, LocalTime.now().format(TIME_FORMAT), null, false);
    }

    @Override
    public void unload(List<AcceptHello.User.Contact> contacts) {
        contactListModel.clear();
        if (contacts != null) {
            for (AcceptHello.User.Contact contact : contacts) {
                String name = contact.getName() != null ? contact.getName() : "(Sin nombre)";
                String ip = contact.getIp() != null ? contact.getIp() : "";
                ContactListItem item = new ContactListItem(name, ip, contact.getCode(), false);
                contactListModel.addElement(item);
            }
        }
        refreshContactPresence();
        selectFirstContact();
        if (!autoHelloSent) {
            autoHelloSent = true;
            autoSendHelloToContacts(contacts);
        }
    }

    @Override
    public void unloadMessages(List<MessageDAO.Message> messages) {
        messagesPanel.removeAll();
        outgoingStatusById.clear();
        if (messages != null) {
            for (MessageDAO.Message message : messages) {
                if (message.getMessage() == null || message.getMessage().isBlank()) {
                    continue;
                }
                boolean outgoing = userId.toString().equals(message.getSenderCode());
                String senderName = outgoing ? username : "Desconocido";
                String time = extractTime(message.getCreatedDate());
                if (message.getType() == TypeMessage.IMAGE) {
                    addImageMessageWithTime(message.getMessage(), outgoing, senderName, time, message.getCodMessage(), false);
                } else {
                    addChatMessageWithTime(message.getMessage(), outgoing, senderName, time, message.getCodMessage(), false);
                }
            }
        }
        messagesPanel.revalidate();
        messagesPanel.repaint();
    }

    private void addChatMessageWithTime(String message, boolean outgoing, String senderName, String time, String messageId, boolean read) {
        Runnable task = () -> {
            boolean resolvedRead = read;
            if (outgoing && messageId != null && !messageId.isBlank() && !read) {
                resolvedRead = Mediator.getInstance().isMessageRead(messageId);
            }
            MessageRow row = new MessageRow(message, null, outgoing, senderName, time, messageId, resolvedRead);
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

    private void addImageMessageWithTime(String imageBase64, boolean outgoing, String senderName, String time, String messageId, boolean read) {
        Runnable task = () -> {
            boolean resolvedRead = read;
            if (outgoing && messageId != null && !messageId.isBlank() && !read) {
                resolvedRead = Mediator.getInstance().isMessageRead(messageId);
            }
            ImageIcon icon = decodeImage(imageBase64);
            String fallback = icon == null ? "Imagen no disponible" : null;
            MessageRow row = new MessageRow(fallback, icon, outgoing, senderName, time, messageId, resolvedRead);
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
                "Invitaci\u00f3n recibida de: " + userName + ". \u00bfAceptar?",
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

    @Override
    public void markMessageRead(String messageId) {
        if (messageId == null || messageId.isBlank()) {
            return;
        }
        Runnable task = () -> {
            JLabel statusLabel = outgoingStatusById.get(messageId);
            if (statusLabel != null) {
                statusLabel.setIcon(CHECK_READ_ICON);
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            SwingUtilities.invokeLater(task);
        }
    }

    private void autoSendHelloToContacts(List<AcceptHello.User.Contact> contacts) {
        if (contacts == null || contacts.isEmpty()) {
            return;
        }
        String senderName = jTextUserName != null ? jTextUserName.getText() : null;
        if (senderName == null || senderName.isBlank()) {
            senderName = username;
        }
        String finalSenderName = senderName;
        for (AcceptHello.User.Contact contact : contacts) {
            if (contact == null) {
                continue;
            }
            String code = contact.getCode();
            String ip = contact.getIp();
            if (code != null && code.equals(userId.toString())) {
                continue;
            }
            if (ip == null || ip.isBlank()) {
                continue;
            }
            new Thread(() -> Mediator.getInstance().connectToContact(code, ip, userId.toString(), finalSenderName)).start();
        }
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
            contactController.deleteByCode(selected.getCode());
            selectedContactCode = null;
            messagesPanel.removeAll();
            messagesPanel.revalidate();
            messagesPanel.repaint();
            reloadContacts();
        } catch (Exception e) {
            showError("No se pudo eliminar el contacto: " + e.getMessage());
        }
    }

    private void sendImage() {
        ContactListItem selected = contactList.getSelectedValue();
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imagenes", "png", "jpg", "jpeg", "gif", "bmp"));
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (file == null) {
            return;
        }
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String base64 = Base64.getEncoder().encodeToString(bytes);
            String senderName = jTextUserName.getText();
            if (senderName == null || senderName.isBlank()) {
                senderName = username;
            }
            String messageId = UUID.randomUUID().toString();
            addImageMessageWithTime(base64, true, senderName, LocalTime.now().format(TIME_FORMAT), messageId, false);
            Mediator.getInstance().sendImage(base64, userId.toString(), messageId, selected.getCode(), selected.getIp());
        } catch (Exception e) {
            showError("No se pudo enviar la imagen: " + e.getMessage());
        }
    }

    private final class MessageRow extends JPanel {
        private MessageRow(String message, ImageIcon image, boolean outgoing, String senderName, String time, String messageId, boolean read) {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

            AvatarView avatar = new AvatarView(senderName, outgoing);
            BubblePanel bubble = new BubblePanel(message, image, outgoing, time, read);
            bubble.setAlignmentY(Component.TOP_ALIGNMENT);
            avatar.setAlignmentY(Component.TOP_ALIGNMENT);
            if (outgoing && messageId != null) {
                JLabel statusLabel = bubble.getStatusLabel();
                if (statusLabel != null) {
                    outgoingStatusById.put(messageId, statusLabel);
                }
            }

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
        private JLabel statusLabel;

        private BubblePanel(String message, ImageIcon image, boolean outgoing, String time, boolean read) {
            this.bubbleColor = outgoing ? BUBBLE_OUT : BUBBLE_IN;
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(8, 10, 6, 10));
            setMaximumSize(new Dimension(MESSAGE_MAX_WIDTH, Integer.MAX_VALUE));

            if (image != null) {
                JLabel imageLabel = new JLabel(image);
                imageLabel.setOpaque(false);
                add(imageLabel, BorderLayout.CENTER);
            } else {
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
                add(messageArea, BorderLayout.CENTER);
            }

            JLabel timeLabel = new JLabel(time == null || time.isBlank() ? LocalTime.now().format(TIME_FORMAT) : time);
            timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
            timeLabel.setForeground(TIME_TEXT);

            JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
            timePanel.setOpaque(false);
            timePanel.add(timeLabel);
            if (outgoing) {
                statusLabel = new JLabel();
                statusLabel.setIcon(read ? CHECK_READ_ICON : CHECK_SENT_ICON);
                timePanel.add(statusLabel);
            }

            add(timePanel, BorderLayout.SOUTH);
        }

        public JLabel getStatusLabel() {
            return statusLabel;
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

    private ImageIcon decodeImage(String base64) {
        if (base64 == null || base64.isBlank()) {
            return null;
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            ImageIcon icon = new ImageIcon(bytes);
            if (icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) {
                return null;
            }
            return scaleIcon(icon, 240, 240);
        } catch (Exception e) {
            return null;
        }
    }

    private ImageIcon scaleIcon(ImageIcon icon, int maxWidth, int maxHeight) {
        int width = icon.getIconWidth();
        int height = icon.getIconHeight();
        if (width <= 0 || height <= 0) {
            return icon;
        }
        if (width <= maxWidth && height <= maxHeight) {
            return icon;
        }
        double ratio = Math.min((double) maxWidth / width, (double) maxHeight / height);
        int newWidth = (int) Math.round(width * ratio);
        int newHeight = (int) Math.round(height * ratio);
        Image scaled = icon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
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

        @Override
        public String toString() {
            String safeName = name != null ? name : "(Sin nombre)";
            String safeCode = code != null ? code : "";
            return safeCode.isBlank() ? safeName : safeName + " (" + safeCode + ")";
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
            ipLabel.setVisible(false);

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
                ipLabel.setText("");
                DotIcon icon = new DotIcon(value.isOnline() ? PRESENCE_ONLINE : new Color(0xE74C3C), 10);
                dotLabel.setIcon(icon);
            }
            setBackground(isSelected ? new Color(0xEAF5EF) : BG_PANEL);
            return this;
        }
    }
}//172.16.41.214

