package edu.upb.chatupb_v2.VIews;

import edu.upb.chatupb_v2.Controller.*;
import edu.upb.chatupb_v2.Model.entities.*;
import edu.upb.chatupb_v2.Model.entities.comands.*;
import edu.upb.chatupb_v2.Model.entities.comands.Image;
import edu.upb.chatupb_v2.Model.entities.enums.*;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class JUi extends JFrame implements IChatView {

    // =========================================================================
    // CONSTANTES DE DISEÑO
    // =========================================================================

    private static final Color BG_DARK        = new Color(0xF0F2F5);
    private static final Color BG_SIDEBAR     = new Color(0xFFFFFF);
    private static final Color BG_CHAT        = new Color(0xF7F8FA);
    private static final Color BG_INPUT       = new Color(0xFFFFFF);
    private static final Color ACCENT         = new Color(0x5B5BD6);
    private static final Color ACCENT_HOVER   = new Color(0x4848C0);
    private static final Color TEXT_PRIMARY   = new Color(0x111118);
    private static final Color TEXT_SECONDARY = new Color(0x666680);
    private static final Color BORDER_COLOR   = new Color(0xDDDDE8);
    private static final Color ONLINE_GREEN   = new Color(0x16A34A);
    private static final Color DANGER         = new Color(0xDC2626);

    private static final Font FONT_TITLE = new Font("SF Pro Display", Font.BOLD,  15);
    private static final Font FONT_BODY  = new Font("SF Pro Text",    Font.PLAIN, 13);
    private static final Font FONT_SMALL = new Font("SF Pro Text",    Font.PLAIN, 11);

    // =========================================================================
    // ESTADO
    // =========================================================================

    @Setter @Getter private UIController UIController;

    private final String username;
    private static String userId;

    private DialogUtil dialogUtil = new DialogUtil();
    private User selectedUser;
    private ContactController controller;
    private ThemesUtil.ChatTheme activeTheme = ThemesUtil.DEFAULT_THEME;

    private final CobroController cobroController = new CobroController();
    private final java.util.Map<String, String> pinnedMessages = new java.util.HashMap<>();
    private String currentPinnedId = null;

    // Componentes de la UI
    private DefaultListModel<User> chatListModel;
    private JList<User> chatList;
    private JPanel messagesPanel;
    private JScrollPane scrollPane;
    private JPanel pinnedBar;
    private JLabel pinnedLabel;
    private JTextField jTextMensaje;
    private JLabel statusDot;
    private JLabel statusLabel;
    private JLabel chatTitle;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    public JUi() {
        Account account = ConnectionDialog.showAccountPicker(this);
        if (account == null) System.exit(0);

        this.username = account.getNombre();
        userId = account.getId();

        applyGlobalLAF();
        initComponents();
        this.UIController = new UIController(this, username, userId);
    }

    // =========================================================================
    // INICIALIZACIÓN DE LA UI
    // =========================================================================

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

    private void initComponents() {
        setTitle("ChatUPB");
        setSize(1000, 680);
        setMinimumSize(new Dimension(800, 550));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(BG_DARK);

        JPanel sidebar    = buildSidebar();
        JPanel rightPanel = buildRightPanel();

        setLayout(new BorderLayout());
        add(sidebar,    BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

        sidebar.add(buildSidebarTop(),    BorderLayout.NORTH);
        sidebar.add(buildContactList(),   BorderLayout.CENTER);
        sidebar.add(buildSidebarBottom(), BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel buildSidebarTop() {
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

        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(BG_SIDEBAR);
        header.setBorder(new EmptyBorder(18, 16, 14, 16));
        header.add(avatar,    BorderLayout.WEST);
        header.add(nameStack, BorderLayout.CENTER);

        JLabel contactsLabel = new JLabel("CONTACTOS");
        contactsLabel.setFont(new Font("SF Pro Text", Font.BOLD, 10));
        contactsLabel.setForeground(TEXT_SECONDARY);
        contactsLabel.setBorder(new EmptyBorder(8, 16, 4, 16));

        JPanel sideTop = new JPanel(new BorderLayout());
        sideTop.setBackground(BG_SIDEBAR);
        sideTop.add(header,        BorderLayout.NORTH);
        sideTop.add(contactsLabel, BorderLayout.SOUTH);
        return sideTop;
    }

    private JScrollPane buildContactList() {
        chatListModel = new DefaultListModel<>();
        chatList = new JList<>(chatListModel);
        chatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chatList.setFixedCellHeight(58);
        chatList.setBackground(BG_SIDEBAR);
        chatList.setForeground(TEXT_PRIMARY);
        chatList.setCellRenderer(new ContactCellRenderer());
        chatList.setBorder(null);

        chatList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onContactSelected(chatList.getSelectedValue());
        });

        JPopupMenu popup = buildContactContextMenu();
        chatList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    chatList.setSelectedIndex(chatList.locationToIndex(e.getPoint()));
                    popup.show(chatList, e.getX(), e.getY());
                }
            }
        });

        JScrollPane scroll = new JScrollPane(chatList);
        scroll.setBorder(null);
        scroll.setBackground(BG_SIDEBAR);
        scroll.getViewport().setBackground(BG_SIDEBAR);
        styleScrollBar(scroll);
        return scroll;
    }

    private JPanel buildSidebarBottom() {
        JButton btnNewConn = createSidebarButton();
        btnNewConn.addActionListener(e -> dialogUtil.showConnectDialog(this, UIController));

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(BG_SIDEBAR);
        bottom.setBorder(new EmptyBorder(10, 12, 14, 12));
        bottom.add(btnNewConn, BorderLayout.CENTER);
        return bottom;
    }

    private JPopupMenu buildContactContextMenu() {
        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(BG_INPUT);
        popup.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        JMenuItem itemConectar = styledMenuItem("Conectar",          ACCENT);
        JMenuItem itemEliminar = styledMenuItem("Eliminar Contacto", TEXT_SECONDARY);

        itemConectar.addActionListener(e -> UIController.connectPrev(chatList.getSelectedValue()));
        itemEliminar.addActionListener(e -> {
            UIController.deleteUser(chatList.getSelectedValue());
            repaint();
        });

        popup.add(itemConectar);
        popup.add(itemEliminar);
        return popup;
    }

    // ── Panel derecho ─────────────────────────────────────────────────────────

    private JPanel buildRightPanel() {
        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(BG_CHAT);
        right.add(buildTopBar(),   BorderLayout.NORTH);
        right.add(buildChatBody(), BorderLayout.CENTER);
        right.add(buildInputBar(), BorderLayout.SOUTH);
        return right;
    }

    private JPanel buildTopBar() {
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

        JPanel topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setBackground(BG_CHAT);
        topBar.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(12, 18, 12, 14)));
        topBar.add(chatInfo,        BorderLayout.CENTER);
        topBar.add(buildActionRow(), BorderLayout.EAST);
        return topBar;
    }

    private JPanel buildActionRow() {
        JButton btnBuzz    = createIconButton("⚡", "Buzz",           new Color(0xFBBF24));
        JButton btnContact = createIconButton("👤", "Send Contact",   ACCENT);
        JButton btnTheme   = createIconButton("🎨", "Tema",           new Color(0xA855F7));
        JButton btnPay     = createIconButton("💳", "Pagar",          ONLINE_GREEN);
        JButton btnBye     = createIconButton("⏻",  "Fuera de Línea", DANGER);

        btnBuzz   .addActionListener(e -> UIController.sendBuzz());
        btnContact.addActionListener(e -> {
            try { UIController.sendContact(chatList.getSelectedValue()); }
            catch (IOException ex) { throw new RuntimeException(ex); }
        });
        btnTheme.addActionListener(e -> dialogUtil.showThemeDialog(
                this,
                activeTheme,
                FONT_TITLE,
                FONT_BODY,
                t -> {
                    applyTheme(t);
                    if (selectedUser != null) UIController.sendTheme(t.id(), selectedUser);
                }
        ));        btnPay  .addActionListener(e -> dialogUtil.showPaymentDialog(this, cobroController));
        btnBye  .addActionListener(e -> UIController.sendBye());

        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        row.setOpaque(false);
        row.add(btnBuzz);
        row.add(btnContact);
        row.add(btnTheme);
        row.add(btnPay);
        row.add(btnBye);
        return row;
    }

    private JPanel buildChatBody() {
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

        JPanel chatBody = new JPanel(new BorderLayout());
        chatBody.setBackground(BG_CHAT);
        chatBody.add(buildPinnedBar(), BorderLayout.NORTH);
        chatBody.add(scrollPane,       BorderLayout.CENTER);
        return chatBody;
    }

    private JPanel buildPinnedBar() {
        JLabel pinIcon = new JLabel("📌");
        pinIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));

        pinnedLabel = new JLabel("");
        pinnedLabel.setFont(FONT_BODY);
        pinnedLabel.setForeground(ACCENT);

        JButton btnUnpin = new JButton("✕");
        btnUnpin.setFont(FONT_SMALL);
        btnUnpin.setForeground(TEXT_SECONDARY);
        btnUnpin.setBorderPainted(false);
        btnUnpin.setContentAreaFilled(false);
        btnUnpin.setFocusPainted(false);
        btnUnpin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnUnpin.addActionListener(e -> pinnedBar.setVisible(false));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.setOpaque(false);
        left.add(pinIcon);
        left.add(pinnedLabel);

        pinnedBar = new JPanel(new BorderLayout(10, 0));
        pinnedBar.setBackground(new Color(0xEEEEFF));
        pinnedBar.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(7, 16, 7, 12)));
        pinnedBar.setVisible(false);
        pinnedBar.add(left,     BorderLayout.CENTER);
        pinnedBar.add(btnUnpin, BorderLayout.EAST);
        return pinnedBar;
    }

    private JPanel buildInputBar() {
        jTextMensaje = new JTextField();
        jTextMensaje.setFont(FONT_BODY);
        jTextMensaje.setBackground(BG_INPUT);
        jTextMensaje.setForeground(TEXT_PRIMARY);
        jTextMensaje.setCaretColor(ACCENT);
        jTextMensaje.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(10, 14, 10, 14)));

        JButton btnImage  = buildImageButton();
        JButton btnSend   = createStyledButton("Enviar →");
        JButton btnUnique = createStyledButton("Unique");

        btnSend  .addActionListener(e -> sendCurrentMessage());
        btnUnique.addActionListener(e -> sendCurrentMessageUnique());

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightButtons.setOpaque(false);
        rightButtons.add(btnUnique);
        rightButtons.add(btnSend);

        JPanel inputBar = new JPanel(new BorderLayout(10, 0));
        inputBar.setBackground(BG_SIDEBAR);
        inputBar.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(12, 16, 14, 16)));
        inputBar.add(btnImage,     BorderLayout.WEST);
        inputBar.add(jTextMensaje, BorderLayout.CENTER);
        inputBar.add(rightButtons, BorderLayout.EAST);
        return inputBar;
    }

    private JButton buildImageButton() {
        JButton btn = createStyledButton("Imagen");
        btn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                addImage(new ImageIcon(file.getPath()), true, true);
                UIController.sendImage(file, chatList.getSelectedValue());
            }
        });
        return btn;
    }

    // =========================================================================
    // FÁBRICA DE COMPONENTES
    // =========================================================================

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text) {
            { setFont(new Font("SF Pro Text", Font.BOLD, 13));
                setForeground(Color.WHITE);
                setBackground(ACCENT);
                setBorder(new EmptyBorder(10, 20, 10, 20));
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setOpaque(true); }
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
        btn.setContentAreaFilled(false);
        return btn;
    }

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

    private JButton createSidebarButton() {
        JButton btn = new JButton("＋  Nueva Conexión") {
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

    private JMenuItem styledMenuItem(String text, Color fg) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(FONT_BODY);
        item.setForeground(fg);
        item.setBackground(BG_INPUT);
        item.setBorder(new EmptyBorder(8, 14, 8, 14));
        item.setOpaque(true);
        return item;
    }

    private void styleScrollBar(JScrollPane sp) {
        JScrollBar vsb = sp.getVerticalScrollBar();
        vsb.setBackground(BG_CHAT);
        vsb.setForeground(new Color(0x3A3A50));
        vsb.setPreferredSize(new Dimension(6, 0));
    }

    // =========================================================================
    // LÓGICA DE MENSAJES
    // =========================================================================

    private void sendCurrentMessage() {
        String text = jTextMensaje.getText().trim();
        if (!text.isEmpty()) {
            String id = UUID.randomUUID().toString();
            addMessage(text, true, id, true);
            UIController.sendMessage(text, selectedUser, id);
            jTextMensaje.setText("");
        }
    }

    private void sendCurrentMessageUnique() {
        String text = jTextMensaje.getText().trim();
        if (!text.isEmpty()) {
            UIController.sendMessageUnique(text, selectedUser, UUID.randomUUID().toString());
            jTextMensaje.setText("");
        }
    }

    @Override
    public void addMessage(String text, boolean isOwn, String idMessage, boolean isRead) {
        JPanel row = createMessageRow(isOwn);
        MessageBubble bubble = new MessageBubble(text, isOwn, isRead);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, bubble.getPreferredSize().height + 8));
        row.add(bubble, isOwn ? BorderLayout.EAST : BorderLayout.WEST);
        attachBubblePopup(bubble, text, idMessage);
        appendToMessagesPanel(row);
    }

    private void addImage(ImageIcon icon, boolean isOwn, boolean isRead) {
        JPanel row = createMessageRow(isOwn);
        MessageBubble bubble = new MessageBubble(null, icon, isOwn, isRead);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, bubble.getPreferredSize().height + 8));
        row.add(bubble, isOwn ? BorderLayout.EAST : BorderLayout.WEST);
        appendToMessagesPanel(row);
    }

    private JPanel createMessageRow(boolean isOwn) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(4, 4, 4, 4));
        return row;
    }

    private void appendToMessagesPanel(JPanel row) {
        messagesPanel.add(row);
        messagesPanel.add(Box.createVerticalStrut(6));
        messagesPanel.revalidate();
        messagesPanel.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar v = scrollPane.getVerticalScrollBar();
            v.setValue(v.getMaximum());
        });
    }

    private void attachBubblePopup(MessageBubble bubble, String text, String idMessage) {
        if (text == null) return;
        bubble.putClientProperty("msgText", text);

        JMenuItem pinItem    = styledMenuItem("📌  Fijar mensaje", ACCENT);
        JMenuItem deleteItem = styledMenuItem("Borrar Mensaje",    ACCENT);

        pinItem.addActionListener(e -> {
            currentPinnedId = idMessage;
            displayPinnedBanner(text);
            if (selectedUser != null) UIController.sendPinMessage(idMessage, selectedUser);
        });
        deleteItem.addActionListener(e -> {
            if (selectedUser != null) UIController.deleteMessage(idMessage);
            repaint();
        });

        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(BG_SIDEBAR);
        menu.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        menu.add(pinItem);
        menu.add(deleteItem);

        bubble.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e)  { maybeShow(e); }
            @Override public void mouseReleased(MouseEvent e) { maybeShow(e); }
            private void maybeShow(MouseEvent e) {
                if (e.isPopupTrigger()) menu.show(bubble, e.getX(), e.getY());
            }
        });
    }

    private void displayPinnedBanner(String text) {
        String preview = text.length() > 60 ? text.substring(0, 57) + "…" : text;
        pinnedLabel.setText(preview);
        pinnedBar.setVisible(true);
        pinnedBar.revalidate();
        pinnedBar.repaint();
    }

    // =========================================================================
    // GESTIÓN DE CONTACTOS / CONVERSACIÓN
    // =========================================================================

    public void setController(ContactController controller) {
        this.controller = controller;
        renderContacts();
    }

    private void onContactSelected(User sel) {
        if (sel == null) return;
        selectedUser = sel;
        chatTitle.setText(sel.getName() != null ? sel.getName() : sel.toString());
        pinnedBar.setVisible(false);
        currentPinnedId = null;
        renderMessages(controller.returnMessages(userId, selectedUser.getId()));
    }

    private void selectUser(String targetUserId) {
        for (int i = 0; i < chatListModel.size(); i++) {
            if (chatListModel.get(i).getId().equals(targetUserId)) {
                chatList.setSelectedIndex(i);
                break;
            }
        }
    }
    private void applyTheme(ThemesUtil.ChatTheme t) {
        ThemesUtil.applyTheme(t, activeTheme, getContentPane(), messagesPanel, scrollPane);
        activeTheme = t;
    }

    // =========================================================================
    // IMPLEMENTACIÓN IChatView
    // =========================================================================

    public void init() {
        EventQueue.invokeLater(() -> setVisible(true));
    }

    @Override
    public void updateStatus(String status) {
        statusLabel.setText(status);
        statusDot.setForeground(status.toLowerCase().contains("offline") ? DANGER : ONLINE_GREEN);
    }

    @Override
    public void showMessage(String message) {
        addMessage(message, false, UUID.randomUUID().toString(), true);
    }

    @Override
    public void showError(String error) {
        JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public boolean showInvitationDialog(String userName, String id) {
        int res = JOptionPane.showConfirmDialog(
                this, "Invitación de: " + userName + "\n¿Aceptar?",
                "Nueva conexión", JOptionPane.YES_NO_OPTION);
        return res == JOptionPane.YES_OPTION;
    }

    @Override
    public void showBuzzNotification(String senderName) {
        vibrateWindow();
    }

    private void vibrateWindow() {
        Point origin     = getLocation();
        int   amplitude  = 8;
        int   cycles     = 6;
        int   periodMs   = 40;

        int[] offsets = new int[cycles * 2 + 1];
        for (int i = 0; i < cycles; i++) {
            offsets[i * 2]     =  amplitude;
            offsets[i * 2 + 1] = -amplitude;
        }
        offsets[cycles * 2] = 0;

        Timer timer = new Timer(periodMs, null);
        int[] step  = {0};
        timer.addActionListener(e -> {
            if (step[0] < offsets.length) {
                setLocation(origin.x + offsets[step[0]], origin.y);
                step[0]++;
            } else {
                timer.stop();
                setLocation(origin);
            }
        });
        timer.start();
    }

    @Override
    public void showChat(Chat chat) {
        addMessage(chat.getMessage(), false, chat.getIdMessage(), true);
    }

    @Override
    public void showUniqueMessage(UniqueMessage uni) {
        addMessage(uni.getMessage(), false, uni.getIdMessage(), true);
    }

    @Override
    public void renderContacts() {
        chatListModel.clear();
        try {
            for (User u : controller.returnContacts(userId)) chatListModel.addElement(u);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void renderMessages(List<Message> messages) {
        messagesPanel.removeAll();
        for (Message m : messages) {
            boolean isMine = m.getSendUser().equals(userId);
            if (m.getTypeMessage() == TypeMessage.IMAGE) {
                try {
                    byte[] bytes = Base64.getDecoder().decode(m.getBody());
                    addImage(new ImageIcon(bytes), isMine, m.getStatusMessage().equals(StatusMessage.READ));
                } catch (Exception ignored) {}
            } else {
                addMessage(m.getBody(), isMine, m.getIdMessage(), m.getStatusMessage().equals(StatusMessage.READ));
            }
        }
        messagesPanel.revalidate();
        messagesPanel.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar v = scrollPane.getVerticalScrollBar();
            v.setValue(v.getMaximum());
        });
    }

    @Override
    public void showByeNotification(String id) {
        JOptionPane.showMessageDialog(this,
                id + " se desconectó", "Desconectado", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void onNewConnectionEstablished(String userName, String userId) {
        renderContacts();
        updateStatus("Status: Online");
        selectUser(userId);
    }

    @Override
    public void showPinnedMessage(String messageId) {
        String message = ClientController.getInstance().retrieveMessage(messageId);
        if (message == null || message.isEmpty()) message = "Mensaje Pineado";
        currentPinnedId = messageId;
        displayPinnedBanner(message);
    }

    @Override
    public void changeThemeSelected(Theme theme) {
        SwingUtilities.invokeLater(() ->
                ThemesUtil.findById(theme.getIdTheme()).ifPresent(this::applyTheme));
    }

    @Override
    public void showImage(Image image) {
        try {
            byte[] bytes = Base64.getDecoder().decode(image.getMessage());
            addImage(new ImageIcon(bytes), image.getSendUser().equals(userId), true);
        } catch (Exception ignored) {}
    }

    // =========================================================================
    // RENDERIZADOR DE CELDA DE CONTACTO
    // =========================================================================

    private static class ContactCellRenderer extends JPanel implements ListCellRenderer<User> {

        private final JLabel avatarLabel = new JLabel();
        private final JLabel nameLabel   = new JLabel();
        private final JLabel subLabel    = new JLabel();

        private static final Color[] AVATAR_PALETTE = {
                new Color(0x5B5BD6), new Color(0xE5484D), new Color(0x30A46C),
                new Color(0xF76B15), new Color(0x8E4EC6), new Color(0x0091FF)
        };

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

            JLabel dotLabel = new JLabel("●");
            dotLabel.setFont(FONT_SMALL);
            dotLabel.setForeground(Color.RED);

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
                JList<? extends User> list, User user, int index,
                boolean isSelected, boolean focused) {

            String name     = user.getName() != null ? user.getName() : user.toString();
            String initials = name.isEmpty() ? "?" : String.valueOf(name.charAt(0)).toUpperCase();

            avatarLabel.setText(initials);
            avatarLabel.setBackground(AVATAR_PALETTE[Math.abs(name.hashCode()) % AVATAR_PALETTE.length]);
            nameLabel.setText(name);
            subLabel.setText(user.getId() != null ? user.getId() : "");

            setBackground(isSelected ? new Color(0x5B5BD6, true).darker() : BG_SIDEBAR);
            nameLabel.setForeground(isSelected ? Color.WHITE            : TEXT_PRIMARY);
            subLabel .setForeground(isSelected ? new Color(0xCCCCDD)    : TEXT_SECONDARY);
            setOpaque(true);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }
}