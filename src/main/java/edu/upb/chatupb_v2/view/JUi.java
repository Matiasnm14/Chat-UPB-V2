package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.bl.ChatService;
import edu.upb.chatupb_v2.controller.ContactController;
import edu.upb.chatupb_v2.controller.Controller;
import edu.upb.chatupb_v2.controller.MessageController;
import edu.upb.chatupb_v2.controller.UserController;
import edu.upb.chatupb_v2.controller.exception.OperationException;
import edu.upb.chatupb_v2.model.entities.Contact;
import edu.upb.chatupb_v2.model.entities.Message;
import edu.upb.chatupb_v2.model.entities.User;
import edu.upb.chatupb_v2.model.entities.comands.*;
import edu.upb.chatupb_v2.model.repository.*;
import edu.upb.chatupb_v2.model.repository.enums.StatusMessage;
import edu.upb.chatupb_v2.model.repository.enums.TypeMessage;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class JUi extends JFrame implements IChatView {

    private ChatService chatService;
    @Getter
    private String username;
    @Getter
    private String userId;
    private static final Logger logger = Logger.getLogger(JUi.class.getName());

    private DefaultListModel<Message> messageListModel;
    private JList<Message> messageList;

    private JTextField jTextMensaje;
    private JLabel jOnline;

    private DefaultListModel<Contact> contactListModel;
    private JList<Contact> contactList;

    @Getter
    private Contact currentContact;
    @Setter
    private ContactController contactController;
    @Setter
    private MessageController messageController;
    @Setter
    private UserController userController = new UserController(this);


    private JPanel imagePanel;
    private JLabel imageDropLabel;
    private String base64ImagePending = null;


    private JPanel pinnedMessagePanel;
    private JLabel pinnedMessageLabel;
    private String currentPinnedMessageId = null;

    private long lastBuzzTime = 0;
    private static final long BUZZ_COOLDOWN_MS = 3000;
    private Image chatBackgroundImage = null;

    public JUi() {
        this.username = userController.onLoadUser();
        if (this.username == null || this.username.trim().isEmpty()) {
            System.exit(0);
        }
        initComponents();
        try {
            if (UserDao.getInstance().exist("name='" + username + "'"))
                userId = UserDao.getInstance().findByName(username).getId();
            else {
                userId = UUID.randomUUID().toString();
                UserDao.getInstance().save(new User(userId, username));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("------------------------------------------------------------------");
        System.out.println("ID: " + userId);
        System.out.println("Nombre: " + username);
        System.out.println("------------------------------------------------------------------");
        Controller.getInstance().addUi(this);
    }

    public void updateContacts() {
        String selectedContactId = (currentContact != null) ? currentContact.getId() : null;
        contactController.onLoadContacts();

        if (selectedContactId != null) {
            for (int i = 0; i < contactListModel.getSize(); i++) {
                Contact c = contactListModel.getElementAt(i);
                if (c.getId().equals(selectedContactId)) {
                    contactList.setSelectedIndex(i);
                    currentContact = c;
                    messageController.onLoadMessages(currentContact.getId());
                    break;
                }
            }
        }
    }

    private String askForUsername() {
        JTextField textField = new JTextField(20);

        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string == null) return;
                if ((fb.getDocument().getLength() + string.length()) <= 60) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) return;
                if ((fb.getDocument().getLength() + text.length() - length) <= 60) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });

        Object[] message = {
                "Ingresa tu nombre de usuario (máx 60 caracteres):", textField
        };

        int option = JOptionPane.showConfirmDialog(
                this,
                message,
                "Bienvenido a ChatUPB",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (option == JOptionPane.OK_OPTION) {
            return textField.getText();
        }

        return null;
    }

    private void initComponents() {
        setTitle("ChatUPB");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // ================= LEFT PANEL =================
        contactListModel = new DefaultListModel<>();
        contactList = new JList<>(contactListModel);
        contactList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactList.setFixedCellHeight(40);
        contactList.setCellRenderer(new ContactRender());

        contactList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                currentContact = contactList.getSelectedValue();
                if (currentContact != null) {
                    currentContact.setBuzz(false);
                    applyThemeToChat(currentContact.getTheme());
                    messageController.onLoadMessages(currentContact.getId());
                    if (Controller.getInstance().getClients().containsKey(currentContact.getId())) {
                        Controller.getInstance().sendConfirms(currentContact.getId());
                    }
                    if (currentContact.isBuzz()) {
                        currentContact.setBuzz(false);
                        updateContacts();
                    }
                }
            }
        });

        JScrollPane leftScrollPane = new JScrollPane(contactList);
        leftScrollPane.setPreferredSize(new Dimension(250, 600));

        // ================= RIGHT PANEL =================

        messageListModel = new DefaultListModel<>();
        messageList = new JList<>(messageListModel);
        messageList.setOpaque(false);

        // 2. Creamos el ScrollPane base
        JScrollPane chatScrollPane = new JScrollPane();
        chatScrollPane.setOpaque(false);

        // 3. ¡La magia! Creamos un Viewport (el cristal) personalizado
        JViewport viewport = new JViewport() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Pintamos la imagen en el cristal. Como el cristal no se mueve al scrollear,
                // la imagen siempre estará visible.
                if (chatBackgroundImage != null) {
                    g.drawImage(chatBackgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };

        viewport.setOpaque(false);
        viewport.setView(messageList);


        chatScrollPane.setViewport(viewport);


        messageList.setOpaque(false);
        messageList.setCellRenderer(new ChatRender());
        messageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        messageList.setFocusable(false);
        messageList.setOpaque(false);



        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem itemEliminarParaMi = new JMenuItem("Eliminar para mí");
        JMenuItem itemEliminarParaTodos = new JMenuItem("Eliminar para todos");
        JMenuItem itemFijarMensaje = new JMenuItem("Fijar Mensaje");
        JMenuItem itemCambiarTema = new JMenuItem("Cambiar tema de contacto");

        popupMenu.add(itemEliminarParaMi);
        popupMenu.add(itemEliminarParaTodos);
        popupMenu.addSeparator();
        popupMenu.add(itemFijarMensaje);
        popupMenu.addSeparator();
        popupMenu.add(itemCambiarTema);

        // --- LISTENER DE MENSAJES (CLICK IZQUIERDO PARA UNIQUE, DERECHO PARA MENÚ) ---
        messageList.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Click Izquierdo (BUTTON1)
                if (e.getButton() == java.awt.event.MouseEvent.BUTTON1) {
                    int index = messageList.locationToIndex(e.getPoint());
                    if (index != -1 && messageList.getCellBounds(index, index).contains(e.getPoint())) {
                        Message selectedMessage = messageListModel.getElementAt(index);


                        if (selectedMessage.getTypeMessage() == TypeMessage.UNIQUE) {

                            JOptionPane.showMessageDialog(
                                    JUi.this,
                                    selectedMessage.getBody(),
                                    "Mensaje de una sola vista",
                                    JOptionPane.INFORMATION_MESSAGE
                            );

                            try {
                                messageController.updateUniqueMessage(selectedMessage.getIdMessage());
                                messageController.onLoadMessages(selectedMessage.getContactId());
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    }
                }
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                showPopup(e);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                showPopup(e);
            }

            private void showPopup(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int index = messageList.locationToIndex(e.getPoint());
                    if (index != -1 && messageList.getCellBounds(index, index).contains(e.getPoint())) {
                        messageList.setSelectedIndex(index);
                        Message selectedMessage = messageList.getModel().getElementAt(index);

                        boolean isMine = selectedMessage.getStatusMessage() != StatusMessage.READ;
                        itemEliminarParaTodos.setVisible(isMine);

                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        itemEliminarParaMi.addActionListener(e -> {
            Message msg = messageList.getSelectedValue();
            messageController.delete(msg.getIdMessage());
            if (currentContact != null) messageController.onLoadMessages(currentContact.getId());
        });

        itemEliminarParaTodos.addActionListener(e -> {
            Message msg = messageList.getSelectedValue();
            if (currentContact != null){
                DeleteMessage deleteMessage = new DeleteMessage(msg.getIdMessage());
                messageController.delete(msg.getIdMessage());
                Controller.getInstance().sendMessage(deleteMessage, currentContact.getId());
                messageController.onLoadMessages(currentContact.getId());
            }
        });

        itemFijarMensaje.addActionListener(e -> {
            Message msg = messageList.getSelectedValue();
            if (currentContact != null){
                PinMessage pinMessage = new PinMessage(msg.getIdMessage());
                Controller.getInstance().sendMessage(pinMessage, currentContact.getId());
                onPinMessageReceived(pinMessage);
                messageController.onLoadMessages(currentContact.getId());
            }

        });

        itemCambiarTema.addActionListener(e -> {
            if (currentContact == null) {
                showError("Selecciona un contacto primero.");
                return;
            }

            String[] options = {"Tema 1", "Tema 2", "Tema 3", "Tema 4", "Tema 5"};
            int selection = JOptionPane.showOptionDialog(this,
                    "Elige un tema de fondo para " + currentContact.getName(),
                    "Cambiar Tema",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

            if (selection >= 0) {
                String themeId = (selection == 5) ? "0" : String.valueOf(selection + 1);

                try {
                    ContactDao.getInstance().updateTheme(currentContact.getId(), themeId);
                    currentContact.setTheme(themeId);
                    applyThemeToChat(themeId);

                    Theme themeCmd = new Theme(userId, themeId);
                    Controller.getInstance().sendMessage(themeCmd, currentContact.getId());

                } catch (Exception ex) {
                    showError("Error al guardar el tema: " + ex.getMessage());
                }
            }
        });

        jTextMensaje = new JTextField();
        JButton btnSend = new JButton("Enviar");
        JButton btnSendUnique = new JButton("Enviar Único 🔒");
        JToggleButton btnToggleImage = new JToggleButton("📷");

        JButton btnBuzz = new JButton("Buzz");
        JButton btnTema = new JButton("Cambiar Tema");
        JButton btnNewConnection = new JButton("Nueva Conexión");
        JButton btnConectar = new JButton("Conectar a Contacto");

        jOnline = new JLabel("Status: Offline");

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(btnConectar);
        topPanel.add(btnNewConnection);
        topPanel.add(btnBuzz);
        topPanel.add(btnTema);

        // --- ZONA DE MENSAJE FIJADO ---
        pinnedMessagePanel = new JPanel(new BorderLayout());
        pinnedMessagePanel.setBackground(new Color(230, 240, 255));
        pinnedMessagePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        pinnedMessagePanel.setVisible(false);
        pinnedMessagePanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        pinnedMessageLabel = new JLabel("📌 Mensaje Fijado");
        pinnedMessageLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pinnedMessagePanel.add(pinnedMessageLabel, BorderLayout.CENTER);

        pinnedMessagePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentPinnedMessageId != null) {
                    for (int i = 0; i < messageListModel.getSize(); i++) {
                        if (messageListModel.getElementAt(i).getIdMessage().equals(currentPinnedMessageId)) {
                            messageList.ensureIndexIsVisible(i);
                            messageList.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
        });

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(topPanel, BorderLayout.NORTH);
        topContainer.add(pinnedMessagePanel, BorderLayout.SOUTH);

        // --- ZONA DE IMAGEN ---
        imagePanel = new JPanel(new BorderLayout());
        imagePanel.setVisible(false);
        imagePanel.setPreferredSize(new Dimension(0, 120));
        imagePanel.setBorder(BorderFactory.createTitledBorder("Enviar Imagen"));

        imageDropLabel = new JLabel("Arrastra y suelta una imagen aquí", SwingConstants.CENTER);
        imageDropLabel.setBorder(BorderFactory.createDashedBorder(Color.GRAY, 2, 5, 2, false));

        JButton btnSendImage = new JButton("Enviar Imagen");
        btnSendImage.setEnabled(false);

        imagePanel.add(imageDropLabel, BorderLayout.CENTER);
        imagePanel.add(btnSendImage, BorderLayout.EAST);

        imageDropLabel.setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

                    if (droppedFiles != null && !droppedFiles.isEmpty()) {
                        File file = droppedFiles.get(0);
                        String name = file.getName().toLowerCase();
                        if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif")) {
                            byte[] fileContent = Files.readAllBytes(file.toPath());
                            base64ImagePending = Base64.getEncoder().encodeToString(fileContent);

                            ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                            Image scaledImage = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                            imageDropLabel.setText("");
                            imageDropLabel.setIcon(new ImageIcon(scaledImage));
                            btnSendImage.setEnabled(true);
                        } else {
                            showError("Por favor, selecciona un archivo de imagen válido (.jpg, .png, .gif)");
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError("Error al cargar la imagen");
                }
            }
        });

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(btnToggleImage, BorderLayout.WEST);
        inputPanel.add(jTextMensaje, BorderLayout.CENTER);
//        inputPanel.add(btnSend, BorderLayout.EAST);
        JPanel sendButtonsPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        sendButtonsPanel.add(btnSendUnique);
        sendButtonsPanel.add(btnSend);
        inputPanel.add(sendButtonsPanel, BorderLayout.EAST);

        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.add(imagePanel, BorderLayout.NORTH);
        bottomContainer.add(inputPanel, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(topContainer, BorderLayout.NORTH);
        rightPanel.add(chatScrollPane, BorderLayout.CENTER);
        rightPanel.add(bottomContainer, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(leftScrollPane, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        // ================= ACTIONS =================

        btnToggleImage.addActionListener(e -> {
            imagePanel.setVisible(btnToggleImage.isSelected());
            revalidate();
            repaint();
        });

        btnSendImage.addActionListener(e -> {
            new Thread(() -> {
                if (base64ImagePending != null && currentContact != null) {
                    String destinationId = currentContact.getId();

                    ImageChat imageChat = new ImageChat(userId, UUID.randomUUID().toString(), base64ImagePending);
                    Controller.getInstance().sendMessage(imageChat, destinationId);

                    SwingUtilities.invokeLater(() -> {
                        base64ImagePending = null;
                        imageDropLabel.setIcon(null);
                        imageDropLabel.setText("Arrastra y suelta una imagen aquí");
                        btnSendImage.setEnabled(false);
                        btnToggleImage.setSelected(false);
                        imagePanel.setVisible(false);
                        revalidate();
                    });
                } else if (currentContact == null) {
                    SwingUtilities.invokeLater(() -> showError("Selecciona un contacto primero."));
                }
            }).start();
        });

        btnSend.addActionListener(e -> {
            String texto = jTextMensaje.getText().trim();

            if (!texto.isEmpty() && currentContact != null) {
                String destinationId = currentContact.getId();
                if (destinationId == null || destinationId.trim().isEmpty()) {
                    SwingUtilities.invokeLater(() ->
                            showError("Selecciona un contacto primero."));
                    return;
                }
                Chat chat = new Chat(userId, UUID.randomUUID().toString(), texto);
                Controller.getInstance().sendMessage(chat, destinationId);
                jTextMensaje.setText("");
            } else if (currentContact == null) {
                showError("Por favor, selecciona un contacto de la lista izquierda para chatear.");
            }
        });

        btnSendUnique.addActionListener(e -> {
            String texto = jTextMensaje.getText().trim();

            if (!texto.isEmpty() && currentContact != null) {
                String destinationId = currentContact.getId();
                if (destinationId == null || destinationId.trim().isEmpty()) {
                    SwingUtilities.invokeLater(() ->
                            showError("Selecciona un contacto primero."));
                    return;
                }


                UniqueMessage uniqueMessage = new UniqueMessage(userId, UUID.randomUUID().toString(), texto);
                Controller.getInstance().sendMessage(uniqueMessage, destinationId);

                jTextMensaje.setText("");
            } else if (currentContact == null) {
                showError("Por favor, selecciona un contacto de la lista izquierda para chatear.");
            }
        });

        btnConectar.addActionListener(e -> {
            if (currentContact != null) {
                if (!Controller.getInstance().getClients().containsKey(currentContact.getId())) {
                    SwingUtilities.invokeLater(() -> {
                        Controller.getInstance().sendHello(currentContact.getIp(), currentContact.getName(), currentContact.getId());
                    });
                }
            } else showError("Seleccione un Contacto Primero");
        });

        btnBuzz.addActionListener(e -> Controller.getInstance().sendBuzz());




        btnTema.addActionListener(e -> {
            if (currentContact == null) {
                showError("Selecciona un contacto primero.");
                return;
            }else if (!currentContact.isStateConnect()){
                showError("Contacto fuera de linea.");
                return;
            }

            String[] options = {"Tema 1", "Tema 2", "Tema 3", "Tema 4", "Tema 5"};
            int selection = JOptionPane.showOptionDialog(this,
                    "Elige un tema de fondo para " + currentContact.getName(),
                    "Cambiar Tema",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

            if (selection >= 0) {
                String themeId = (selection == 5) ? "0" : String.valueOf(selection + 1);

                try {
                    ContactDao.getInstance().updateTheme(currentContact.getId(), themeId);
                    currentContact.setTheme(themeId);
                    applyThemeToChat(themeId);

                    Theme themeCmd = new Theme(userId, themeId);
                    Controller.getInstance().sendMessage(themeCmd, currentContact.getId());

                } catch (Exception ex) {
                    showError("Error al guardar el tema: " + ex.getMessage());
                }
            }
        });

        btnNewConnection.addActionListener(e ->
                new ConnectionDialog(this).setVisible(true)
        );
    }

    public void init() {
        EventQueue.invokeLater(() -> setVisible(true));
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            int size = messageListModel.getSize();
            if (size > 0) {
                messageList.ensureIndexIsVisible(size - 1);
            }
        });
    }

    @Override
    public void updateStatus(String status) {
        jOnline.setText(status);
    }

    @Override
    public void showMessage(String message) {
        Message sysMsg = new Message(
                UUID.randomUUID().toString(),
                "system",
                "⚙️ " + message,
                TypeMessage.TEXT,
                StatusMessage.READ,
                LocalDate.now().toString()
        );
        messageListModel.addElement(sysMsg);
        scrollToBottom();
    }

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
        JOptionPane.showMessageDialog(this,
                senderName + " te envió un buzz",
                "Buzz",
                JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void showChat(Chat chat) {
        Message msg = new Message(
                chat.getIdMessage(),
                chat.getIdUser(),
                chat.getMessage(),
                TypeMessage.TEXT,
                StatusMessage.READ,
                LocalDate.now().toString()
        );
        messageListModel.addElement(msg);
        scrollToBottom();
    }

    @Override
    public void showByeNotification(String id) {
        JOptionPane.showMessageDialog(this,
                id + " se desconectó",
                "Desconectado",
                JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void addModel(Contact contact) {
        boolean exists = false;
        for (int i = 0; i < contactListModel.size(); i++) {
            if (contactListModel.get(i).getId().equals(contact.getId())) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            contactListModel.addElement(contact);
        }
    }

    // --- MÉTODOS DE IMÁGENES ---
    public void showImageChat(ImageChat imageChat) {
        Message msg = new Message(
                imageChat.getIdMessage(),
                imageChat.getIdUser(),
                imageChat.getMessage(),
                TypeMessage.IMAGE,
                StatusMessage.READ,
                LocalDate.now().toString()
        );
        messageListModel.addElement(msg);
        scrollToBottom();
    }

    public ConfirmRecived onImageChatReceived(ImageChat imageChat) {
        if (currentContact != null && currentContact.getId().equals(imageChat.getIdUser())) {
            showImageChat(imageChat);
        }
        try {
            Message msgDb = new Message(
                    imageChat.getIdMessage(),
                    imageChat.getIdUser(),
                    imageChat.getMessage(),
                    TypeMessage.IMAGE,
                    StatusMessage.READ,
                    LocalDate.now().toString()
            );
            messageController.save(msgDb);

        } catch (Exception e) {
            System.out.println("Error al guardar imagen recibida: " + e.getMessage());
        }
        return new ConfirmRecived(imageChat.getIdMessage());
    }

    public void updateMessages(){
        messageController.onLoadMessages(currentContact.getId());
    }

    @Override
    public void onLoadContacts(List<Contact> contacts) {
        contactListModel.clear();
        if (contacts != null) {
            for (Contact c : contacts) {
                c.setStateConnect(Controller.getInstance().getClients().containsKey(c.getId()));
                contactListModel.addElement(c);
            }
        }
    }

    @Override
    public String onLoadUser(List<User> users) {
        if (users.isEmpty()) {
            return askForUsername();
        } else return users.getFirst().getName();
    }

    @Override
    public void onLoadMessages(List<Message> messages) {
        messageListModel.clear();
        if (messages != null && currentContact != null) {
            for (Message msg : messages) {
                messageListModel.addElement(msg);
            }
        }
        scrollToBottom();

        if (currentContact != null) {
            String pinId = currentContact.getId_pin();

//            showError(pinId);

            if (pinId != null && !pinId.equals("none")) {
                Message pinnedMsg = null;
                for (int i = 0; i < messageListModel.getSize(); i++) {
                    if (messageListModel.getElementAt(i).getIdMessage().equals(pinId)) {
                        pinnedMsg = messageListModel.getElementAt(i);
                        break;
                    }
                }
                if (pinnedMsg != null) {
                    showPinnedMessageUI(pinnedMsg);
                } else {
                    pinnedMessagePanel.setVisible(false);
                    currentPinnedMessageId = null;
                }
            } else {
                pinnedMessagePanel.setVisible(false);
                currentPinnedMessageId = null;
            }
        }
    }

    public boolean onInvitationReceived(Invitation invitation) {
        boolean accepted = showInvitationDialog(invitation.getUserName(), invitation.getIdUser());
        Controller.getInstance().getPendingClients().getFirst().setUid(invitation.getIdUser());
        Controller.getInstance().addClients(Controller.getInstance().getPendingClients().getFirst());
        Controller.getInstance().getPendingClients().removeFirst();

        if (accepted) {
            try {
                Contact nuevoContacto = new Contact();
                nuevoContacto.setId(invitation.getIdUser());
                nuevoContacto.setName(invitation.getUserName());
                nuevoContacto.setIp(Controller.getInstance().getClients().get(invitation.getIdUser()).getIp());
                nuevoContacto.setUserId(this.userId);
                nuevoContacto.setStateConnect(true);
                nuevoContacto.setId_pin("none");
                nuevoContacto.setTheme("1");

                ContactDao.getInstance().save(nuevoContacto);

                SwingUtilities.invokeLater(() -> {
                    nuevoContacto.setId(invitation.getIdUser());
                    addModel(nuevoContacto);
                    contactController.onLoadContacts();
                });
                return true;

            } catch (Exception e) {
                System.out.println("Error procesando invitación aceptada: " + e.getMessage());
            }
            return true;
        } else {
            return false;
        }
    }

    public Contact onAcceptReceived(Accept accept, String ip) {
        updateStatus("Status: Online");
        showMessage("Conexión Aceptada");
        Contact nuevoContacto = new Contact();
        nuevoContacto.setId(accept.getIdUser());
        nuevoContacto.setName(accept.getUserName());
        nuevoContacto.setIp(ip);
        nuevoContacto.setUserId(this.userId);
        nuevoContacto.setStateConnect(true);
        nuevoContacto.setId_pin("none");
        nuevoContacto.setTheme("1");

        nuevoContacto.setId(accept.getIdUser());
        addModel(nuevoContacto);

        updateStatus("Status: Online");
        showMessage("Conexión Aceptada con " + accept.getUserName());
        return nuevoContacto;
    }

    public void onDeclineReceived(Decline decline) {
        SwingUtilities.invokeLater(() -> {
            updateStatus("Status: Rejected");
            showMessage("Conexión Rechazada");
        });
    }

    public AcceptHello onHelloReceived(Hello hello) {
        return new AcceptHello(userId);
    }

    public ConfirmRecived onChatReceived(Chat chat) {
        if (currentContact != null && currentContact.getId().equals(chat.getIdUser()))
            showChat(chat);
        try {
            Message msgDb = new Message(
                    chat.getIdMessage(),
                    chat.getIdUser(),
                    chat.getMessage(),
                    TypeMessage.TEXT,
                    StatusMessage.READ,
                    LocalDate.now().toString()
            );
            messageController.save(msgDb);

        } catch (Exception e) {
            System.out.println("Error al guardar mensaje recibido: " + e.getMessage());
        }
        return new ConfirmRecived(chat.getIdMessage());
    }



    public void onBuzzingReceived(String finalName) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastBuzzTime < BUZZ_COOLDOWN_MS) return;
        lastBuzzTime = currentTime;

        SwingUtilities.invokeLater(() -> {

            shakeWindow(this);
            markContactConBuzz(finalName);
        });
    }

    private void shakeWindow(Window window) {
        final int originalX = window.getLocation().x;
        final int originalY = window.getLocation().y;
        final int distance = 15;

        new Thread(() -> {
            try {
                for (int i = 0; i < 15; i++) {
                    Thread.sleep(30);
                    final int offset = (i % 2 == 0) ? distance : -distance;
                    SwingUtilities.invokeLater(() -> window.setLocation(originalX + offset, originalY));
                }

                SwingUtilities.invokeLater(() -> window.setLocation(originalX, originalY));
            } catch (InterruptedException err) {
                System.out.println("Error en la vibración: " + err.getMessage());
            }
        }).start();
    }

    private void markContactConBuzz(String contactName) {
        try {
            for (int i = 0; i < contactListModel.size(); i++) {
                Contact contact = contactListModel.get(i);
                if (contact.getName().equals(contactName)) {
                    contact.setBuzz(true);
                }
            }
            contactList.repaint();
        }catch (Exception e){
            throw new OperationException("No se encontro el contacto en la DB");
        }
    }

    public void onByeReceived() {}

    public void onAcceptHelloReceived(AcceptHello acceptHello) {}

    public void onDeleteMessageReceived(DeleteMessage deleteMessage) {
        messageController.delete(deleteMessage.getIdMessage());
        if (currentContact != null) messageController.onLoadMessages(currentContact.getId());
    }

    public void onDeclineHelloReceived(DeclineHello declineHello) {}

    public void onConfirmedReceived(ConfirmRecived confirmRecived) {
        try {
            messageController.updateReceived(confirmRecived.getIdMessage());
            messageController.onLoadMessages(messageController.obtainContact(confirmRecived.getIdMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            throw new OperationException("Error al actualizar el estado del mensaje");
        }
    }

    public void onPinMessageReceived(PinMessage pinMessage) {
        String msgId = pinMessage.getIdMessage();
        try {
            String contactId = messageController.obtainContact(msgId);

            for (int i = 0; i < contactListModel.getSize(); i++) {
                Contact c = contactListModel.getElementAt(i);
                if (c.getId().equals(contactId)) {
                    c.setId_pin(msgId);
                    contactController.updatePin(c.getId(), msgId);
                    break;
                }
            }

            if (currentContact != null && currentContact.getId().equals(contactId)) {
                currentContact.setId_pin(msgId);
                contactController.updatePin(currentContact.getId(), msgId);

                Message pinnedMsg = null;
                for (int i = 0; i < messageListModel.getSize(); i++) {
                    if (messageListModel.getElementAt(i).getIdMessage().equals(msgId)) {
                        pinnedMsg = messageListModel.getElementAt(i);
                        break;
                    }
                }

                if (pinnedMsg != null) {
                    showPinnedMessageUI(pinnedMsg);
                }
            }
        } catch (Exception e) {
            System.out.println("Error al procesar mensaje fijado: " + e.getMessage());
        }
    }

    private void showPinnedMessageUI(Message msg) {
        SwingUtilities.invokeLater(() -> {
            currentPinnedMessageId = msg.getIdMessage();


            String textToShow = msg.getTypeMessage() == TypeMessage.IMAGE ?
                    "📷 Imagen" : msg.getBody();

            if (textToShow.length() > 50) {
                textToShow = textToShow.substring(0, 47) + "...";
            }

            pinnedMessageLabel.setText("📌 " + textToShow);
            pinnedMessagePanel.setVisible(true);
            revalidate();
            repaint();
        });
    }

    public void onUniqueMessageReceived(UniqueMessage uniqueMessage) {
        try {
            Message msgDb = new Message(
                    uniqueMessage.getIdMessage(),
                    uniqueMessage.getIdUser(),
                    uniqueMessage.getMessage(),
                    TypeMessage.UNIQUE,
                    StatusMessage.READ,
                    LocalDate.now().toString()
            );

            messageController.save(msgDb);

            if (currentContact != null && currentContact.getId().equals(uniqueMessage.getIdUser())) {
                SwingUtilities.invokeLater(() -> {
                    messageListModel.addElement(msgDb);
                    scrollToBottom();
                });
            }
        } catch (Exception e) {
            System.out.println("Error al procesar mensaje único: " + e.getMessage());
        }
    }

    // --- AQUÍ REEMPLAZAMOS EL MÉTODO VACÍO POR LA LÓGICA DE TEMAS ---
    public void onThemeReceived(Theme theme) {
        String senderId = theme.getIdUser();
        String themeId = theme.getIdTheme();

        try {
            // Actualizamos en base de datos
            ContactDao.getInstance().updateTheme(senderId, themeId);

            // Actualizamos en la lista visual de contactos
            for (int i = 0; i < contactListModel.getSize(); i++) {
                Contact c = contactListModel.getElementAt(i);
                if (c.getId().equals(senderId)) {
                    c.setTheme(themeId);
                    break;
                }
            }

            // Si es el contacto con el que estamos hablando, aplicamos el fondo inmediatamente
            if (currentContact != null && currentContact.getId().equals(senderId)) {
                currentContact.setTheme(themeId);
                applyThemeToChat(themeId);
            }
        } catch (Exception e) {
            System.out.println("Error al procesar el tema recibido: " + e.getMessage());
        }
    }

    private void applyThemeToChat(String themeNumber) {
        if (themeNumber == null || themeNumber.equals("0") || themeNumber.trim().isEmpty()) {
            chatBackgroundImage = null;
        } else {
            String imagePath = "";
            switch (themeNumber) {
                case "1": imagePath = "/images/Standard.jpg"; break;
                case "2": imagePath = "/images/Balatro.gif";break;
                case "3": imagePath = "/images/Terraria(Islas).png"; break;
                case "4": imagePath = "/images/Corrupcion.png"; break;
                case "5": imagePath = "/images/Jimbo.jpg"; break;
                default: break;
            }

            if (!imagePath.isEmpty()) {

                java.net.URL imgURL = getClass().getResource(imagePath);

                if (imgURL != null) {
                    System.out.println("✅ Imagen encontrada: " + imgURL);
                    chatBackgroundImage = new ImageIcon(imgURL).getImage();
                } else {
                    System.out.println("❌ ERROR: No se encontró la ruta: " + imagePath);
                    chatBackgroundImage = null;
                }
            }
        }
        if (messageList != null) {
            messageList.repaint();
        }
    }
}