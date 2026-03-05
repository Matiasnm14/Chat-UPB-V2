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
//import edu.upb.chatupb_v2.repository.*;
import edu.upb.chatupb_v2.model.repository.enums.StatusMessage;
import edu.upb.chatupb_v2.model.repository.enums.TypeMessage;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
//import javax.swing.*;
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

    private JTextArea chatArea;
    private JTextField jTextMensaje;
    private JLabel jOnline;

    private DefaultListModel<Contact> contactListModel;
    private JList<Contact> contactList;

    // Añade esta variable para llevar el control del chat actual:
    @Getter
    private Contact currentContact;
    @Setter
    private ContactController contactController;
    @Setter
    private MessageController messageController;
    @Setter
    private UserController userController = new UserController(this);



    public JUi() {
        this.username = userController.onLoadUser();
        if (this.username == null || this.username.trim().isEmpty()) {
            System.exit(0);
        }
        initComponents();
        try {
            if (UserDao.getInstance().exist("name='"+username+"'"))
                userId = UserDao.getInstance().findByName(username).getId();
            else{
                userId = UUID.randomUUID().toString();
                UserDao.getInstance().save(new User(userId,username));
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        System.out.println("------------------------------------------------------------------");
        System.out.println("ID: " + userId);
        System.out.println("Nombre: " + username);
        System.out.println("------------------------------------------------------------------");
        Controller.getInstance().addUi(this);


    }
//    public void addModel(String contact){
//        if(!chatListModel.contains(contact)){
//            chatListModel.add(chatListModel.size(),contact);
//        }
//    }

    private String askForUsername() {
        //Campo de texto
        JTextField textField = new JTextField(20);

        // Filtro
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
        contactList.setFixedCellHeight(40); // Ajusta la altura si lo ves muy separado

        // ¡AQUÍ USAS TU RENDERER PERSONALIZADO!
        contactList.setCellRenderer(new ContactRender());


        contactList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                currentContact = contactList.getSelectedValue();
                if (currentContact != null) {
                    System.out.println(currentContact.getId());
                    messageController.onLoadMessages(currentContact.getId());
                    if (Controller.getInstance().getClients().containsKey(currentContact.getId())){
                        Controller.getInstance().sendConfirms(currentContact.getId());
                    }
                }
            }
        });

        JScrollPane leftScrollPane = new JScrollPane(contactList);
        leftScrollPane.setPreferredSize(new Dimension(250, 600));



        // ================= RIGHT PANEL =================

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        JScrollPane chatScrollPane = new JScrollPane(chatArea);

        jTextMensaje = new JTextField();
        JButton btnSend = new JButton("Enviar");

        JButton btnBuzz = new JButton("Buzz");
        JButton btnOffline = new JButton("Fuera de Línea");
        JButton btnNewConnection = new JButton("Nueva Conexión");
        JButton btnConectar = new JButton("Conectar a Contacto");

        jOnline = new JLabel("Status: Offline");

        // Top Panel (SOLO Buzz y Offline + Nueva Conexión)
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(btnConectar);
        topPanel.add(btnNewConnection);
        topPanel.add(btnBuzz);
        topPanel.add(btnOffline);


        // Bottom Panel (mensaje)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(jTextMensaje, BorderLayout.CENTER);
        bottomPanel.add(btnSend, BorderLayout.EAST);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(topPanel, BorderLayout.NORTH);
        rightPanel.add(chatScrollPane, BorderLayout.CENTER);
        rightPanel.add(bottomPanel, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(leftScrollPane, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        // ================= ACTIONS =================

        btnSend.addActionListener(e -> {
            String texto = jTextMensaje.getText().trim();

            if (!texto.isEmpty() && currentContact != null) {

                Controller.getInstance().sendMessage(texto, currentContact.getId());
                jTextMensaje.setText("");
            } else if (currentContact == null) {
                showError("Por favor, selecciona un contacto de la lista izquierda para chatear.");
            }
        });

        btnConectar.addActionListener(e -> {
            if (currentContact != null) {
                if (!Controller.getInstance().getClients().containsKey(currentContact.getId())) {
                    SwingUtilities.invokeLater(() ->{
                        Controller.getInstance().sendHello(currentContact.getIp(), currentContact.getName(), currentContact.getId());
                    });
                }
            }else showError("Seleccione un Contacto Primero");
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

    // ================= IChatView =================

    @Override
    public void updateStatus(String status) {
        jOnline.setText(status);
    }

    @Override
    public void showMessage(String message) {
        chatArea.append(message + "\n");
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
        String name = Controller.getInstance().getClients().get(chat.getIdUser()).getNombre();
        chatArea.append(name + " | " + chat.getMessage() + "\n");
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

    @Override
    public void onLoadContacts(List<Contact> contacts) {
        contactListModel.clear();
        if (contacts != null) {
            for (Contact c : contacts) {
                contactListModel.addElement(c);
            }
        }
    }
    @Override
    public String onLoadUser(List<User> users){
        if (users.isEmpty()){
            return askForUsername();
        }else return users.getFirst().getName();
    }

    @Override
    public void onLoadMessages(List<Message> messages) {
        chatArea.setText("");
        if (messages != null && currentContact != null) {
            for (Message msg : messages) {
                String senderName;
                if (msg.getStatusMessage().toString().equals("SENT")) {
                    senderName = "Tú";
                } else if (msg.getStatusMessage().toString().equals("RECEIVED")) {
                    senderName = "Tú (Recibido)";
                }else senderName = currentContact.getName();
                chatArea.append(senderName + " | " + msg.getBody() + "\n");
            }
        }
        chatArea.setCaretPosition(chatArea.getDocument().getLength());

    }
    
    
    
    //=================LOGICA DE COMANDOS=============================
    public boolean onInvitationReceived(Invitation invitation) {

        boolean accepted = showInvitationDialog(invitation.getUserName(), invitation.getIdUser());
        Controller.getInstance().getPendingClients().getFirst().setUid(invitation.getIdUser());
        Controller.getInstance().addClients(Controller.getInstance().getPendingClients().getFirst());
        Controller.getInstance().getPendingClients().removeFirst();

        if (accepted) {
//            Controller.getInstance().addContact(invitation.getUserName());

            try {
                Contact nuevoContacto = new Contact();
                nuevoContacto.setId(invitation.getIdUser());
                nuevoContacto.setName(invitation.getUserName());
                nuevoContacto.setIp(Controller.getInstance().getClients().get(invitation.getIdUser()).getIp());
                nuevoContacto.setUserId(this.userId);

                nuevoContacto.setStateConnect(true);

                ContactDao.getInstance().save(nuevoContacto);

                SwingUtilities.invokeLater(() -> {

                    nuevoContacto.setId(invitation.getIdUser());
                    addModel(nuevoContacto);

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
        AcceptHello acceptHello = new AcceptHello(userId);
        return acceptHello;
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

    public void onByeReceived(){
//        SwingUtilities.invokeLater(() -> showByeNotification());
    }
    public void onAcceptHelloReceived(AcceptHello acceptHello) {}
    public void onDeleteMessageReceived(DeleteMessage deleteMessage) {
//        String id_message = deleteMessage.getIdMessage();
//        try {
//            MessageDAO.getInstance().delete(id_message);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }
    public void onDeclineHelloReceived(DeclineHello declineHello) {}

    public void onConfirmedReceived(ConfirmRecived confirmRecived) {

        System.out.println("Recibido");
        try {
            messageController.updateReceived(confirmRecived.getIdMessage());
//            chatArea.setText("");
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


    // ================= DB LOADERS =================
//    private void loadContacts() {
//        try {
//            chatListModel.clear();
//            java.util.List<Contact> contacts = ContactDao.getInstance().findByOwner(this.userId);
//            if (contacts != null) {
//                for (Contact c : contacts) {
//                    chatListModel.addElement(c);
//                }
//            }
//        } catch (Exception e) {
//            logger.severe("Error cargando contactos: " + e.getMessage());
//        }
//    }

//    private void loadMessages(String contactId) {
//        chatArea.setText("");
//        try {
//            java.util.List<Message> messages = MessageDAO.getInstance().findByContact(contactId);
//
//            if (messages != null) {
//                for (Message msg : messages) {
//                    String senderName;
//                    if (msg.getStatusMessage().toString().equals("SENT")) {
//                        senderName = this.username;
//                    } else {
//                        senderName = currentContact.getName();
//                    }
//                    chatArea.append(senderName + " | " + msg.getBody() + "\n");
//                }
//            }
//        } catch (Exception e) {
//            logger.severe("Error cargando mensajes: " + e.getMessage());
//        }
//    }

}