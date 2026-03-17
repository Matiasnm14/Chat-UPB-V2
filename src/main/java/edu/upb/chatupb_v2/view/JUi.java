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
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private static final Icon CHECK_SENT_ICON = loadIcon("/images/check_sent.png");
    private static final Icon CHECK_READ_ICON = loadIcon("/images/check_read.png");
    private final BuzzSoundPlayer buzzSoundPlayer;
    private Color currentBgApp = BG_APP;
    private Color currentPanel = BG_PANEL;
    private Color currentChatBg = CHAT_BG;
    private Color currentAccent = ACCENT;
    private Color currentTextPrimary = TEXT_PRIMARY;
    private Color currentTextMuted = TEXT_MUTED;
    private Color currentBorder = BORDER;
    private Color currentBubbleOut = BUBBLE_OUT;
    private Color currentBubbleIn = BUBBLE_IN;
    private Color currentTimeText = TIME_TEXT;
    private Color currentPresenceOnline = PRESENCE_ONLINE;
    private Color currentUniqueBubble = new Color(0xFFF3CD);
    private ChatThemeOption currentTheme = ChatThemeOption.DEFAULT;

    public JUi() {
        this(null);
    }

    public JUi(String username) {
        this.username = sanitizeUsername(username);
        this.userId = edu.upb.chatupb_v2.UserIdentity.loadOrCreateUserId();
        this.contactController = new ContactController(this);
        this.messageController = new MessageController(this);
        this.buzzSoundPlayer = new BuzzSoundPlayer();
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
        jbUnico = new JButton("Unico");
        jbZumbido = new JButton("Zumbido");
        jbFijar = new JButton("Fijar");
        jbTema = new JButton("Tema");
        jbEliminarMensaje = new JButton("Eliminar msj");
        messagePopupMenu = new JPopupMenu();
        miEliminarMensaje = new JMenuItem("Eliminar mensaje");
        miFijarMensaje = new JMenuItem("Anclar mensaje");
        messagePopupMenu.add(miFijarMensaje);
        messagePopupMenu.add(miEliminarMensaje);

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
        contactList.setBackground(currentPanel);
        contactList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    ContactListItem selected = contactList.getSelectedValue();
                    if (selected != null) {
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
                    applyTheme(selected.getThemeId());
                    loadMessagesForContact(selected);
                    Mediator.getInstance().setActiveContact(selected.getCode(), selected.getIp());
                }
            }
        });

        JScrollPane contactScrollPane = new JScrollPane(contactList);
        contactScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        contactScrollPane.setBorder(BorderFactory.createEmptyBorder());
        contactScrollPane.getViewport().setBackground(currentPanel);

        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(currentChatBg);

        messagesScrollPane = new JScrollPane(messagesPanel);
        messagesScrollPane.setBorder(BorderFactory.createEmptyBorder());
        messagesScrollPane.getViewport().setBackground(currentChatBg);
        messagesScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        styleButtonPrimary(jbConectar);
        styleButtonPrimary(jbEnviar);
        styleButtonPrimary(jbUnico);
        styleButtonGhost(jbImagen);
        styleButtonAccentOutline(jbZumbido);
        styleButtonGhost(jbFijar);
        styleButtonGhost(jbTema);
        styleButtonDanger(jbEliminarMensaje);

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
        jbUnico.addActionListener(evt -> sendUniqueMessage());
        jbZumbido.addActionListener(evt -> sendBuzz());
        jbFijar.addActionListener(evt -> pinSelectedMessage());
        jbTema.addActionListener(evt -> chooseTheme());
        jbEliminarMensaje.addActionListener(evt -> deleteSelectedMessage());
        miFijarMensaje.addActionListener(evt -> pinSelectedMessage());
        miEliminarMensaje.addActionListener(evt -> deleteSelectedMessage());

        rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(currentBgApp);
        setContentPane(rootPanel);

        leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(currentPanel);
        leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, currentBorder));
        leftPanel.setPreferredSize(new Dimension(280, 0));

        leftHeaderPanel = new JPanel();
        leftHeaderPanel.setBackground(currentPanel);
        leftHeaderPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));
        leftHeaderPanel.setLayout(new BoxLayout(leftHeaderPanel, BoxLayout.Y_AXIS));

        JLabel contactsTitle = new JLabel("Contactos");
        contactsTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        contactsTitle.setForeground(currentTextPrimary);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        statusPanel.setOpaque(false);
        jOnline.setFont(new Font("SansSerif", Font.PLAIN, 12));
        jOnline.setForeground(currentTextMuted);
        statusPanel.add(statusDot);
        statusPanel.add(jOnline);

        jbEliminar = new JButton("Eliminar");
        styleButtonDanger(jbEliminar);
        jbEliminar.setAlignmentX(Component.LEFT_ALIGNMENT);
        jbEliminar.addActionListener(evt -> deleteSelectedContact());

        leftHeaderPanel.add(contactsTitle);
        leftHeaderPanel.add(Box.createVerticalStrut(6));
        leftHeaderPanel.add(statusPanel);
        leftHeaderPanel.add(Box.createVerticalStrut(10));
        leftHeaderPanel.add(jbEliminar);

        leftPanel.add(leftHeaderPanel, BorderLayout.NORTH);
        leftPanel.add(contactScrollPane, BorderLayout.CENTER);

        rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(currentBgApp);

        topBarPanel = new JPanel(new GridBagLayout());
        topBarPanel.setBackground(currentPanel);
        topBarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, currentBorder));
        topBarPanel.setPreferredSize(new Dimension(0, 72));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 12, 6, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;

        gbc.gridx = 0;
        gbc.weightx = 0;
        topBarPanel.add(makeFieldLabel("Contacto"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        topBarPanel.add(jIP, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        topBarPanel.add(makeFieldLabel("Usuario"), gbc);

        gbc.gridx = 3;
        gbc.weightx = 1;
        topBarPanel.add(jTextUserName, gbc);

        gbc.gridx = 4;
        gbc.weightx = 0;
        topBarPanel.add(jbConectar, gbc);

        gbc.gridx = 5;
        gbc.weightx = 0;
        topBarPanel.add(jbZumbido, gbc);

        pinnedPanel = new JPanel(new BorderLayout(8, 0));
        pinnedTitleLabel = new JLabel("Fijado");
        pinnedTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        pinnedPreviewLabel = new JLabel("Sin mensaje fijado");
        pinnedPreviewLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        pinnedPanel.add(pinnedTitleLabel, BorderLayout.WEST);
        pinnedPanel.add(pinnedPreviewLabel, BorderLayout.CENTER);
        pinnedPanel.setVisible(false);

        centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(pinnedPanel, BorderLayout.NORTH);
        centerPanel.add(messagesScrollPane, BorderLayout.CENTER);

        inputBarPanel = new JPanel(new BorderLayout(8, 8));
        inputBarPanel.setBackground(currentPanel);
        inputBarPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, currentBorder));
        inputBarPanel.add(jTextMensaje, BorderLayout.CENTER);
        actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);
        actionPanel.add(jbImagen);
        actionPanel.add(jbUnico);
        actionPanel.add(jbTema);
        actionPanel.add(jbEnviar);
        inputBarPanel.add(actionPanel, BorderLayout.EAST);

        rightPanel.add(topBarPanel, BorderLayout.NORTH);
        rightPanel.add(centerPanel, BorderLayout.CENTER);
        rightPanel.add(inputBarPanel, BorderLayout.SOUTH);

        rootPanel.add(leftPanel, BorderLayout.WEST);
        rootPanel.add(rightPanel, BorderLayout.CENTER);

        setMinimumSize(new Dimension(900, 600));
        applyThemeInternal(ChatThemeOption.DEFAULT.getId());
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
    private JButton jbUnico;
    private JButton jbZumbido;
    private JButton jbFijar;
    private JButton jbTema;
    private JButton jbEliminarMensaje;
    private JButton jbEliminar;
    private JPopupMenu messagePopupMenu;
    private JMenuItem miEliminarMensaje;
    private JMenuItem miFijarMensaje;
    private DotIcon statusIcon;
    private DefaultListModel<ContactListItem> contactListModel;
    private JList<ContactListItem> contactList;
    private JPanel rootPanel;
    private JPanel leftPanel;
    private JPanel leftHeaderPanel;
    private JPanel rightPanel;
    private JPanel topBarPanel;
    private JPanel centerPanel;
    private JPanel inputBarPanel;
    private JPanel actionPanel;
    private JPanel pinnedPanel;
    private JLabel pinnedTitleLabel;
    private JLabel pinnedPreviewLabel;
    private String selectedContactCode;
    private String pinnedMessageId;
    private final java.util.Map<String, JLabel> outgoingStatusById = new java.util.HashMap<>();
    private final java.util.Map<String, JComponent> messageContainersById = new java.util.HashMap<>();
    private MessageRow selectedMessageRow;
    private Timer buzzShakeTimer;
    private Point buzzOrigin;

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
                applyTheme(selected.getThemeId());
                Mediator.getInstance().setActiveContact(selected.getCode(), selected.getIp());
            }
        }
    }

    private void loadMessagesForContact(ContactListItem contact) {
        if (contact == null) {
            return;
        }
        messageController.unloadForContact(userId.toString(), username, contact.getCode(), contact.getIp());
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
            statusIcon.setColor(currentPresenceOnline);
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
        label.setForeground(currentTextMuted);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return label;
    }

    private boolean looksLikeIp(String value) {
        return value != null && value.matches("\\d{1,3}(\\.\\d{1,3}){3}");
    }

    private void styleButtonPrimary(JButton button) {
        button.setBackground(currentAccent);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
    }
// Stilo de mi interfase
    private void styleButtonGhost(JButton button) {
        button.setBackground(currentPanel);
        button.setForeground(currentTextPrimary);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(currentBorder));
    }

    private void styleButtonDanger(JButton button) {
        button.setBackground(currentPanel);
        button.setForeground(new Color(0xE74C3C));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(0xE74C3C)));
    }

    private void styleButtonAccentOutline(JButton button) {
        button.setBackground(blendColors(currentAccent, currentPanel, 0.08f));
        button.setForeground(currentAccent);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(currentAccent));
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
    public void addChatMessage(String message, boolean outgoing, String senderName, String messageId) {
        addChatMessageWithTime(message, outgoing, senderName, LocalTime.now().format(TIME_FORMAT), messageId, false);
    }

    @Override
    public void addUniqueMessage(String message, boolean outgoing, String senderName, String messageId) {
        addUniqueMessageWithTime(message, outgoing, senderName, LocalTime.now().format(TIME_FORMAT), messageId, false);
    }

    @Override
    public void addImageMessage(String imageBase64, boolean outgoing, String senderName) {
        addImageMessageWithTime(imageBase64, outgoing, senderName, LocalTime.now().format(TIME_FORMAT), null, false);
    }

    @Override
    public void addImageMessage(String imageBase64, boolean outgoing, String senderName, String messageId) {
        addImageMessageWithTime(imageBase64, outgoing, senderName, LocalTime.now().format(TIME_FORMAT), messageId, false);
    }

    @Override
    public void unload(List<AcceptHello.User.Contact> contacts) {
        contactListModel.clear();
        if (contacts != null) {
            for (AcceptHello.User.Contact contact : contacts) {
                String name = contact.getName() != null ? contact.getName() : "(Sin nombre)";
                String ip = contact.getIp() != null ? contact.getIp() : "";
                ContactListItem item = new ContactListItem(name, ip, contact.getCode(), contact.getThemeId(), false);
                contactListModel.addElement(item);
            }
        }
        refreshContactPresence();
        selectFirstContact();
    }

    @Override
    public void unloadMessages(List<ChatMessageViewModel> messages) {
        messagesPanel.removeAll();
        outgoingStatusById.clear();
        messageContainersById.clear();
        selectedMessageRow = null;
        clearPinnedMessage();
        ChatMessageViewModel pinnedMessage = null;
        if (messages != null) {
            for (ChatMessageViewModel message : messages) {
                if (message.getContent() == null || message.getContent().isBlank()) {
                    continue;
                }
                if (message.getType() == TypeMessage.IMAGE) {
                    addImageMessageWithTime(message.getContent(), message.isOutgoing(), message.getSenderName(), message.getTime(), message.getMessageId(), message.isRead());
                } else if (message.getType() == TypeMessage.UNIQUE) {
                    addUniqueMessageWithTime(message.getContent(), message.isOutgoing(), message.getSenderName(), message.getTime(), message.getMessageId(), message.isRead());
                } else {
                    addChatMessageWithTime(message.getContent(), message.isOutgoing(), message.getSenderName(), message.getTime(), message.getMessageId(), message.isRead());
                }
                if (message.isPinned()) {
                    pinnedMessage = message;
                }
            }
        }
        if (pinnedMessage != null) {
            showPinnedMessage(pinnedMessage.getMessageId(), buildPinnedPreview(pinnedMessage));
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
            MessageRow row = new MessageRow(message, null, outgoing, senderName, time, messageId, resolvedRead, TypeMessage.TEXT);
            JComponent entry = wrapMessageRow(row);
            messagesPanel.add(entry);
            repaintMessages();
        };
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            SwingUtilities.invokeLater(task);
        }
    }

    private void addUniqueMessageWithTime(String message, boolean outgoing, String senderName, String time, String messageId, boolean read) {
        Runnable task = () -> {
            boolean resolvedRead = read;
            if (outgoing && messageId != null && !messageId.isBlank() && !read) {
                resolvedRead = Mediator.getInstance().isMessageRead(messageId);
            }
            MessageRow row = new MessageRow(message, null, outgoing, senderName, time, messageId, resolvedRead, TypeMessage.UNIQUE);
            JComponent entry = wrapMessageRow(row);
            messagesPanel.add(entry);
            repaintMessages();
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
            MessageRow row = new MessageRow(fallback, icon, outgoing, senderName, time, messageId, resolvedRead, TypeMessage.IMAGE);
            JComponent entry = wrapMessageRow(row);
            messagesPanel.add(entry);
            repaintMessages();
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
        String name = (senderName == null || senderName.isBlank()) ? "Alguien" : senderName;
        updateStatus("Zumbido de " + name);
        buzzSoundPlayer.play();
        shakeWindow();
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

    @Override
    public void removeMessage(String messageId) {
        if (messageId == null || messageId.isBlank()) {
            return;
        }
        Runnable task = () -> {
            JComponent entry = messageContainersById.remove(messageId);
            outgoingStatusById.remove(messageId);
            if (selectedMessageRow != null && messageId.equals(selectedMessageRow.getMessageId())) {
                selectedMessageRow.setSelectedState(false);
                selectedMessageRow = null;
            }
            if (messageId.equals(pinnedMessageId)) {
                clearPinnedMessage();
            }
            if (entry != null) {
                messagesPanel.remove(entry);
                messagesPanel.revalidate();
                messagesPanel.repaint();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            SwingUtilities.invokeLater(task);
        }
    }

    @Override
    public void showPinnedMessage(String messageId, String previewText) {
        Runnable task = () -> {
            pinnedMessageId = messageId;
            pinnedPreviewLabel.setText((previewText == null || previewText.isBlank()) ? "Mensaje fijado" : previewText);
            pinnedPanel.setVisible(true);
            pinnedPanel.revalidate();
            pinnedPanel.repaint();
        };
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            SwingUtilities.invokeLater(task);
        }
    }

    @Override
    public void applyTheme(String themeId) {
        Runnable task = () -> applyThemeInternal(themeId);
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            SwingUtilities.invokeLater(task);
        }
    }

    @Override
    public void applyThemeForContact(String contactCode, String themeId) {
        Runnable task = () -> {
            ContactListItem item = findContactByCode(contactCode);
            if (item != null) {
                item.setThemeId(themeId);
            }
            if (contactCode != null && contactCode.equals(selectedContactCode)) {
                applyThemeInternal(themeId);
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            SwingUtilities.invokeLater(task);
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
            outgoingStatusById.clear();
            messageContainersById.clear();
            selectedMessageRow = null;
            clearPinnedMessage();
            messagesPanel.revalidate();
            messagesPanel.repaint();
            reloadContacts();
        } catch (Exception e) {
            showError("No se pudo eliminar el contacto: " + e.getMessage());
        }
    }

    private void sendBuzz() {
        ContactListItem selected = contactList.getSelectedValue();
        if (selected == null) {
            showMessage("Selecciona un contacto para enviar el zumbido.");
            return;
        }
        Mediator.getInstance().sendBuzz(userId.toString(), selected.getCode(), selected.getIp());
    }

    private void shakeWindow() {
        if (buzzShakeTimer != null && buzzShakeTimer.isRunning()) {
            return;
        }
        buzzOrigin = getLocation();
        final int[] offsets = {0, -14, 14, -12, 12, -10, 10, -6, 6, -3, 3, 0};
        final int[] index = {0};
        buzzShakeTimer = new Timer(35, evt -> {
            int offset = offsets[index[0]];
            setLocation(buzzOrigin.x + offset, buzzOrigin.y);
            index[0]++;
            if (index[0] >= offsets.length) {
                buzzShakeTimer.stop();
                setLocation(buzzOrigin);
            }
        });
        buzzShakeTimer.start();
    }

    private void pinSelectedMessage() {
        if (selectedMessageRow == null || selectedMessageRow.getMessageId() == null || selectedMessageRow.getMessageId().isBlank()) {
            showMessage("Selecciona un mensaje para fijar.");
            return;
        }
        ContactListItem selectedContact = contactList.getSelectedValue();
        String recipientCode = selectedContact != null ? selectedContact.getCode() : null;
        String recipientIp = selectedContact != null ? selectedContact.getIp() : null;
        Mediator.getInstance().sendPinMessage(selectedMessageRow.getMessageId(), recipientCode, recipientIp);
    }

    private void deleteSelectedMessage() {
        if (selectedMessageRow == null || selectedMessageRow.getMessageId() == null || selectedMessageRow.getMessageId().isBlank()) {
            showMessage("Selecciona un mensaje para eliminar.");
            return;
        }
        if (!selectedMessageRow.isOutgoing()) {
            showMessage("Solo puedes eliminar mensajes enviados por ti.");
            return;
        }
        ContactListItem selectedContact = contactList.getSelectedValue();
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Eliminar este mensaje?",
                "Eliminar mensaje",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        String messageId = selectedMessageRow.getMessageId();
        String recipientCode = selectedContact != null ? selectedContact.getCode() : null;
        String recipientIp = selectedContact != null ? selectedContact.getIp() : null;
        Mediator.getInstance().sendDeleteMessage(messageId, recipientCode, recipientIp);
    }

    private void sendUniqueMessage() {
        ContactListItem selected = contactList.getSelectedValue();
        if (selected == null) {
            showMessage("Selecciona un contacto para enviar el mensaje unico.");
            return;
        }
        String text = jTextMensaje.getText() == null ? "" : jTextMensaje.getText().trim();
        if (text.isEmpty()) {
            showMessage("Escribe el mensaje unico.");
            return;
        }
        String senderName = jTextUserName.getText();
        if (senderName == null || senderName.isBlank()) {
            senderName = username;
        }
        String messageId = UUID.randomUUID().toString();
        addUniqueMessageWithTime(text, true, senderName, LocalTime.now().format(TIME_FORMAT), messageId, false);
        Mediator.getInstance().sendUniqueMessage(text, userId.toString(), messageId, selected.getCode(), selected.getIp());
        jTextMensaje.setText("");
    }

    private void chooseTheme() {
        ContactListItem selected = contactList.getSelectedValue();
        if (selected == null) {
            showMessage("Selecciona un contacto para cambiar su tema.");
            return;
        }
        String[] options = ChatThemeOption.displayNames();
        String selectedOption = (String) JOptionPane.showInputDialog(
                this,
                "Selecciona el tema",
                "Tema",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                currentTheme.getDisplayName()
        );
        if (selectedOption == null || selectedOption.isBlank()) {
            return;
        }
        ChatThemeOption theme = ChatThemeOption.fromDisplayName(selectedOption);
        String themeId = theme.getId();
        selected.setThemeId(themeId);
        try {
            contactController.updateThemeByCode(selected.getCode(), themeId);
        } catch (Exception e) {
            showError("No se pudo guardar el tema del contacto: " + e.getMessage());
            return;
        }
        applyTheme(themeId);
        contactList.repaint();
        Mediator.getInstance().sendTheme(userId.toString(), themeId, selected.getCode(), selected.getIp());
    }

    private void sendImage() {
        ContactListItem selected = contactList.getSelectedValue();
        if (selected == null) {
            showMessage("Selecciona un contacto para enviar la imagen.");
            return;
        }
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
        private final String messageId;
        private final boolean outgoing;

        private MessageRow(String message, ImageIcon image, boolean outgoing, String senderName, String time, String messageId, boolean read, TypeMessage type) {
            this.messageId = messageId;
            this.outgoing = outgoing;
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

            AvatarView avatar = new AvatarView(senderName, outgoing);
            BubblePanel bubble = new BubblePanel(message, image, outgoing, time, read, type);
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
            installSelectionHandler(this);
            setSelectedState(false);
        }

        public String getMessageId() {
            return messageId;
        }

        public boolean isOutgoing() {
            return outgoing;
        }

        public void setSelectedState(boolean selected) {
            Color borderColor = selected ? currentAccent : currentChatBg;
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(borderColor, 2, true),
                    BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));
            repaint();
        }

        private void installSelectionHandler(Component component) {
            component.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                        return;
                    }
                    selectMessageRow(MessageRow.this);
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    maybeShowMessageMenu(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    maybeShowMessageMenu(e);
                }
            });
            if (component instanceof Container) {
                for (Component child : ((Container) component).getComponents()) {
                    installSelectionHandler(child);
                }
            }
        }
    }

    private final class BubblePanel extends JPanel {
        private final boolean outgoing;
        private final TypeMessage type;
        private JLabel statusLabel;

        private BubblePanel(String message, ImageIcon image, boolean outgoing, String time, boolean read, TypeMessage type) {
            this.outgoing = outgoing;
            this.type = type;
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(8, 10, 6, 10));
            setMaximumSize(new Dimension(MESSAGE_MAX_WIDTH, Integer.MAX_VALUE));

            if (type == TypeMessage.UNIQUE) {
                JLabel uniqueLabel = new JLabel("UNICO");
                uniqueLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
                uniqueLabel.setForeground(new Color(0x8A6D3B));
                uniqueLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
                add(uniqueLabel, BorderLayout.NORTH);
            }

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
                messageArea.setForeground(currentTextPrimary);
                messageArea.setColumns(24);
                add(messageArea, BorderLayout.CENTER);
            }

            JLabel timeLabel = new JLabel(time == null || time.isBlank() ? LocalTime.now().format(TIME_FORMAT) : time);
            timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
            timeLabel.setForeground(currentTimeText);

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
            g2.setColor(resolveBubbleColor());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }

        private Color resolveBubbleColor() {
            if (type == TypeMessage.UNIQUE) {
                return currentUniqueBubble;
            }
            return outgoing ? currentBubbleOut : currentBubbleIn;
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

    private JComponent wrapMessageRow(MessageRow row) {
        JPanel entry = new JPanel(new BorderLayout());
        entry.setOpaque(false);
        entry.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        entry.add(row, BorderLayout.CENTER);
        if (row.getMessageId() != null && !row.getMessageId().isBlank()) {
            messageContainersById.put(row.getMessageId(), entry);
        }
        return entry;
    }

    private void selectMessageRow(MessageRow row) {
        if (row == null || row.getMessageId() == null || row.getMessageId().isBlank()) {
            return;
        }
        if (selectedMessageRow != null && selectedMessageRow != row) {
            selectedMessageRow.setSelectedState(false);
        }
        selectedMessageRow = row;
        selectedMessageRow.setSelectedState(true);
    }

    private void maybeShowMessageMenu(MouseEvent event) {
        if (event == null || !event.isPopupTrigger()) {
            return;
        }
        if (!(event.getComponent() instanceof Component)) {
            return;
        }
        Component component = event.getComponent();
        MessageRow row = resolveMessageRow(component);
        if (row == null) {
            return;
        }
        selectMessageRow(row);
        miFijarMensaje.setEnabled(true);
        miEliminarMensaje.setEnabled(row.isOutgoing());
        messagePopupMenu.show(component, event.getX(), event.getY());
    }

    private MessageRow resolveMessageRow(Component component) {
        Component current = component;
        while (current != null) {
            if (current instanceof MessageRow) {
                return (MessageRow) current;
            }
            current = current.getParent();
        }
        return null;
    }

    private ContactListItem findContactByCode(String contactCode) {
        if (contactCode == null || contactCode.isBlank()) {
            return null;
        }
        for (int i = 0; i < contactListModel.size(); i++) {
            ContactListItem item = contactListModel.get(i);
            if (contactCode.equals(item.getCode())) {
                return item;
            }
        }
        return null;
    }

    private static final class ContactListItem {
        private final String name;
        private final String ip;
        private final String code;
        private String themeId;
        private boolean online;

        private ContactListItem(String name, String ip, String code, String themeId, boolean online) {
            this.name = name;
            this.ip = ip;
            this.code = code;
            this.themeId = themeId;
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

        public String getThemeId() {
            return themeId;
        }

        public boolean isOnline() {
            return online;
        }

        public void setThemeId(String themeId) {
            this.themeId = themeId;
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
            nameLabel.setForeground(currentTextPrimary);
            ipLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
            ipLabel.setForeground(currentTextMuted);
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
                nameLabel.setForeground(currentTextPrimary);
                ipLabel.setForeground(currentTextMuted);
                ipLabel.setText("");
                DotIcon icon = new DotIcon(value.isOnline() ? currentPresenceOnline : new Color(0xE74C3C), 10);
                dotLabel.setIcon(icon);
            }
            setBackground(isSelected ? blendColors(currentAccent, currentPanel, 0.15f) : currentPanel);
            return this;
        }
    }

    private void repaintMessages() {
        messagesPanel.revalidate();
        messagesPanel.repaint();
        JScrollBar bar = messagesScrollPane.getVerticalScrollBar();
        bar.setValue(bar.getMaximum());
    }

    private void clearPinnedMessage() {
        pinnedMessageId = null;
        if (pinnedPreviewLabel != null) {
            pinnedPreviewLabel.setText("Sin mensaje fijado");
        }
        if (pinnedPanel != null) {
            pinnedPanel.setVisible(false);
            pinnedPanel.revalidate();
            pinnedPanel.repaint();
        }
    }

    private String buildPinnedPreview(ChatMessageViewModel message) {
        if (message == null) {
            return "Mensaje fijado";
        }
        if (message.getType() == TypeMessage.IMAGE) {
            return "Imagen";
        }
        String text = message.getContent();
        if (text == null || text.isBlank()) {
            return "Mensaje fijado";
        }
        String normalized = text.replace('\n', ' ').trim();
        if (normalized.length() > 40) {
            return normalized.substring(0, 40) + "...";
        }
        return normalized;
    }

    private void applyThemeInternal(String themeId) {
        currentTheme = ChatThemeOption.fromId(themeId);
        currentBgApp = currentTheme.getBgApp();
        currentPanel = currentTheme.getPanel();
        currentChatBg = currentTheme.getChatBg();
        currentAccent = currentTheme.getAccent();
        currentTextPrimary = currentTheme.getTextPrimary();
        currentTextMuted = currentTheme.getTextMuted();
        currentBorder = currentTheme.getBorder();
        currentBubbleOut = currentTheme.getBubbleOut();
        currentBubbleIn = currentTheme.getBubbleIn();
        currentTimeText = currentTheme.getTimeText();
        currentPresenceOnline = currentTheme.getPresenceOnline();
        currentUniqueBubble = currentTheme.getUniqueBubble();
        refreshThemeUi();
    }

    private void refreshThemeUi() {
        if (rootPanel != null) {
            rootPanel.setBackground(currentBgApp);
        }
        if (leftPanel != null) {
            leftPanel.setBackground(currentPanel);
            leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, currentBorder));
        }
        if (leftHeaderPanel != null) {
            leftHeaderPanel.setBackground(currentPanel);
        }
        if (rightPanel != null) {
            rightPanel.setBackground(currentBgApp);
        }
        if (topBarPanel != null) {
            topBarPanel.setBackground(currentPanel);
            topBarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, currentBorder));
        }
        if (inputBarPanel != null) {
            inputBarPanel.setBackground(currentPanel);
            inputBarPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, currentBorder));
        }
        if (pinnedPanel != null) {
            pinnedPanel.setBackground(blendColors(currentAccent, currentPanel, 0.10f));
            pinnedPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, currentBorder),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
        }
        if (pinnedTitleLabel != null) {
            pinnedTitleLabel.setForeground(currentAccent);
        }
        if (pinnedPreviewLabel != null) {
            pinnedPreviewLabel.setForeground(currentTextPrimary);
        }
        if (messagesPanel != null) {
            messagesPanel.setBackground(currentChatBg);
        }
        if (messagesScrollPane != null) {
            messagesScrollPane.getViewport().setBackground(currentChatBg);
        }
        if (contactList != null) {
            contactList.setBackground(currentPanel);
            contactList.repaint();
        }
        if (jOnline != null) {
            jOnline.setForeground(currentTextMuted);
        }
        styleTextField(jIP);
        styleTextField(jTextUserName);
        styleTextField(jTextMensaje);
        styleButtonPrimary(jbConectar);
        styleButtonPrimary(jbEnviar);
        styleButtonPrimary(jbUnico);
        styleButtonGhost(jbImagen);
        styleButtonAccentOutline(jbZumbido);
        styleButtonGhost(jbFijar);
        styleButtonGhost(jbTema);
        styleButtonDanger(jbEliminarMensaje);
        styleButtonDanger(jbEliminar);
        updateStatusIndicator(jOnline != null ? jOnline.getText() : "");
        for (JComponent entry : messageContainersById.values()) {
            if (entry.getComponentCount() == 0) {
                continue;
            }
            Component child = entry.getComponent(0);
            if (child instanceof MessageRow) {
                MessageRow row = (MessageRow) child;
                row.setSelectedState(row == selectedMessageRow);
            }
        }
        repaint();
    }

    private void styleTextField(JTextField field) {
        if (field == null) {
            return;
        }
        field.setBackground(currentPanel);
        field.setForeground(currentTextPrimary);
        field.setCaretColor(currentTextPrimary);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(currentBorder),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
    }

    private Color blendColors(Color first, Color second, float ratioToFirst) {
        float ratio = Math.max(0f, Math.min(1f, ratioToFirst));
        float inverse = 1f - ratio;
        int red = Math.round(first.getRed() * ratio + second.getRed() * inverse);
        int green = Math.round(first.getGreen() * ratio + second.getGreen() * inverse);
        int blue = Math.round(first.getBlue() * ratio + second.getBlue() * inverse);
        return new Color(red, green, blue);
    }
}//172.16.41.214

