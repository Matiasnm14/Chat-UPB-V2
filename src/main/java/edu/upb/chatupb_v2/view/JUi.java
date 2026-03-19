package edu.upb.chatupb_v2.view;
import edu.upb.chatupb_v2.controller.ContactController;
import edu.upb.chatupb_v2.controller.Controller;
import edu.upb.chatupb_v2.controller.MessageController;
import edu.upb.chatupb_v2.controller.UserController;
import edu.upb.chatupb_v2.model.entities.Contact;
import edu.upb.chatupb_v2.model.entities.Message;
import edu.upb.chatupb_v2.model.entities.User;
import edu.upb.chatupb_v2.model.entities.commands.ImageMesagge;
import edu.upb.chatupb_v2.model.entities.enums.Sound;
import edu.upb.chatupb_v2.model.entities.enums.TypeMessage;
import edu.upb.chatupb_v2.model.repository.UserDao;
import edu.upb.chatupb_v2.model.entities.commands.Chat;
import lombok.Getter;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
public class JUi extends JFrame implements IChatView {
    private Controller controller;
    private ContactController contactController;
    private MessageController messageController;
    private UserController userController;
    private String username;
    @Getter
    private String userId;
    private static final Logger logger = Logger.getLogger(JUi.class.getName());
    private DefaultListModel<Message> messageListModel;
    private JList<Message> messageList;
    private JTextField jTextMensaje;
    private JLabel jOnline;
    private DefaultListModel<Contact> chatListModel;
    private JList<Contact> chatList;
    private Contact currentContact;
    private JPanel chatBackgroundPanel;
    private Image currentBackgroundImage = null;
    private JPanel pinnedMessagePanel;
    private JLabel jPinnedText;
    public JUi(String name, String id) {
        this.userId = id;
        this.username = name;
        initComponents();
        this.controller = Controller.getInstance();
        controller.initController(username,userId,this);
        System.out.println(userId);
    }
    
    private void initComponents() {
        setTitle("ChatUPB");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // ================= LEFT PANEL =================
        chatListModel = new DefaultListModel<>();
        chatList = new JList<>(chatListModel);
        chatList.setCellRenderer(new ContactRender());
        chatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chatList.setFixedCellHeight(40);
        chatList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                currentContact = chatList.getSelectedValue();
                if (currentContact != null) {
                    System.out.println("///////////Datos////////////");
                    System.out.println(currentContact.getId());
                    System.out.println(currentContact.getName());
                    System.out.println("//////////////////////////////");
                    messageController.onLoadMessages(currentContact.getId());
                    controller.markRead(currentContact);
                    System.out.println("IDTHEME: " + currentContact.getIdTheme());
                    applyTheme(currentContact.getIdTheme());
                    if (currentContact.getIdPinMessage() != null && !currentContact.getIdPinMessage().isEmpty()) {
                        boolean found = false;
                        for (int i = 0; i < messageListModel.getSize(); i++) {
                            Message m = messageListModel.getElementAt(i);
                            if (m.getIdMessage().equals(currentContact.getIdPinMessage())) {
                                updatePinnedMessageUI(m.getBody());
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            updatePinnedMessageUI("Mensaje fijado...");
                        }
                    } else {
                        updatePinnedMessageUI(null);
                    }
                }
            }
        });
        JScrollPane leftScrollPane = new JScrollPane(chatList);
        leftScrollPane.setPreferredSize(new Dimension(250, 600));
        // ================= RIGHT PANEL =================
        messageListModel = new DefaultListModel<>();
        messageList = new JList<>(messageListModel);
        messageList.setCellRenderer(new MessageRender());
        messageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        messageList.setOpaque(false);
        messageList.setBackground(new Color(0, 0, 0, 0));
        JScrollPane chatScrollPane = new JScrollPane(messageList);
        chatScrollPane.setBorder(null);
        chatScrollPane.setOpaque(false);
        chatScrollPane.getViewport().setOpaque(false);
        setupMessageContextMenu();
        chatBackgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (currentBackgroundImage != null) {
                    g.drawImage(currentBackgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(new Color(240, 240, 240));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        chatBackgroundPanel.setLayout(new BorderLayout());
        chatBackgroundPanel.add(chatScrollPane, BorderLayout.CENTER);
        pinnedMessagePanel = new JPanel(new BorderLayout());
        pinnedMessagePanel.setBackground(new Color(230, 240, 255));
        pinnedMessagePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        pinnedMessagePanel.setVisible(false);
        JLabel pinIcon = new JLabel(" 📌 ");
        jPinnedText = new JLabel("Mensaje fijado...");
        jPinnedText.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        pinnedMessagePanel.add(pinIcon, BorderLayout.WEST);
        pinnedMessagePanel.add(jPinnedText, BorderLayout.CENTER);
        JPanel centerChatPanel = new JPanel(new BorderLayout());
        centerChatPanel.add(pinnedMessagePanel, BorderLayout.NORTH);
        centerChatPanel.add(chatBackgroundPanel, BorderLayout.CENTER);
        // ================= BUTTONS & MENUS =================
        jTextMensaje = new JTextField();
        JButton btnSend = new JButton("Enviar 💣");
        JButton btnImage = new JButton("📷");
        JButton btnBuzz = new JButton("Buzz");
        JButton btnTheme = new JButton("🎨 Temas");
        JButton btnOffline = new JButton("Fuera de Línea");
        JButton btnNewConnection = new JButton("Nueva Conexión");
        JButton btnConnect = new JButton("Conectar");
        JPopupMenu themeMenu = new JPopupMenu();
        String[] themeNames = {"1. Atacama", "2. Oscuro", "3. Valle", "4. Salar", "5. XP"};
        jOnline = new JLabel("Status: Offline");
        // Top Panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(btnConnect);
        topPanel.add(btnNewConnection);
        topPanel.add(btnBuzz);
        topPanel.add(btnTheme);
        for (int i = 0; i < 5; i++) {
            String themeId = String.valueOf(i + 1);
            JMenuItem item = new JMenuItem(themeNames[i]);
            item.addActionListener(e -> {
                if (currentContact == null) {
                    showError("Selecciona un contacto primero para cambiar el tema de la conversación.");
                    return;
                }
                applyTheme(themeId);
                controller.sendTheme(currentContact.getId(), themeId);
            });
            themeMenu.add(item);
        }
        
        btnTheme.addActionListener(e -> themeMenu.show(btnTheme, 0, btnTheme.getHeight()));
        // Bottom Panel
        JPanel leftActionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftActionPanel.add(btnImage);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(leftActionPanel, BorderLayout.WEST);
        bottomPanel.add(jTextMensaje, BorderLayout.CENTER);
        bottomPanel.add(btnSend, BorderLayout.EAST);
        // Ensamblaje Final del Right Panel
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(topPanel, BorderLayout.NORTH);
        rightPanel.add(centerChatPanel, BorderLayout.CENTER);
        rightPanel.add(bottomPanel, BorderLayout.SOUTH);
        setLayout(new BorderLayout());
        add(leftScrollPane, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
        // ================= ACTIONS =================
        jTextMensaje.addActionListener(e -> {
            String texto = jTextMensaje.getText().trim();
            if (!texto.isEmpty() && currentContact != null) {
                Chat chat = new Chat(this.userId, UUID.randomUUID().toString(), texto);
                controller.sendMessage(chat, currentContact.getId());
                jTextMensaje.setText("");
            } else if (currentContact == null) {
                showError("Por favor, selecciona un contacto de la lista izquierda para chatear.");
            }
        });
        btnSend.addActionListener(e -> {
            String texto = jTextMensaje.getText().trim();
            if (!texto.isEmpty() && currentContact != null) {
                controller.sendUniqueMessage(texto, currentContact.getId());
                jTextMensaje.setText("");
            } else if (currentContact == null) {
                showError("Por favor, selecciona un contacto de la lista izquierda para chatear.");
            }
        });
        btnBuzz.addActionListener(e -> controller.sendBuzz(currentContact.getId()));
        btnOffline.addActionListener(e -> controller.sendBye());
        btnNewConnection.addActionListener(e ->
                new ConnectionDialog(this, controller).setVisible(true)
        );
        btnConnect.addActionListener(e->{
            if(currentContact != null){
                System.out.println("CurrentContact: " + currentContact.getName());
                if(!controller.getClients().containsKey(currentContact.getId())){
                    controller.sendHello(currentContact.getIp(),currentContact.getName(),currentContact.getId());
                }
            }
        });
        btnImage.addActionListener(e -> {
            if (currentContact == null) {
                showError("Por favor, selecciona un contacto primero.");
                return;
            }
            new ImageDialog(this, currentContact.getId(), controller).setVisible(true);
        });
        pinnedMessagePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(currentContact != null && currentContact.getIdPinMessage() != null){
                    for (int i = 0; i < messageListModel.getSize(); i++) {
                        if(messageListModel.getElementAt(i).getIdMessage().equals(currentContact.getIdPinMessage())){
                            messageList.ensureIndexIsVisible(i);
                            messageList.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
        });
        try {
            Image icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/logo.png"));
            this.setIconImage(icon);
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono de la ventana");
        }
    }
    public void init() {
        EventQueue.invokeLater(() -> setVisible(true));
    }
    
    //Funciones que utiliza la UI y solamente ella
    private void shakeWindow() {
        final Point originalLocation = this.getLocation();
        final int shakeDistance = 15;
        Timer timer = new Timer(30, new java.awt.event.ActionListener() {
            int counter = 0;
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (counter >= 20) {
                    setLocation(originalLocation);
                    ((Timer) e.getSource()).stop();
                } else {
                    int dx = (counter % 2 == 0) ? shakeDistance : -shakeDistance;
                    int dy = (counter % 4 < 2) ? shakeDistance : -shakeDistance;
                    setLocation(originalLocation.x + dx, originalLocation.y + dy);
                    counter++;
                }
            }
        });
        timer.start();
    }
    public void changeTheme(String imagePath, Color myBubble, Color otherBubble, Color myText, Color otherText) {
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                java.net.URL imgURL = getClass().getResource(imagePath);
                if (imgURL != null) {
                    currentBackgroundImage = new ImageIcon(imgURL).getImage();
                } else {
                    System.err.println("❌ No se encontró la imagen del tema en: " + imagePath);
                    currentBackgroundImage = null;
                }
            } catch (Exception e) {
                System.err.println("Error al cargar la imagen del tema: " + e.getMessage());
                currentBackgroundImage = null;
            }
        } else {
            currentBackgroundImage = null;
        }
        MessageRender render = (MessageRender) messageList.getCellRenderer();
        render.setThemeColors(myBubble, otherBubble, myText, otherText);
        chatBackgroundPanel.repaint();
        messageList.repaint();
    }
    private void setupMessageContextMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem pinItem = new JMenuItem("Pin mensaje");
        JMenuItem deleteMeItem = new JMenuItem("Eliminar para mí");
        JMenuItem deleteAllItem = new JMenuItem("Eliminar para todos");
        popupMenu.add(pinItem);
        popupMenu.add(deleteMeItem);
        popupMenu.add(deleteAllItem);
        // Acciones de los botones del menú
        pinItem.addActionListener(e -> {
            Message selectedMsg = messageList.getSelectedValue();
            if (selectedMsg != null && currentContact != null) {
                System.out.println("Pin al mensaje: " + selectedMsg.getIdMessage());
                controller.sendPinMessage(selectedMsg, currentContact.getId());
                updatePinnedMessageUI(selectedMsg.getBody());
            }
        });
        deleteMeItem.addActionListener(e -> {
            Message selectedMsg = messageList.getSelectedValue();
            if (selectedMsg != null) {
                System.out.println("Eliminar localmente: " + selectedMsg.getIdMessage());
                controller.deleteMessage(selectedMsg);
            }
        });
        deleteAllItem.addActionListener(e -> {
            Message selectedMsg = messageList.getSelectedValue();
            if (selectedMsg != null) {
                System.out.println("Solicitar eliminar en red: " + selectedMsg.getIdMessage());
                controller.sendDeleteMessage(selectedMsg,currentContact.getId());
            }
        });
        messageList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { showPopup(e); }
            @Override
            public void mouseReleased(MouseEvent e) { showPopup(e); }
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    int index = messageList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        Message selectedMsg = messageList.getModel().getElementAt(index);
                        if (selectedMsg.getTypeMessage() == TypeMessage.UNIQUE && !selectedMsg.getBody().equals("VISTO")) {
                            JOptionPane.showMessageDialog(JUi.this,
                                    "Mensaje confidencial:\n\n" + selectedMsg.getBody(),
                                    "💣 Mensaje de una sola vista",
                                    JOptionPane.WARNING_MESSAGE);
                            if (currentContact != null && controller != null) {
                                controller.sendUniqueMessageSeen(selectedMsg, currentContact.getId());
                            }
                            selectedMsg.setBody("VISTO");
                            messageList.repaint();
                        }
                    }
                }
            }
            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = messageList.locationToIndex(e.getPoint());
                    if (row >= 0) {
                        messageList.setSelectedIndex(row);
                        Message selectedMsg = messageList.getModel().getElementAt(row);
                        boolean isMe = !selectedMsg.getStatusMessage().toString().equals("RECEIVED");
                        deleteAllItem.setVisible(isMe);
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
    }
    // ================= IChatView =================
    @Override
    public void updateStatus(String status) {
        jOnline.setText(status);
    }
    @Override
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Notificación", JOptionPane.INFORMATION_MESSAGE);    }
    @Override
    public void showError(String error) {
        JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
    }
    @Override
    public boolean showInvitationDialog(String userName, String id) {
        int res = JOptionPane.showConfirmDialog(
                this,
                "Invitación de: " + userName + ". ¿Aceptar?",
                "Nueva conexión",
                JOptionPane.YES_NO_OPTION
        );
        return res == JOptionPane.YES_OPTION;
    }
    @Override
    public void showBuzzNotification(String senderName) {
//        showMessage("\n--- ¡" + senderName + " te ha enviado un Zumbido! ---\n");
        this.setExtendedState(javax.swing.JFrame.NORMAL);
        this.toFront();
        this.requestFocus();
        shakeWindow();
        Sound.BUZZ.play();
        showMessage(senderName + " te envio un zumbido");
    }
    @Override
    public void showChat(Chat chat) {
        if (currentContact != null && currentContact.getId().equals(chat.getIdUser())) {
            refreshChatView();
        }
    }
    @Override
    public void showByeNotification(String id) {
        JOptionPane.showMessageDialog(this,
                id + " se desconectó",
                "Desconectado",
                JOptionPane.INFORMATION_MESSAGE);
    }
    @Override
    public void onLoadContacts(List<Contact> contacts) {
        String selectedId = null;
        if (currentContact != null) {
            selectedId = currentContact.getId();
        }
        chatListModel.clear();
        if (contacts != null) {
            for (Contact c : contacts) {
                if(controller.getClients().containsKey(c.getId()))
                    c.setStateConnect(true);
                chatListModel.addElement(c);
            }
        }
        if (selectedId != null) {
            for (int i = 0; i < chatListModel.getSize(); i++) {
                Contact c = chatListModel.getElementAt(i);
                if (c.getId().equals(selectedId)) {
                    currentContact = c;
                    chatList.setSelectedIndex(i);
                    break;
                }
            }
        }
    }
    @Override
    public void onLoadMessages(List<Message> messages) {
        messageListModel.clear();
        if (messages != null) {
            for (Message msg : messages) {
                messageListModel.addElement(msg);
            }
            int lastIndex = messageListModel.getSize() - 1;
            if (lastIndex >= 0) {
                messageList.ensureIndexIsVisible(lastIndex);
            }
        }
    }
    @Override
    public void onAddModel(Contact contact) {
        boolean exists = false;
        for (int i = 0; i < chatListModel.size(); i++) {
            if (chatListModel.get(i).getId().equals(contact.getId())) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            chatListModel.addElement(contact);
        }
    }
    @Override
    public void refreshChatView() {
        if (currentContact != null) {
            messageController.onLoadMessages(currentContact.getId());
        }
    }
    @Override
    public Contact getCurrentContact() {
        return currentContact;
    }
    @Override
    public void showImageMessage(ImageMesagge imageMessage) {
        if (currentContact != null && currentContact.getId().equals(imageMessage.getIdUser())) {
            refreshChatView();
        } else {
            System.out.println("Recibiste una imagen en segundo plano de: " + imageMessage.getIdUser());
        }
    }
    @Override
    public void applyTheme(String themeId) {
        String basePath = "/images/themes/";
        switch (themeId) {
            case "1": // Por Defecto (Claro)
                changeTheme(basePath + "Atacama.png",
                        new Color(220, 248, 198), Color.WHITE,
                        Color.BLACK, Color.BLACK);
                break;
            case "2": // Modo Oscuro
                changeTheme(basePath + "paisaje-digital-en-atardecer-5846.jpg",
                        new Color(5, 70, 64), new Color(38, 45, 49),
                        Color.WHITE, Color.WHITE);
                break;
            case "3":  //LandScape
                changeTheme(basePath + "HD-wallpaper-summer-landscape-mountain-summer-beaches-landscape.jpg",
                        Color.BLACK, new Color(20, 20, 20),
                        Color.GREEN, new Color(0, 200, 0));
                break;
            case "4": // Salar
                changeTheme(basePath + "hermoso-salar-uyuni-bolivia_181624-41087.png",
                        new Color(173, 216, 230), new Color(240, 248, 255),
                        Color.DARK_GRAY, Color.DARK_GRAY);
                break;
            case "5": // XP
                changeTheme(basePath + "600_315.jpg",
                        new Color(255, 200, 150), new Color(255, 230, 200),
                        Color.BLACK, Color.BLACK);
                break;
            default:
                System.out.println("ID de tema desconocido: " + themeId);
        }
    }
    @Override
    public void appendMessageToChat(Message msg) {
        messageListModel.addElement(msg);
        messageList.ensureIndexIsVisible(messageListModel.getSize() - 1);
    }
    
    @Override
    public void updatePinnedMessageUI(String text) {
        if (text == null || text.isEmpty()) {
            pinnedMessagePanel.setVisible(false);
        } else {
            String displayText = text.length() > 50 ? text.substring(0, 50) + "..." : text;
            jPinnedText.setText(displayText);
            pinnedMessagePanel.setVisible(true);
        }
        pinnedMessagePanel.revalidate();
        pinnedMessagePanel.repaint();
    }
    public void setContactController(ContactController contactController){
        this.contactController = contactController;
    }
    public void setMessageController(MessageController messageController){
        this.messageController = messageController;
    }
    public void setUserController(UserController userController){
        this.userController = userController;
    }
}