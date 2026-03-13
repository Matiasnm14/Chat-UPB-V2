package edu.upb.chatupb_v2.VIews;

import edu.upb.chatupb_v2.Controller.*;
import edu.upb.chatupb_v2.Model.entities.*;
import edu.upb.chatupb_v2.Model.entities.comands.*;
import edu.upb.chatupb_v2.Model.entities.comands.Image;
import edu.upb.chatupb_v2.Model.entities.enums.TypeMessage;
import edu.upb.chatupb_v2.Model.network.SocketClient;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;

public class JUi extends JFrame implements IChatView {

    // ── Palette (Light Mode) ─────────────────────────────────
    private static final Color BG_DARK        = new Color(0xF0F2F5);
    private static final Color BG_SIDEBAR     = new Color(0xFFFFFF);
    private static final Color BG_CHAT        = new Color(0xF7F8FA);
    private static final Color BG_INPUT       = new Color(0xFFFFFF);
    private static final Color BG_BUBBLE_OWN  = new Color(0x5B5BD6);
    private static final Color BG_BUBBLE_THEM = new Color(0xE8E9F0);
    private static final Color ACCENT         = new Color(0x5B5BD6);
    private static final Color ACCENT_HOVER   = new Color(0x4848C0);
    private static final Color TEXT_PRIMARY   = new Color(0x111118);
    private static final Color TEXT_SECONDARY = new Color(0x666680);
    private static final Color BORDER_COLOR   = new Color(0xDDDDE8);
    private static final Color ONLINE_GREEN   = new Color(0x16A34A);
    private static final Color DANGER         = new Color(0xDC2626);
    private static final Color GOLD           = new Color(0xD97706);

    // ── Fonts ─────────────────────────────────────────────────
    private static final Font FONT_TITLE  = new Font("SF Pro Display", Font.BOLD, 15);
    private static final Font FONT_BODY   = new Font("SF Pro Text",    Font.PLAIN, 13);
    private static final Font FONT_SMALL  = new Font("SF Pro Text",    Font.PLAIN, 11);
    private static final Font FONT_MONO   = new Font("JetBrains Mono", Font.PLAIN, 12);

    // ── State ─────────────────────────────────────────────────
    @Setter @Getter private UIController UIController;
    private String username;
    private static String userId;
    private User selectedUser;
    private DefaultListModel<User> chatListModel;
    private JList<User> chatList;
    private ContactController controller;
    private JPanel messagesPanel;
    private JScrollPane scrollPane;
    private CobroController cobroController = new CobroController();
    private JTextField jTextMensaje;
    private JLabel statusDot;
    private JLabel statusLabel;
    private JLabel chatTitle;

    public void setController(ContactController controller) {
        this.controller = controller;
        renderContacts();
    }

    public JUi() {
        Account account = ConnectionDialog.showAccountPicker(this);
        if (account == null) System.exit(0);
        this.username = account.getNombre();
        this.userId   = account.getId();
        applyGlobalLAF();
        initComponents();
        this.UIController = new UIController(this, username, userId);
    }

    // ── Global look-and-feel tweaks ───────────────────────────
    private void applyGlobalLAF() {
        UIManager.put("Panel.background",          BG_DARK);
        UIManager.put("Label.foreground",          TEXT_PRIMARY);
        UIManager.put("TextField.background",      BG_INPUT);
        UIManager.put("TextField.foreground",      TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", ACCENT);
        UIManager.put("ScrollBar.thumb",           new Color(0xCCCCDD));
        UIManager.put("ScrollBar.track",           BG_CHAT);
        UIManager.put("ScrollBar.width",           6);
    }

    // ── Build UI ──────────────────────────────────────────────
    private void initComponents() {
        setTitle("ChatUPB");
        setSize(1000, 680);
        setMinimumSize(new Dimension(800, 550));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(BG_DARK);

        // ── Sidebar ───────────────────────────────────────────
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

        // Sidebar header
        JPanel sideHeader = new JPanel(new BorderLayout());
        sideHeader.setBackground(BG_SIDEBAR);
        sideHeader.setBorder(new EmptyBorder(18, 16, 14, 16));

        JLabel appName = new JLabel("ChatUPB");
        appName.setFont(new Font("SF Pro Display", Font.BOLD, 20));
        appName.setForeground(TEXT_PRIMARY);

        JLabel userTag = new JLabel("@" + username);
        userTag.setFont(FONT_SMALL);
        userTag.setForeground(TEXT_SECONDARY);

        JPanel nameStack = new JPanel();
        nameStack.setLayout(new BoxLayout(nameStack, BoxLayout.Y_AXIS));
        nameStack.setOpaque(false);
        nameStack.add(appName);
        nameStack.add(Box.createVerticalStrut(2));
        nameStack.add(userTag);

        // Avatar circle
        JLabel avatar = new JLabel(String.valueOf(username.charAt(0)).toUpperCase()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setFont(new Font("SF Pro Display", Font.BOLD, 16));
        avatar.setForeground(Color.WHITE);
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(38, 38));

        sideHeader.add(avatar,    BorderLayout.WEST);
        sideHeader.add(Box.createHorizontalStrut(10), BorderLayout.CENTER);
        sideHeader.add(nameStack, BorderLayout.EAST);
        // Actually use a proper layout:
        sideHeader.removeAll();
        sideHeader.setLayout(new BorderLayout(10, 0));
        sideHeader.add(avatar,    BorderLayout.WEST);
        sideHeader.add(nameStack, BorderLayout.CENTER);

        // Section label
        JLabel contactsLabel = new JLabel("CONTACTOS");
        contactsLabel.setFont(new Font("SF Pro Text", Font.BOLD, 10));
        contactsLabel.setForeground(TEXT_SECONDARY);
        contactsLabel.setBorder(new EmptyBorder(8, 16, 4, 16));

        // Chat list
        chatListModel = new DefaultListModel<>();
        chatList = new JList<>(chatListModel);
        chatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chatList.setFixedCellHeight(58);
        chatList.setBackground(BG_SIDEBAR);
        chatList.setForeground(TEXT_PRIMARY);
        chatList.setCellRenderer(new ContactCellRenderer());
        chatList.setBorder(null);

        JScrollPane leftScroll = new JScrollPane(chatList);
        leftScroll.setBorder(null);
        leftScroll.setBackground(BG_SIDEBAR);
        leftScroll.getViewport().setBackground(BG_SIDEBAR);
        styleScrollBar(leftScroll);

        // New connection button
        JButton btnNewConn = createSidebarButton("＋  Nueva Conexión", ACCENT);

        JPanel sideBottom = new JPanel(new BorderLayout());
        sideBottom.setBackground(BG_SIDEBAR);
        sideBottom.setBorder(new EmptyBorder(10, 12, 14, 12));
        sideBottom.add(btnNewConn, BorderLayout.CENTER);

        sidebar.add(sideHeader,    BorderLayout.NORTH);
        sidebar.add(contactsLabel, BorderLayout.NORTH); // overwritten — fix below
        sidebar.setLayout(new BorderLayout());
        JPanel sideTop = new JPanel(new BorderLayout());
        sideTop.setBackground(BG_SIDEBAR);
        sideTop.add(sideHeader,    BorderLayout.NORTH);
        sideTop.add(contactsLabel, BorderLayout.SOUTH);
        sidebar.add(sideTop,       BorderLayout.NORTH);
        sidebar.add(leftScroll,    BorderLayout.CENTER);
        sidebar.add(sideBottom,    BorderLayout.SOUTH);

        // ── Chat Area ─────────────────────────────────────────
        // Top bar
        JPanel topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setBackground(BG_CHAT);
        topBar.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(12, 18, 12, 14)
        ));

        chatTitle = new JLabel("Selecciona un contacto");
        chatTitle.setFont(new Font("SF Pro Display", Font.BOLD, 16));
        chatTitle.setForeground(TEXT_PRIMARY);

        statusDot = new JLabel("●");
        statusDot.setForeground(ONLINE_GREEN);
        statusDot.setFont(FONT_SMALL);

        statusLabel = new JLabel("Online");
        statusLabel.setFont(FONT_SMALL);
        statusLabel.setForeground(TEXT_SECONDARY);

        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        statusRow.setOpaque(false);
        statusRow.add(statusDot);
        statusRow.add(statusLabel);

        JPanel chatInfo = new JPanel(new BorderLayout(0, 2));
        chatInfo.setOpaque(false);
        chatInfo.add(chatTitle, BorderLayout.NORTH);
        chatInfo.add(statusRow, BorderLayout.SOUTH);

        // Action buttons
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionRow.setOpaque(false);
        actionRow.add(createIconButton("⚡", "Buzz",           new Color(0xFBBF24)));
        actionRow.add(createIconButton("👤", "Send Contact",   ACCENT));
        actionRow.add(createIconButton("💳", "Pagar",          ONLINE_GREEN));
        actionRow.add(createIconButton("⏻",  "Fuera de Línea", DANGER));

        topBar.add(chatInfo,  BorderLayout.CENTER);
        topBar.add(actionRow, BorderLayout.EAST);

        // Messages area
        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(BG_CHAT);
        messagesPanel.setBorder(new EmptyBorder(16, 16, 8, 16));

        scrollPane = new JScrollPane(messagesPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(BG_CHAT);
        scrollPane.getViewport().setBackground(BG_CHAT);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        styleScrollBar(scrollPane);

        // Input bar
        JPanel inputBar = new JPanel(new BorderLayout(10, 0));
        inputBar.setBackground(BG_SIDEBAR);
        inputBar.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(12, 16, 14, 16)
        ));

        jTextMensaje = new JTextField();
        jTextMensaje.setFont(FONT_BODY);
        jTextMensaje.setBackground(BG_INPUT);
        jTextMensaje.setForeground(TEXT_PRIMARY);
        jTextMensaje.setCaretColor(ACCENT);
        jTextMensaje.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));

        JButton btnImage = new JButton("Imagen") {
            {
                setFont(new Font("SF Pro Text", Font.BOLD, 13));
                setForeground(Color.WHITE);
                setBackground(ACCENT);
                setBorder(new EmptyBorder(10, 20, 10, 20));
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setOpaque(true);
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ACCENT_HOVER : ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnImage.setContentAreaFilled(false);

        btnImage.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(null);

            if(result == JFileChooser.APPROVE_OPTION){
                File file = fileChooser.getSelectedFile();
                ImageIcon icon = new ImageIcon(file.getPath());
                addImage(icon, true);
                UIController.sendImage(file,chatList.getSelectedValue());
            }
        });

        JButton btnSend = new JButton("Enviar →") {
            {
                setFont(new Font("SF Pro Text", Font.BOLD, 13));
                setForeground(Color.WHITE);
                setBackground(ACCENT);
                setBorder(new EmptyBorder(10, 20, 10, 20));
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setOpaque(true);
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ACCENT_HOVER : ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnSend.setContentAreaFilled(false);

        inputBar.add(jTextMensaje, BorderLayout.CENTER);
        inputBar.add(btnSend,      BorderLayout.EAST);
        inputBar.add(btnImage, BorderLayout.WEST);

        // Assemble right panel
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(BG_CHAT);
        rightPanel.add(topBar,    BorderLayout.NORTH);
        rightPanel.add(scrollPane, BorderLayout.CENTER);
        rightPanel.add(inputBar,  BorderLayout.SOUTH);

        // ── Root layout ───────────────────────────────────────
        setLayout(new BorderLayout());
        add(sidebar,    BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        // ── Wire actions ──────────────────────────────────────
        btnSend.addActionListener(e -> sendCurrentMessage());
        jTextMensaje.addActionListener(e -> sendCurrentMessage());

        // Wire icon buttons (stored in actionRow)
        Component[] btns = actionRow.getComponents();
        // ⚡ Buzz
        ((JButton) btns[0]).addActionListener(e -> UIController.sendBuzz());
        // 👤 Send Contact
        ((JButton) btns[1]).addActionListener(e -> {
            try { UIController.sendContact(chatList.getSelectedValue()); }
            catch (IOException ex) { throw new RuntimeException(ex); }
        });
        // 💳 Pagar
        ((JButton) btns[2]).addActionListener(e -> showPaymentDialog());
        // ⏻ Offline
        ((JButton) btns[3]).addActionListener(e -> UIController.sendBye());

        btnNewConn.addActionListener(e -> showConnectDialog());

        chatList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                User sel = chatList.getSelectedValue();
                if (sel != null) {
                    selectedUser = sel;
                    chatTitle.setText(sel.getName() != null ? sel.getName() : sel.toString());
                    renderMessages(controller.returnMessages(this.userId, selectedUser.getId()));
                }
            }
        });

        // Right-click context menu
        JPopupMenu popup = buildContextMenu();
        chatList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int idx = chatList.locationToIndex(e.getPoint());
                    chatList.setSelectedIndex(idx);
                    popup.show(chatList, e.getX(), e.getY());
                }
            }
        });
    }

    // ── Helper builders ───────────────────────────────────────

    private JButton createIconButton(String icon, String tooltip, Color accent) {
        JButton btn = new JButton(icon) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover()
                        ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 50)
                        : new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 20);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        btn.setForeground(accent);
        btn.setToolTipText(tooltip);
        btn.setPreferredSize(new Dimension(38, 34));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createSidebarButton(String text, Color accent) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ACCENT_HOVER : ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SF Pro Text", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(0, 38));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleScrollBar(JScrollPane sp) {
        JScrollBar vsb = sp.getVerticalScrollBar();
        vsb.setBackground(BG_CHAT);
        vsb.setForeground(new Color(0x3A3A50));
        vsb.setPreferredSize(new Dimension(6, 0));
    }

    private JPopupMenu buildContextMenu() {
        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(BG_INPUT);
        popup.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        JMenuItem j1 = styledMenuItem("Conectar", ACCENT);
        JMenuItem j2 = styledMenuItem("Eliminar Contacto",  TEXT_SECONDARY);
        popup.add(j1);
        popup.add(j2);

        j1.addActionListener(e -> UIController.connectPrev(chatList.getSelectedValue()));
        j2.addActionListener(e ->
                UIController.deleteUser(chatList.getSelectedValue()));
        return popup;
    }

    private JMenuItem styledMenuItem(String text, Color fg) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(FONT_BODY);
        item.setForeground(fg);
        item.setBackground(BG_INPUT);
        item.setBorder(new EmptyBorder(8, 14, 8, 14));
        item.setOpaque(true);
        return item;
    }

    // ── Actions ───────────────────────────────────────────────

    private void sendCurrentMessage() {
        String text = jTextMensaje.getText().trim();
        if (!text.isEmpty()) {
            addMessage(text, true);
            UIController.sendMessage(text, selectedUser);
            jTextMensaje.setText("");
        }
    }

    private void showPaymentDialog() {
        String[] options = {"🔐  Cripto", "🏦  Bob (Fiat)"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "¿Tipo de pago?",
                "Nuevo Pago",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]
        );
        if (choice < 0) return;
        Cobro res = cobroController.cobrarController(choice);
        String tipo = res.getRed().isEmpty() ? "FIAT" : "CRIPTO";
        String red  = res.getRed().isEmpty() ? "" : "\nRed: " + res.getRed();
        JOptionPane.showMessageDialog(this,
                "Tipo: " + tipo + "\nImporte: " + res.getImporte() + "\nQR: " + res.getQr() + red,
                "Pago", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showConnectDialog() {
        String ip = JOptionPane.showInputDialog(
                this, "Dirección IP del servidor:", "Nueva Conexión", JOptionPane.QUESTION_MESSAGE);
        if (ip != null && !ip.trim().isEmpty()) UIController.connect(ip.trim());
    }

    // ── Message rendering ─────────────────────────────────────

    private void addMessage(String text, boolean isOwn) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(4, 4, 4, 4));

        MessageBubble bubble = new MessageBubble(text, isOwn);

        if (isOwn) {
            row.add(bubble, BorderLayout.EAST);
        } else {
            row.add(bubble, BorderLayout.WEST);
        }

        messagesPanel.add(row);
        messagesPanel.add(Box.createVerticalStrut(6));

        messagesPanel.revalidate();
        messagesPanel.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar v = scrollPane.getVerticalScrollBar();
            v.setValue(v.getMaximum());
        });
    }

    private void addImage(ImageIcon icon, boolean isOwn) {

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(4, 4, 4, 4));

        MessageBubble bubble = new MessageBubble(null, icon, isOwn);

        if (isOwn) {
            row.add(bubble, BorderLayout.EAST);
        } else {
            row.add(bubble, BorderLayout.WEST);
        }

        messagesPanel.add(row);
        messagesPanel.add(Box.createVerticalStrut(8));

        messagesPanel.revalidate();
        messagesPanel.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar v = scrollPane.getVerticalScrollBar();
            v.setValue(v.getMaximum());
        });
    }

    // Nuevo método privado
    private void selectUser(String targetUserId) {
        for (int i = 0; i < chatListModel.size(); i++) {
            if (chatListModel.get(i).getId().equals(targetUserId)) {
                chatList.setSelectedIndex(i);
                // Dispara el ListSelectionListener que ya existe,
                // que carga los mensajes y setea selectedUser
                break;
            }
        }
    }

    public void init() {
        EventQueue.invokeLater(() -> setVisible(true));
    }

    // ── IChatView ─────────────────────────────────────────────

    @Override public void updateStatus(String status) {
        statusLabel.setText(status);
        statusDot.setForeground(status.toLowerCase().contains("offline") ? DANGER : ONLINE_GREEN);
    }

    @Override public void showMessage(String message)   { addMessage(message, false); }

    @Override public void showError(String error) {
        JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override public boolean showInvitationDialog(String userName, String id) {
        int res = JOptionPane.showConfirmDialog(
                this, "Invitación de: " + userName + "\n¿Aceptar?",
                "Nueva conexión", JOptionPane.YES_NO_OPTION);
        return res == JOptionPane.YES_OPTION;
    }

    @Override public void showBuzzNotification(String senderName) {
        JOptionPane.showMessageDialog(this,
                "⚡ " + senderName + " te envió un buzz", "Buzz", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override public void showChat(Chat chat) {
        addMessage(chat.getMessage(), false);
    }

    @Override public void renderContacts() {
        chatListModel.clear();
        try {
            for (User u : controller.returnContacts(userId)) chatListModel.addElement(u);
        } catch (Exception e) { System.out.println(e.getMessage()); }
    }

    @Override
    public void renderMessages(List<Message> messages) {
        messagesPanel.removeAll();

        for (Message m : messages) {
            messagesPanel.add(Box.createVerticalStrut(8)); // Un poco más de espacio entre burbujas
            boolean isMine = m.getSendUser().equals(userId);

            if (m.getTypeMessage() == TypeMessage.IMAGE) {
                try {
                    // 1. Decodificar el cuerpo del mensaje (Base64) a bytes
                    byte[] imageBytes = Base64.getDecoder().decode(m.getBody());

                    // 2. Crear el icono
                    ImageIcon icon = new ImageIcon(imageBytes);

                    // 3. Determinar si el mensaje es mío comparando IDs

                    // 4. Llamar al método addImage que creamos
                    addImage(icon, isMine);

                } catch (Exception e) {
                    // En caso de error (Base64 corrupto, etc.), podrías mostrar un mensaje de texto
                    addMessage("[Error al cargar imagen]", m.getSendUser().equals(userId));
                    e.printStackTrace();
                }
            } else {
                messagesPanel.add(new MessageBubble(m.getBody(), isMine));
            }
        }

        // Forzar actualización de layout
        messagesPanel.revalidate();
        messagesPanel.repaint();

        // Scroll al final
        SwingUtilities.invokeLater(() -> {
            JScrollBar v = scrollPane.getVerticalScrollBar();
            v.setValue(v.getMaximum());
        });
    }
    @Override public void showByeNotification(String id) {
        JOptionPane.showMessageDialog(this,
                id + " se desconectó", "Desconectado", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void onNewConnectionEstablished(String userName, String userId) {
        renderContacts();
        updateStatus("Status: Online");
        selectUser(userId); // ← selecciona automáticamente
    }

    @Override
    public void changeThemeSelected(Theme theme) {

    }

    @Override
    public void showImage(Image image){
        try {
            byte[] imageBytes = Base64.getDecoder().decode(image.getMessage());

            // 2. Crear el icono
            ImageIcon icon = new ImageIcon(imageBytes);

            // 3. Determinar si el mensaje es mío comparando IDs
            boolean isMine = image.getSendUser().equals(userId);

            // 4. Llamar al método addImage que creamos
            addImage(icon, isMine);

        } catch (Exception e) {
            // En caso de error (Base64 corrupto, etc.), podrías mostrar un mensaje de texto
            addMessage("[Error al cargar imagen]", image.getSendUser().equals(userId));
            e.printStackTrace();
        }
    }

    // ── Inner: Contact cell renderer ──────────────────────────

    private class ContactCellRenderer extends JPanel implements ListCellRenderer<User> {
        private final JLabel avatarLabel  = new JLabel();
        private final JLabel nameLabel    = new JLabel();
        private final JLabel subLabel     = new JLabel();
        private final JLabel dotLabel     = new JLabel("●");

        ContactCellRenderer() {
            setLayout(new BorderLayout(10, 0));
            setBorder(new EmptyBorder(8, 12, 8, 12));

            avatarLabel.setOpaque(true);
            avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
            avatarLabel.setFont(new Font("SF Pro Display", Font.BOLD, 15));
            avatarLabel.setForeground(Color.WHITE);
            avatarLabel.setPreferredSize(new Dimension(40, 40));

            nameLabel.setFont(new Font("SF Pro Text", Font.BOLD, 13));
            nameLabel.setForeground(TEXT_PRIMARY);

            subLabel.setFont(FONT_SMALL);
            subLabel.setForeground(TEXT_SECONDARY);

            dotLabel.setFont(FONT_SMALL);
            dotLabel.setForeground(ONLINE_GREEN);

            JPanel textCol = new JPanel(new BorderLayout(0, 2));
            textCol.setOpaque(false);
            textCol.add(nameLabel, BorderLayout.NORTH);
            textCol.add(subLabel,  BorderLayout.SOUTH);

            add(avatarLabel, BorderLayout.WEST);
            add(textCol,     BorderLayout.CENTER);
            add(dotLabel,    BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends User> list, User user, int index, boolean isSelected, boolean focused) {

            String name   = user.getName() != null ? user.getName() : user.toString();
            String initials = name.length() > 1
                    ? String.valueOf(name.charAt(0)).toUpperCase()
                    : name.toUpperCase();

            avatarLabel.setText(initials);
            nameLabel.setText(name);
            subLabel.setText(user.getId() != null ? user.getId() : "");

            // Pastel avatar color derived from name hash
            int hash  = name.hashCode();
            Color[] palette = {
                    new Color(0x5B5BD6), new Color(0xE5484D), new Color(0x30A46C),
                    new Color(0xF76B15), new Color(0x8E4EC6), new Color(0x0091FF)
            };
            avatarLabel.setBackground(palette[Math.abs(hash) % palette.length]);

            setBackground(isSelected ? new Color(0x5B5BD6, true).darker() : BG_SIDEBAR);
            nameLabel.setForeground(isSelected ? Color.WHITE : TEXT_PRIMARY);
            subLabel.setForeground(isSelected ? new Color(0xCCCCDD) : TEXT_SECONDARY);
            setOpaque(true);
            return this;
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
            // Rounded avatar
        }
    }

    // ── Inner: Message bubble ─────────────────────────────────

    static class MessageBubble extends JPanel {
        private final String text;
        private final boolean isOwn;
        private final ImageIcon image; // Nuevo: Soporte para imagen
        private static final int ARC = 16;
        private static final int MAX_WIDTH = 420;
        private static final int IMAGE_MAX_HEIGHT = 250; // Altura máxima para imágenes

        // Constructor para texto e imagen
        MessageBubble(String text, ImageIcon image, boolean isOwn) {
            this.text = text;
            this.isOwn = isOwn;
            this.image = image;
            setOpaque(false);
            // Permitir que el layout maneje el tamaño máximo correctamente
            setMaximumSize(new Dimension(MAX_WIDTH + 20, Integer.MAX_VALUE));        }

        // Sobrecarga para solo texto
        MessageBubble(String text, boolean isOwn) {
            this(text, null, isOwn);
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(new Font("SF Pro Text", Font.PLAIN, 13));
            int bubbleW = 0;
            int bubbleH = 0;

            // 1. Calcular dimensiones si hay imagen
            if (image != null) {
                double scale = Math.min((double) (MAX_WIDTH - 28) / image.getIconWidth(), 1.0);
                bubbleW = (int) (image.getIconWidth() * scale);
                bubbleH = (int) (image.getIconHeight() * scale);
                if (bubbleH > IMAGE_MAX_HEIGHT) {
                    scale = (double) IMAGE_MAX_HEIGHT / bubbleH;
                    bubbleW = (int) (bubbleW * scale);
                    bubbleH = IMAGE_MAX_HEIGHT;
                }
            }

            // 2. Calcular dimensiones del texto
            if (text != null && !text.isEmpty()) {
                int textW = fm.stringWidth(text);
                int wrappedTextW = Math.min(textW + 28, MAX_WIDTH);
                bubbleW = Math.max(bubbleW, wrappedTextW);

                // Estimación simple de líneas
                int lines = (int) Math.ceil((double)(textW + 28) / MAX_WIDTH);
                bubbleH += (fm.getHeight() * lines) + 20;
            } else {
                bubbleH += 20; // Padding si no hay texto
            }

            return new Dimension(bubbleW + 30, bubbleH + 10);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int w = getWidth();
            Dimension pref = getPreferredSize();
            int bw = Math.min(w - 10, pref.width - 10);
            int x = isOwn ? w - bw - 5 : 5;
            int h = getHeight() - 5;

            // Sombra
            g2.setColor(new Color(0, 0, 0, 20));
            g2.fillRoundRect(x + 1, 2, bw, h, ARC, ARC);

            // Fondo del Bubble
            Color bubbleColor = isOwn ? new Color(0x007AFF) : new Color(0xE9E9EB); // Colores tipo iOS
            g2.setColor(bubbleColor);
            g2.fillRoundRect(x, 0, bw, h, ARC, ARC);

            int currentY = 10;

            // Renderizar Imagen
            if (image != null) {
                double scale = Math.min((double) (bw - 20) / image.getIconWidth(), 1.0);
                int imgW = (int) (image.getIconWidth() * scale);
                int imgH = (int) (image.getIconHeight() * scale);

                // Dibujar imagen centrada en el bubble
                g2.setClip(new RoundRectangle2D.Float(x + 10, currentY, imgW, imgH, 10, 10));
                g2.drawImage(image.getImage(), x + 10, currentY, imgW, imgH, null);
                g2.setClip(null);

                currentY += imgH + 10;
            }

            // Renderizar Texto
            if (text != null && !text.isEmpty()) {
                g2.setFont(new Font("SF Pro Text", Font.PLAIN, 13));
                g2.setColor(isOwn ? Color.WHITE : Color.BLACK);
                FontMetrics fm = g2.getFontMetrics();

                String[] words = text.split(" ");
                StringBuilder line = new StringBuilder();
                int maxTextW = bw - 24;

                for (String word : words) {
                    String test = line.length() == 0 ? word : line + " " + word;
                    if (fm.stringWidth(test) > maxTextW) {
                        g2.drawString(line.toString(), x + 12, currentY + fm.getAscent());
                        currentY += fm.getHeight();
                        line = new StringBuilder(word);
                    } else {
                        line = new StringBuilder(test);
                    }
                }
                g2.drawString(line.toString(), x + 12, currentY + fm.getAscent());
            }

            g2.dispose();
        }
    }
}