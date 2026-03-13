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
    @Setter
    private Contact currentContact;
    @Getter
    @Setter
    private ContactController contactController;
    @Setter
    private MessageController messageController;
    @Setter
    private UserController userController = new UserController(this);

    // --- NUEVOS COMPONENTES PARA IMÁGENES ---
    private JPanel imagePanel;
    private JLabel imageDropLabel;
    private String base64ImagePending = null;

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
        contactController.onLoadContacts();
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
                    System.out.println(currentContact.getId());
                    messageController.onLoadMessages(currentContact.getId());
                    if (Controller.getInstance().getClients().containsKey(currentContact.getId())) {
                        Controller.getInstance().sendConfirms(currentContact.getId());
                    }
                }
            }
        });

        JScrollPane leftScrollPane = new JScrollPane(contactList);
        leftScrollPane.setPreferredSize(new Dimension(250, 600));

        // ================= RIGHT PANEL =================

        messageListModel = new DefaultListModel<>();
        messageList = new JList<>(messageListModel);
        messageList.setCellRenderer(new ChatRender());
        messageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        messageList.setFocusable(false);

        JScrollPane chatScrollPane = new JScrollPane(messageList);

        // 1. Crear el menú contextual (Click derecho)
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem itemEliminarParaMi = new JMenuItem("Eliminar para mí");
        JMenuItem itemEliminarParaTodos = new JMenuItem("Eliminar para todos");
        JMenuItem itemFijarMensaje = new JMenuItem("Fijar Mensaje");
        JMenuItem itemCambiarTema = new JMenuItem("Cambiar tema de contacto");

        // Añadir las opciones al menú
        popupMenu.add(itemEliminarParaMi);
        popupMenu.add(itemEliminarParaTodos);
        popupMenu.addSeparator(); // Una pequeña línea divisoria visual
        popupMenu.add(itemFijarMensaje);
        popupMenu.addSeparator();
        popupMenu.add(itemCambiarTema);

        // 2. Agregar el listener a tu JList de mensajes
        messageList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                showPopup(e);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                showPopup(e);
            }

            private void showPopup(java.awt.event.MouseEvent e) {
                // Verificar si es un click derecho (PopupTrigger)
                if (e.isPopupTrigger()) {
                    // Obtener en qué índice de la lista se hizo click
                    int index = messageList.locationToIndex(e.getPoint());

                    // Validar que el click fue realmente sobre la burbuja de un mensaje
                    if (index != -1 && messageList.getCellBounds(index, index).contains(e.getPoint())) {

                        // Seleccionamos el mensaje visualmente en la lista
                        messageList.setSelectedIndex(index);
                        Message selectedMessage = messageList.getModel().getElementAt(index);

                        // LÓGICA DE INTERFAZ: Verificar si el mensaje es mío
                        // Usamos la misma lógica de tu ChatRender
                        boolean isMine = selectedMessage.getStatusMessage() != StatusMessage.READ;

                        // Ocultar o mostrar el botón "Eliminar para todos"
                        itemEliminarParaTodos.setVisible(isMine);

                        // Mostrar el menú exactamente donde está el ratón
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        // 3. Dejar listos los eventos (Solo Interfaz, sin lógica de negocio por ahora)
        itemEliminarParaMi.addActionListener(e -> {
            Message msg = messageList.getSelectedValue();
            System.out.println("UI: Clic en Eliminar para mí. ID Mensaje: " + msg.getId());
            // Aquí irá tu lógica futura...
        });

        itemEliminarParaTodos.addActionListener(e -> {
            Message msg = messageList.getSelectedValue();
            System.out.println("UI: Clic en Eliminar para todos. ID Mensaje: " + msg.getId());
            // Aquí irá tu lógica futura...
        });

        itemFijarMensaje.addActionListener(e -> {
            Message msg = messageList.getSelectedValue();
            System.out.println("UI: Clic en Fijar Mensaje. ID Mensaje: " + msg.getId());
            // Aquí irá tu lógica futura...
        });

        itemCambiarTema.addActionListener(e -> {
            System.out.println("UI: Clic en Cambiar tema de contacto.");
            // Aquí irá tu lógica futura (abrir un JColorChooser, etc.)...
        });

        jTextMensaje = new JTextField();
        JButton btnSend = new JButton("Enviar");
        JToggleButton btnToggleImage = new JToggleButton("📷"); // Botón para desplegar zona de imagen

        JButton btnBuzz = new JButton("Buzz");
        JButton btnOffline = new JButton("Fuera de Línea");
        JButton btnNewConnection = new JButton("Nueva Conexión");
        JButton btnConectar = new JButton("Conectar a Contacto");

        jOnline = new JLabel("Status: Offline");

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(btnConectar);
        topPanel.add(btnNewConnection);
        topPanel.add(btnBuzz);
        topPanel.add(btnOffline);

        // --- INICIO ZONA DE IMAGEN ---
        imagePanel = new JPanel(new BorderLayout());
        imagePanel.setVisible(false); // Oculto por defecto
        imagePanel.setPreferredSize(new Dimension(0, 120));
        imagePanel.setBorder(BorderFactory.createTitledBorder("Enviar Imagen"));

        imageDropLabel = new JLabel("Arrastra y suelta una imagen aquí", SwingConstants.CENTER);
        imageDropLabel.setBorder(BorderFactory.createDashedBorder(Color.GRAY, 2, 5, 2, false));

        JButton btnSendImage = new JButton("Enviar Imagen");
        btnSendImage.setEnabled(false); // Se habilita al cargar imagen

        imagePanel.add(imageDropLabel, BorderLayout.CENTER);
        imagePanel.add(btnSendImage, BorderLayout.EAST);

        // Lógica de Drag and Drop
        imageDropLabel.setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

                    if (droppedFiles != null && !droppedFiles.isEmpty()) {
                        File file = droppedFiles.get(0);

                        // Validación básica de que sea imagen (por extensión)
                        String name = file.getName().toLowerCase();
                        if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif")) {
                            // Convertir a Base 64
                            byte[] fileContent = Files.readAllBytes(file.toPath());
                            base64ImagePending = Base64.getEncoder().encodeToString(fileContent);

                            // Mostrar preview escalado
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
        // --- FIN ZONA DE IMAGEN ---

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(btnToggleImage, BorderLayout.WEST);
        inputPanel.add(jTextMensaje, BorderLayout.CENTER);
        inputPanel.add(btnSend, BorderLayout.EAST);

        // Contenedor sur que agrupa la zona de imagen y el input de texto
        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.add(imagePanel, BorderLayout.NORTH);
        bottomContainer.add(inputPanel, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(topPanel, BorderLayout.NORTH);
        rightPanel.add(chatScrollPane, BorderLayout.CENTER);
        rightPanel.add(bottomContainer, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(leftScrollPane, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        // ================= ACTIONS =================

        // Mostrar/Ocultar panel de imagen
        btnToggleImage.addActionListener(e -> {
            imagePanel.setVisible(btnToggleImage.isSelected());
            revalidate();
            repaint();
        });

        // Enviar Imagen
        btnSendImage.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                if (base64ImagePending != null && currentContact != null) {
                    String destinationId = currentContact.getId();

                    // NOTA: Asegúrate de que tu clase ImageChat reciba estos parámetros en su constructor
                    ImageChat imageChat = new ImageChat(userId, UUID.randomUUID().toString(), base64ImagePending);
                    Controller.getInstance().sendMessage(imageChat, destinationId);

                    // Limpiar después de enviar
                    base64ImagePending = null;
                    imageDropLabel.setIcon(null);
                    imageDropLabel.setText("Arrastra y suelta una imagen aquí");
                    btnSendImage.setEnabled(false);
                    btnToggleImage.setSelected(false);
                    imagePanel.setVisible(false);
                    revalidate();
                } else if (currentContact == null) {
                    showError("Selecciona un contacto primero.");
                }
            });

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
        btnOffline.addActionListener(e -> Controller.getInstance().sendBye());

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

    // ================= IChatView =================
    // (El resto de métodos sobreescritos de la interfaz se mantienen exactamente igual)

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
//        messageListModel.addElement(sysMsg);
        messageController.onLoadMessages(currentContact.getId());
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

    // Añadir a JUi.java

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

    @Override
    public void onLoadContacts(List<Contact> contacts) {
        contactListModel.clear();
        if (contacts != null) {
            for (Contact c : contacts) {
                if (Controller.getInstance().getClients().containsKey(c.getId()))
                    c.setStateConnect(true);
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
    }

    //================= LOGICA DE COMANDOS =============================
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

                contactController.save(nuevoContacto);

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
        SwingUtilities.invokeLater(() -> showBuzzNotification(finalName));
    }

    public void onByeReceived() {}

    public void onAcceptHelloReceived(AcceptHello acceptHello) {}

    public void onDeleteMessageReceived(DeleteMessage deleteMessage) {}

    public void onDeclineHelloReceived(DeclineHello declineHello) {}

    public void onConfirmedReceived(ConfirmRecived confirmRecived) {
        System.out.println("Recibido");
        try {
            messageController.updateReceived(confirmRecived.getIdMessage());
            messageController.onLoadMessages(messageController.obtainContact(confirmRecived.getIdMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            throw new OperationException("Error al actualizar el estado del mensaje");
        }
    }

    public void onPinMessageReceived(PinMessage pinMessage) {}

    public void onUniqueMessageReceived(UniqueMessage uniqueMessage) {
        System.out.println("MENSAJE ÚNICO");
    }

    public void onThemeReceived(Theme theme) {}
}