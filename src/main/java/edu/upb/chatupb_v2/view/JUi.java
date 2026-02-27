package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.bl.ChatService;
import edu.upb.chatupb_v2.controller.ContactController;
import edu.upb.chatupb_v2.controller.Controller;
import edu.upb.chatupb_v2.controller.MessageController;
import edu.upb.chatupb_v2.model.entities.Contact;
import edu.upb.chatupb_v2.model.entities.Message;
import edu.upb.chatupb_v2.model.entities.User;
import edu.upb.chatupb_v2.model.entities.comands.*;
import edu.upb.chatupb_v2.model.network.SocketClient;
import edu.upb.chatupb_v2.model.repository.*;
//import edu.upb.chatupb_v2.repository.*;
import edu.upb.chatupb_v2.model.repository.enums.StatusMessage;
import edu.upb.chatupb_v2.model.repository.enums.TypeMessage;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

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

    private DefaultListModel<Contact> chatListModel;
    private JList<Contact> chatList;

    // Añade esta variable para llevar el control del chat actual:
    private Contact currentContact;
    @Setter
    private ContactController contactController;
    @Setter
    private MessageController messageController;

    public JUi() {
        this.username = askForUsername();
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

        System.out.println(userId);
        Controller.getInstance().addUi(this);


    }
//    public void addModel(String contact){
//        if(!chatListModel.contains(contact)){
//            chatListModel.add(chatListModel.size(),contact);
//        }
//    }

    private String askForUsername() {
        return JOptionPane.showInputDialog(
                this,
                "Ingresa tu nombre de usuario:",
                "Bienvenida a ChatUPB",
                JOptionPane.QUESTION_MESSAGE
        );
    }

    private void initComponents() {
        setTitle("ChatUPB");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // ================= LEFT PANEL =================

        chatListModel = new DefaultListModel<>();
        chatList = new JList<>(chatListModel);
        chatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chatList.setFixedCellHeight(40); // Ajusta la altura si lo ves muy separado

        // ¡AQUÍ USAS TU RENDERER PERSONALIZADO!
        chatList.setCellRenderer(new ContactRender());


        chatList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                currentContact = chatList.getSelectedValue();
                if (currentContact != null) {
                    messageController.onLoadMessages(currentContact.getId());
                }
            }
        });

        JScrollPane leftScrollPane = new JScrollPane(chatList);
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

        jOnline = new JLabel("Status: Offline");

        // Top Panel (SOLO Buzz y Offline + Nueva Conexión)
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
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
    public void onLoadContacts(List<Contact> contacts) {
        chatListModel.clear();
        if (contacts != null) {
            for (Contact c : contacts) {
                chatListModel.addElement(c);
            }
        }
    }

    @Override
    public void onLoadMessages(List<Message> messages) {
        chatArea.setText("");
        if (messages != null) {
            for (Message msg : messages) {
                String senderName;
                if (msg.getStatusMessage().toString().equals("SENT")) {
                    senderName = "Tú";
                } else {
                    senderName = currentContact.getName();
                }
                chatArea.append(senderName + " | " + msg.getBody() + "\n");
            }
        }
    }
    
    
    
    //=================LOGICA DE COMANDOS=============================
    public void onInvitationReceived(Invitation invitation) {

        boolean accepted = showInvitationDialog(invitation.getUserName(), invitation.getIdUser());
        Controller.getInstance().getPendingClients().getFirst().setUid(invitation.getIdUser());
        Controller.getInstance().addClients(Controller.getInstance().getPendingClients().getFirst());
        Controller.getInstance().getPendingClients().removeFirst();

        if (accepted) {
//            Controller.getInstance().addContact(invitation.getUserName());
            Accept acp = new Accept(userId, username);
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

                SocketClient sc = Controller.getInstance().getClients().get(invitation.getIdUser());
                if (sc != null) {
                    sc.send(acp.createFormat());
                }
            } catch (Exception e) {
                System.out.println("Error procesando invitación aceptada: " + e.getMessage());
            }

        } else {
            Decline dec = new Decline();
            try {
                SocketClient sc = Controller.getInstance().getClients().get(invitation.getIdUser());
                if (sc != null)
                    sc.send(dec.createFormat());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void onAcceptReceived(Accept accept) {
        SwingUtilities.invokeLater(() -> {

            Controller.getInstance().getPendingClients().getFirst().setUid(accept.getIdUser());
            Controller.getInstance().getPendingClients().getFirst().setUserName(accept.getUserName());
            Controller.getInstance().addClients(Controller.getInstance().getPendingClients().getFirst());
            Controller.getInstance().getPendingClients().removeFirst();

//            Controller.getInstance().addContact(accept.getUserName());
            updateStatus("Status: Online");
            showMessage("Conexión Aceptada");
            try {
                SocketClient sc = Controller.getInstance().getClients().get(accept.getIdUser());
                if (sc != null) {
                    Contact nuevoContacto = new Contact();
                    nuevoContacto.setId(accept.getIdUser());
                    nuevoContacto.setName(accept.getUserName());
                    nuevoContacto.setIp(sc.getIp());
                    nuevoContacto.setUserId(this.userId);
                    nuevoContacto.setStateConnect(true);

                    ContactDao.getInstance().save(nuevoContacto);

                   
                    nuevoContacto.setId(accept.getIdUser());
                    addModel(nuevoContacto);
                    

                    updateStatus("Status: Online");
                    showMessage("Conexión Aceptada con " + accept.getUserName());
                }
            } catch (Exception e) {
                System.out.println("Error guardando contacto al aceptar: " + e.getMessage());
            }
        });
    }


    public void onDeclineReceived(Decline decline) {
        SwingUtilities.invokeLater(() -> {
            updateStatus("Status: Rejected");
            showMessage("Conexión Rechazada");
            
        });
    }


    public void onHelloReceived(Hello hello) {
        AcceptHello acceptHello = new AcceptHello(userId);
        SocketClient client = Controller.getInstance().getClients().get(hello.getIdUser());
        if (client != null) {
            try {
                client.send(acceptHello.createFormat());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }


    public void onChatReceived(Chat chat) {
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
        ConfirmRecived confirmRecived = new ConfirmRecived(chat.getIdMessage());
        SocketClient client = Controller.getInstance().getClients().get(chat.getIdUser());
        try {
            client.send(confirmRecived.createFormat());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void onBuzzingReceived(Buzzing buzzing) {
        String name = "Desconocido";
        SocketClient sc = Controller.getInstance().getClients().get(buzzing.getIdUser());
        if (sc != null) {
            name = sc.getNombre();
        }
        String finalName = name;
        SwingUtilities.invokeLater(() -> showBuzzNotification(finalName));
    }

    public void onByeReceived(Bye bye){
        String id = bye.getIdUser();
        System.out.println("ID: " + id );
        SocketClient sc = Controller.getInstance().getClients().get(id);
        if (sc != null){
            sc.close();
        }
        SwingUtilities.invokeLater(() -> showByeNotification(id));
    }
    public void onAcceptHelloReceived(AcceptHello acceptHello) {}
    public void onDeclineHelloReceived(DeclineHello declineHello) {}
    public void onConfirmedReceived(ConfirmRecived confirmRecived) {
        System.out.println("Recibido");
//        try {
//            MessageDAO.getInstance().updateMessage(confirmRecived.getIdMessage());
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }

    public void onDeleteMessageReceived(DeleteMessage deleteMessage) {
//        String id_message = deleteMessage.getIdMessage();
//        try {
//            MessageDAO.getInstance().delete(id_message);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
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